package com.example.goatly.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.LocalProvider
import com.example.goatly.data.local.db.toDto
import com.example.goatly.data.local.db.toEntity
import com.example.goatly.data.network.ApplicationStatsDto
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

    // BQ Sprint 2: Top Offers
    data class TopOfferItem(val title: String, val total: Int)

    private val _topOffers = MutableStateFlow<List<TopOfferItem>>(emptyList())
    val topOffers: StateFlow<List<TopOfferItem>> = _topOffers.asStateFlow()

    // BQ3 — Rating breakdown by dimension (computed locally from Room)
    data class RatingStats(
        val avgOverall: Float,
        val avgPunctuality: Float,
        val avgQuality: Float,
        val avgAttitude: Float,
        val ratedCount: Int
    )

    private val _ratingStats = MutableStateFlow<RatingStats?>(null)
    val ratingStats: StateFlow<RatingStats?> = _ratingStats.asStateFlow()

    // BQ Sprint 3: Avg applications per semester
    data class AvgPerSemesterItem(
        val semester: Int,
        val avgApplications: Float,
        val totalStudents: Int,
        val totalApplications: Int
    )

    private val _avgPerSemester = MutableStateFlow<List<AvgPerSemesterItem>>(emptyList())
    val avgPerSemester: StateFlow<List<AvgPerSemesterItem>> = _avgPerSemester.asStateFlow()

    // Semestre del usuario logueado — se setea desde StudentShell
    private val _userSemester = MutableStateFlow(0)
    val userSemester: StateFlow<Int> = _userSemester.asStateFlow()

    fun setUserSemester(semester: Int) {
        _userSemester.value = semester
    }

    // Cache de todas las aplicaciones para filtrado offline sin reset
    private val _allApplicationsCache = MutableStateFlow<List<MyApplicationItemDto>>(emptyList())

    init {
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

        // FIX bug reset: si ya hay datos cargados y solo cambia el filtro,
        // aplicar el filtro localmente sin mostrar Loading
        val currentApps = _allApplicationsCache.value
        if (statusFilter != null && currentApps.isNotEmpty()) {
            val filtered = currentApps.filter { it.status == statusFilter }
            val stats = ApplicationStatsDto(
                total = filtered.size,
                pending = filtered.count { it.status == "pending" },
                accepted = filtered.count { it.status == "accepted" },
                rejected = filtered.count { it.status == "rejected" }
            )
            if (_isOffline.value) {
                _uiState.value = UiState.SuccessOffline(filtered, stats)
            } else {
                val currentResponse = (_uiState.value as? UiState.Success)?.response
                if (currentResponse != null) {
                    _uiState.value = UiState.Success(
                        currentResponse.copy(
                            applications = filtered,
                            stats = stats
                        )
                    )
                    return
                }
            }
            if (_isOffline.value) return
        }

        viewModelScope.launch {
            // Solo mostrar Loading en la primera carga
            if (_allApplicationsCache.value.isEmpty()) {
                _uiState.value = UiState.Loading
            }

            val token = TokenManager.getAccessToken() ?: run {
                _navigateToLogin.tryEmit(Unit)
                return@launch
            }
            try {
                val (response, topOffersList, avgSemesterList) = coroutineScope {
                    val appsDeferred = async(Dispatchers.IO) {
                        android.util.Log.d("GOATLY_ASYNC", "apps START: ${System.currentTimeMillis()}")
                        retryWithBackoff {
                            // Siempre pedir todas las apps al backend, filtrar localmente
                            RetrofitClient.api.getMyApplications(
                                token = "Bearer $token",
                                status = null,
                                detailed = true
                            )
                        }.also { android.util.Log.d("GOATLY_ASYNC", "apps END: ${System.currentTimeMillis()}") }
                    }
                    val topOffersDeferred = async(Dispatchers.IO) {
                        RetrofitClient.api.getTopOffers()
                    }
                    val avgSemesterDeferred = async(Dispatchers.IO) {
                        try {
                            RetrofitClient.api.getAvgApplicationsPerSemester()
                        } catch (e: Exception) {
                            android.util.Log.w("GOATLY", "avg-per-semester not available: ${e.message}")
                            emptyList()
                        }
                    }
                    Triple(appsDeferred.await(), topOffersDeferred.await(), avgSemesterDeferred.await())
                }

                // Guardar en Room y LRU cache
                withContext(Dispatchers.IO) {
                    dao.insertAll(response.applications.map { it.toEntity() })
                }
                cache.putAll(response.applications)

                // Guardar todas las apps en cache local para filtrado sin reset
                _allApplicationsCache.value = response.applications

                _isOffline.value = false
                _topOffers.value = topOffersList.map { TopOfferItem(it.title, it.total) }
                _avgPerSemester.value = avgSemesterList.map {
                    AvgPerSemesterItem(
                        semester = it.semester,
                        avgApplications = it.avgApplications,
                        totalStudents = it.totalStudents,
                        totalApplications = it.totalApplications
                    )
                }

                // Aplicar filtro localmente sobre los datos frescos
                val appsToShow = if (statusFilter != null) {
                    response.applications.filter { it.status == statusFilter }
                } else {
                    response.applications
                }
                val statsToShow = ApplicationStatsDto(
                    total = appsToShow.size,
                    pending = appsToShow.count { it.status == "pending" },
                    accepted = appsToShow.count { it.status == "accepted" },
                    rejected = appsToShow.count { it.status == "rejected" }
                )
                _uiState.value = UiState.Success(
                    response.copy(applications = appsToShow, stats = statsToShow)
                )

                loadRatingStats()

            } catch (e: HttpException) {
                if (e.code() == 401) {
                    _navigateToLogin.tryEmit(Unit)
                } else {
                    fallbackToLocal(statusFilter)
                }
            } catch (e: Exception) {
                fallbackToLocal(statusFilter)
            }
        }
    }

    private suspend fun loadRatingStats() {
        val breakdown = withContext(Dispatchers.IO) { dao.getRatingBreakdown() }
        if (breakdown != null && breakdown.ratedCount > 0) {
            _ratingStats.value = RatingStats(
                avgOverall = breakdown.avgOverall,
                avgPunctuality = breakdown.avgPunctuality,
                avgQuality = breakdown.avgQuality,
                avgAttitude = breakdown.avgAttitude,
                ratedCount = breakdown.ratedCount
            )
        }
    }

    private suspend fun fallbackToLocal(statusFilter: String?) {
        // FIX offline filter: primero intentar filtrar sobre cache en memoria
        val memoryApps = _allApplicationsCache.value
        if (memoryApps.isNotEmpty()) {
            val filtered = if (statusFilter != null) {
                memoryApps.filter { it.status == statusFilter }
            } else memoryApps
            val stats = ApplicationStatsDto(
                total = filtered.size,
                pending = filtered.count { it.status == "pending" },
                accepted = filtered.count { it.status == "accepted" },
                rejected = filtered.count { it.status == "rejected" }
            )
            _isOffline.value = true
            _uiState.value = UiState.SuccessOffline(filtered, stats)
            loadRatingStats()
            return
        }

        // Si no hay cache en memoria, buscar en Room
        val cached = withContext(Dispatchers.IO) { dao.getAll() }
        if (cached.isNotEmpty()) {
            val allApps = cached.map { it.toDto() }
            _allApplicationsCache.value = allApps
            val filtered = if (statusFilter != null) {
                allApps.filter { it.status == statusFilter }
            } else allApps
            val stats = ApplicationStatsDto(
                total = filtered.size,
                pending = filtered.count { it.status == "pending" },
                accepted = filtered.count { it.status == "accepted" },
                rejected = filtered.count { it.status == "rejected" }
            )
            _isOffline.value = true
            _uiState.value = UiState.SuccessOffline(filtered, stats)
            loadRatingStats()
        } else {
            _isOffline.value = true
            _uiState.value = UiState.Error("Sin conexión y sin datos guardados localmente")
        }
    }

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