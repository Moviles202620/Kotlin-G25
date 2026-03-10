package com.example.goatly.ui.auth

import androidx.lifecycle.ViewModel
import com.example.goatly.data.model.UserModel
import com.example.goatly.data.repository.AuthRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val authRepository: AuthRepository = RepositoryProvider.authRepository
) : ViewModel() {

    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user.asStateFlow()

    val isLoggedIn: Boolean get() = _user.value != null

    fun login(email: String, password: String): Boolean {
        val result = authRepository.login(email, password)
        _user.value = result
        return result != null
    }

    fun logout() {
        authRepository.logout()
        _user.value = null
    }
}
