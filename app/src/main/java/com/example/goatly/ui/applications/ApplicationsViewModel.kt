package com.example.goatly.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.data.network.MyApplicationsResponseDto
import com.example.goatly.data.network.ApplyRequest
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ApplicationsViewModel : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val response: MyApplicationsResponseDto) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter: StateFlow<String?> = _activeFilter.asStateFlow()

    private val _navigateToLogin = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToLogin: SharedFlow<Unit> = _navigateToLogin.asSharedFlow()

    private val _selectedApplication = MutableStateFlow<MyApplicationItemDto?>(null)
    val selectedApplication: StateFlow<MyApplicationItemDto?> = _selectedApplication.asStateFlow()

    private val _applyResult = MutableSharedFlow<Result<Unit>>(extraBufferCapacity = 1)
    val applyResult: SharedFlow<Result<Unit>> = _applyResult.asSharedFlow()

    fun selectApplication(app: MyApplicationItemDto) { _selectedApplication.value = app }

    /** Reload all applications (resets any active filter). Used by ProfileScreen. */
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
                val response = RetrofitClient.api.getMyApplications(
                    token = "Bearer $token",
                    status = statusFilter,
                    detailed = true
                )
                _uiState.value = UiState.Success(response)
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    _navigateToLogin.tryEmit(Unit)
                } else {
                    val detail = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                    _uiState.value = UiState.Error(detail ?: "Error ${e.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error de conexión")
            }
        }
    }

    fun applyToOffer(offerId: Int) {
        viewModelScope.launch {
            val token = TokenManager.getAccessToken() ?: run {
                _navigateToLogin.tryEmit(Unit)
                return@launch
            }
            try {
                // This method is deprecated - applications must be made with full details via OfferDetailScreen
                // Keeping for backward compatibility but won't actually be used
                _applyResult.tryEmit(Result.failure(Exception("Use OfferDetailScreen to apply with full details")))
                return@launch
            } catch (e: HttpException) {
                if (e.code() == 401) {
                    _navigateToLogin.tryEmit(Unit)
                } else {
                    val detail = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                    _applyResult.tryEmit(Result.failure(Exception(detail ?: "Error ${e.code()}")))
                }
            } catch (e: Exception) {
                _applyResult.tryEmit(Result.failure(e))
            }
        }
    }

    //BQ: Top Offers

    data class TopOfferItem(val title: String, val total: Int)

    private val _topOffers = MutableStateFlow<List<TopOfferItem>>(emptyList())
    val topOffers: StateFlow<List<TopOfferItem>> = _topOffers.asStateFlow()

    fun loadTopOffers() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.api.getTopOffers()
                _topOffers.value = result.map { TopOfferItem(it.title, it.total) }
            } catch (e: Exception) {
                android.util.Log.e("GoatlyNet", "loadTopOffers failed: ${e.message}")
            }
        }
    }
}