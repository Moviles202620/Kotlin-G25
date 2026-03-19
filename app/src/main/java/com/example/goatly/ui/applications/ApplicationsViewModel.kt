package com.example.goatly.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.data.network.MyApplicationsResponseDto
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
                    status = statusFilter
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
}
