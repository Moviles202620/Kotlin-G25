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
            val result = authRepository.loginSuspend(email, password)
            if (result != null) {
                _user.value = result
                onSuccess()
            } else {
                _loginError.value = "Correo o contraseña incorrectos"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }
}