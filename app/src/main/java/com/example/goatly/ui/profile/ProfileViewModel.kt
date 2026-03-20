package com.example.goatly.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.ChangePasswordRequest
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import com.example.goatly.data.network.UpdateProfileRequest
import com.example.goatly.data.network.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun seedFromAuth(name: String, email: String, department: String, language: String = "es", isDarkMode: Boolean = false) {
        _user.value = UserResponse(
            id = 0,
            name = name,
            email = email,
            department = department,
            role = "student",
            language = language,
            isDarkMode = isDarkMode
        )
    }

    fun loadProfile() {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _user.value = api.getMe("Bearer $token")
            } catch (_: Exception) {
                _error.value = "No se pudo cargar el perfil"
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(name: String, department: String, language: String, onSuccess: () -> Unit) {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentDarkMode = _user.value?.isDarkMode ?: false
                _user.value = api.updateProfile(
                    "Bearer $token",
                    UpdateProfileRequest(name.trim(), department, language, currentDarkMode)
                )
                onSuccess()
            } catch (_: Exception) {
                _error.value = "No se pudo actualizar el perfil"
            }
            _isLoading.value = false
        }
    }

    fun changePassword(currentPw: String, newPw: String, confirmPw: String, onSuccess: () -> Unit) {
        val token = TokenManager.getAccessToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                api.changePassword(
                    "Bearer $token",
                    ChangePasswordRequest(currentPw, newPw, confirmPw)
                )
                onSuccess()
            } catch (e: HttpException) {
                _error.value = if (e.code() == 400) "Contraseña actual incorrecta" else "Error al cambiar la contraseña"
            } catch (_: Exception) {
                _error.value = "Error al cambiar la contraseña"
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clear() {
        _user.value = null
        _error.value = null
    }
}
