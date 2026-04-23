package com.example.goatly.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.model.UserModel
import com.example.goatly.data.repository.ApiAuthRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager

class AuthViewModel(
    private val authRepository: ApiAuthRepository = RepositoryProvider.authRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    val isLoggedIn: Boolean get() = _user.value != null

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                val result = authRepository.loginSuspend(email, password)
                if (result != null) {
                    _user.value = result
                    onSuccess()
                } else {
                    _loginError.value = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                if (e.message == "STAFF_ROLE") {
                    _loginError.value = "Esta app es solo para estudiantes. Si eres funcionario, usa la aplicación correspondiente."
                } else {
                    _loginError.value = "Correo o contraseña incorrectos"
                }
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }

    fun register(name: String, email: String, password: String, major: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val result = authRepository.registerSuspend(name, email, password, major, role)
            if (result != null) {
                _user.value = result
                onSuccess()
            } else {
                _loginError.value = "No se pudo crear la cuenta. Intenta de nuevo."
            }
            _isLoading.value = false
        }
    }
    fun restoreSession(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getAccessToken()
                if (token != null) {
                    val me = RetrofitClient.api.getMe("Bearer $token")
                    _user.value = UserModel(
                        name = me.name,
                        email = me.email,
                        major = me.department,
                        role = me.role,
                        university = "Universidad de los Andes",
                        language = me.language,
                        isDarkMode = me.isDarkMode
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GoatlyNet", "restoreSession failed: ${e.message}")
                TokenManager.clear()
            }
            onDone()
        }
    }
}