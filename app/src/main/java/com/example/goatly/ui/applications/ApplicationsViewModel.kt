package com.example.goatly.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.LocalProvider
import com.example.goatly.data.local.db.toDto
import com.example.goatly.data.local.db.toEntity
import com.example.goatly.data.network.ApplicationStatsDto
import com.example.goatly.data.network.ApplyRequest
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.data.network.MyApplicationsResponseDto
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ApplicationsViewModel : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val response: MyApplicationsResponseDto) : UiState()
        data class SuccessOffline(
            val applications: List<MyApplicationItemDto>,
            val stats: ApplicationStatsDto
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val dao = LocalProvider.db.applicationDao()
    private val cache = LocalProvider.appsCache

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter: StateFlow<String?> = _activeFilter.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredApplications = MutableStateFlow<List<MyApplicationItemDto>>(emptyList())
    val filteredApplications: StateFlow<List<MyApplicationItemDto>> = _filteredApplications.asStateFlow()

    private val _navigateToLogin = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToLogin: SharedFlow<Unit> = _navigateToLogin.asSharedFlow()

    private val _selectedApplication = MutableStateFlow<MyApplicationItemDto?>(null)
    val selectedApplication: StateFlow<MyApplicationItemDto?> = _selectedApplication.asStateFlow()

    private val _applyResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val applyResult: SharedFlow<Result<Unit>> = _applyResult.asSharedFlow()

    // BQ: Top Offers
    data class TopOfferItem(val title: String, val total: Int)

    private val _topOffers = MutableStateFlow<List<TopOfferItem>>(emptyList())
    val topOffers: StateFlow<List<TopOfferItem>> = _topOffers.asStateFlow()

    init {
        // Literal 4: Flow + debounce para búsqueda reactiva
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _uiState.map { state ->
                        val apps = when (state) {
                            is UiState.Success -> state.response.applications
                            is UiState.SuccessOffline -> state.applications
                            else -> emptyList()
                        }
                        if (query.isEmpty()) apps
                        else apps.filter { app ->
                            app.offer.title.contains(query, ignoreCase = true) ||
                            app.career?.contains(query, ignoreCase = true) == true
                        }
                    }
                }
                .collect { _filteredApplications.value = it }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectApplication(app: MyApplicationItemDto) {
        _selectedApplication.value = app
        cache.put(app.id.toString(), app)
    }

    fun getFromCache(id: String): MyApplicationItemDto? = cache.get(id)

    fun refresh() = load(null)

    fun load(statusFilter: String? = null) {
        _activeFilter.value = statusFilter
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val token = TokenManager.getAccessToken() ?: run {
                _navigateToLogin.tryEmit(Unit)
                return@launch
            }
            try {
                // Literal 4: async/await paralelo — apps y top offers en simultáneo
                val (response, topOffersList) = coroutineScope {
                    val appsDeferred = async(Dispatchers.IO) {
                        android.util.Log.d("GOATLY_ASYNC", "apps START: ${System.currentTimeMillis()}")
                        retryWithBackoff {
                            RetrofitClient.api.getMyApplications(
                                token = "Bearer $token",
                                status = statusFilter,
                                detailed = true
                            )
                        }.also { android.util.Log.d("GOATLY_ASYNC", "apps END: ${System.currentTimeMillis()}") }
                    }
                    val topOffersDeferred = async(Dispatchers.IO) {
                        android.util.Log.d("GOATLY_ASYNC", "topOffers START: ${System.currentTimeMillis()}")
                        RetrofitClient.api.getTopOffers()
                            .also { android.util.Log.d("GOATLY_ASYNC", "topOffers END: ${System.currentTimeMillis()}") }
                    }
                    Pair(appsDeferred.await(), topOffersDeferred.await())
                }

                // Literal 5+7: guardar en Room y LRU cache
                withContext(Dispatchers.IO) {
                    dao.insertAll(response.applications.map { it.toEntity() })
                }
                cache.putAll(response.applications)

                _isOffline.value = false
                _uiState.value = UiState.Success(response)
                _topOffers.value = topOffersList.map { TopOfferItem(it.title, it.total) }

            } catch (e: HttpException) {
                if (e.code() == 401) {
                    _navigateToLogin.tryEmit(Unit)
                } else {
                    fallbackToLocal(statusFilter)
                }
            } catch (e: Exception) {
                // Literal 6: fallback a Room cuando no hay red
                fallbackToLocal(statusFilter)
            }
        }
    }

    private suspend fun fallbackToLocal(statusFilter: String?) {
        val cached = withContext(Dispatchers.IO) {
            if (statusFilter != null) dao.getByStatus(statusFilter)
            else dao.getAll()
        }
        if (cached.isNotEmpty()) {
            val apps = cached.map { it.toDto() }
            val stats = ApplicationStatsDto(
                total = apps.size,
                pending = apps.count { it.status == "pending" },
                accepted = apps.count { it.status == "accepted" },
                rejected = apps.count { it.status == "rejected" }
            )
            _isOffline.value = true
            _uiState.value = UiState.SuccessOffline(apps, stats)
        } else {
            _isOffline.value = true
            _uiState.value = UiState.Error("Sin conexión y sin datos guardados localmente")
        }
    }

    // Literal 6: retry con backoff exponencial (1s, 2s, 4s)
    private suspend fun <T> retryWithBackoff(
        times: Int = 3,
        initialDelayMs: Long = 1000L,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        repeat(times) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < times - 1) delay(initialDelayMs * (1L shl attempt))
            }
        }
        throw lastException!!
    }

    fun applyToOffer(offerId: Int) {
        viewModelScope.launch {
            val token = TokenManager.getAccessToken() ?: run {
                _navigateToLogin.tryEmit(Unit)
                return@launch
            }
            try {
                _applyResult.tryEmit(Result.failure(Exception("Use OfferDetailScreen to apply with full details")))
            } catch (e: HttpException) {
                if (e.code() == 401) _navigateToLogin.tryEmit(Unit)
                else {
                    val detail = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                    _applyResult.tryEmit(Result.failure(Exception(detail ?: "Error ${e.code()}")))
                }
            } catch (e: Exception) {
                _applyResult.tryEmit(Result.failure(e))
            }
        }
    }
}
