package com.example.goatly.ui.auth

import android.util.Log
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
                    // Sprint 3: Eventual Connectivity — persist user profile locally
                    TokenManager.saveUserProfile(result)
                    Log.d("GOATLY_EVC", "login — success, profile saved locally for ${result.email}")
                    onSuccess()
                } else {
                    Log.w("GOATLY_EVC", "login — failed, wrong credentials")
                    _loginError.value = "Correo o contraseña incorrectos"
                }
            } catch (e: Exception) {
                if (e.message == "STAFF_ROLE") {
                    Log.w("GOATLY_EVC", "login — blocked, staff role detected")
                    _loginError.value = "Esta app es solo para estudiantes. Si eres funcionario, usa la aplicación correspondiente."
                } else if (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.net.SocketTimeoutException) {
                    Log.e("GOATLY_EVC", "login — no network, cannot authenticate: ${e.message}")
                    _loginError.value = "Para iniciar sesión, por favor conéctese a internet."
                } else {
                    Log.e("GOATLY_EVC", "login — unexpected error: ${e.message}")
                    _loginError.value = "Correo o contraseña incorrectos"
                }
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        Log.d("GOATLY_EVC", "logout — clearing token and local profile for ${_user.value?.email}")
        authRepository.logout()
        // Sprint 3: Eventual Connectivity — clear local profile on explicit logout
        TokenManager.clearUserProfile()
        _user.value = null
    }

    fun register(name: String, email: String, password: String, major: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            val result = authRepository.registerSuspend(name, email, password, major, role)
            if (result != null) {
                _user.value = result
                // Sprint 3: Eventual Connectivity — persist user profile locally
                TokenManager.saveUserProfile(result)
                Log.d("GOATLY_EVC", "register — success, profile saved locally for ${result.email}")
                onSuccess()
            } else {
                Log.w("GOATLY_EVC", "register — failed")
                _loginError.value = "No se pudo crear la cuenta. Intenta de nuevo."
            }
            _isLoading.value = false
        }
    }

    // Sprint 3: Eventual Connectivity — restoreSession with offline fallback
    // If network is unavailable, restore user profile from SharedPreferences
    // instead of clearing the token and forcing re-login
    fun restoreSession(onDone: () -> Unit) {
        viewModelScope.launch {
            Log.d("GOATLY_EVC", "restoreSession — attempting network restore")
            try {
                val token = TokenManager.getAccessToken()
                if (token != null) {
                    val me = RetrofitClient.api.getMe("Bearer $token")
                    val user = UserModel(
                        name = me.name,
                        email = me.email,
                        major = me.department,
                        role = me.role,
                        university = "Universidad de los Andes",
                        language = me.language,
                        isDarkMode = me.isDarkMode
                    )
                    _user.value = user
                    // Update cached profile with fresh data
                    TokenManager.saveUserProfile(user)
                    Log.d("GOATLY_EVC", "restoreSession — network ok, fresh profile loaded for ${user.email}")
                } else {
                    Log.w("GOATLY_EVC", "restoreSession — no token found, skipping")
                }
            } catch (e: Exception) {
                Log.e("GOATLY_EVC", "restoreSession — network failed (${e.message}), trying local profile")
                // Sprint 3: Eventual Connectivity — fallback to locally cached profile
                val cachedUser = TokenManager.getUserProfile()
                if (cachedUser != null) {
                    _user.value = cachedUser
                    Log.d("GOATLY_EVC", "restoreSession — offline fallback ok, session restored for ${cachedUser.email}")
                } else {
                    // No cached profile and no network — force re-login
                    Log.w("GOATLY_EVC", "restoreSession — no cached profile found, clearing token and forcing re-login")
                    TokenManager.clear()
                }
            }
            onDone()
        }
    }
    // Sprint 3: Eventual Connectivity — END
}