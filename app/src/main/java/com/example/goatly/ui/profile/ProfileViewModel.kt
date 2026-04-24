package com.example.goatly.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.LocalProvider
import com.example.goatly.data.network.ChangePasswordRequest
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import com.example.goatly.data.network.UpdateProfileRequest
import com.example.goatly.data.network.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ProfileViewModel : ViewModel() {

    private val api = RetrofitClient.api
    private val userPrefs = LocalProvider.userPrefs

    private val _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

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
                // Literal 4: withContext(Dispatchers.IO) para no bloquear el main thread
                val result = withContext(Dispatchers.IO) {
                    api.getMe("Bearer $token")
                }
                _user.value = result
                _isOffline.value = false
                // Literal 5: guardar en DataStore para acceso offline
                withContext(Dispatchers.IO) {
                    userPrefs.saveUserPreferences(
                        name = result.name,
                        email = result.email,
                        department = result.department,
                        language = result.language,
                        isDarkMode = result.isDarkMode
                    )
                }
            } catch (_: Exception) {
                // Literal 6: fallback a DataStore cuando no hay red
                val hasLocal = withContext(Dispatchers.IO) { userPrefs.hasData() }
                if (hasLocal) {
                    _isOffline.value = true
                    // Mantener el valor actual si ya fue cargado (seedFromAuth o carga previa)
                    if (_user.value == null) {
                        _error.value = "No se pudo cargar el perfil"
                    }
                } else {
                    _error.value = "No se pudo cargar el perfil"
                }
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
                // Literal 4: withContext(Dispatchers.IO)
                val result = withContext(Dispatchers.IO) {
                    api.updateProfile(
                        "Bearer $token",
                        UpdateProfileRequest(name.trim(), department, language, currentDarkMode)
                    )
                }
                _user.value = result
                // Literal 5: actualizar DataStore con nuevo perfil
                withContext(Dispatchers.IO) {
                    userPrefs.saveUserPreferences(
                        name = result.name,
                        email = result.email,
                        department = result.department,
                        language = result.language,
                        isDarkMode = result.isDarkMode
                    )
                }
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
                // Literal 4: withContext(Dispatchers.IO)
                withContext(Dispatchers.IO) {
                    api.changePassword(
                        "Bearer $token",
                        ChangePasswordRequest(currentPw, newPw, confirmPw)
                    )
                }
                onSuccess()
            } catch (e: HttpException) {
                _error.value = if (e.code() == 400) "Contraseña actual incorrecta" else "Error al cambiar la contraseña"
            } catch (_: Exception) {
                _error.value = "Error al cambiar la contraseña"
            }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }

    fun clear() {
        _user.value = null
        _error.value = null
        _isOffline.value = false
    }
}
