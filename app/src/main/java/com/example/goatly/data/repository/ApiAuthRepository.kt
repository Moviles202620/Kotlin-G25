package com.example.goatly.data.repository

import com.example.goatly.data.model.UserModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.LoginRequest
import com.example.goatly.data.network.TokenManager

class ApiAuthRepository(private val api: ApiService) : AuthRepository {

    private var _currentUser: UserModel? = null

    override fun login(email: String, password: String): UserModel? {
        throw UnsupportedOperationException("Use loginSuspend instead")
    }

    suspend fun loginSuspend(email: String, password: String): UserModel? {
        return try {
            val response = api.login(LoginRequest(email.trim().lowercase(), password))
            TokenManager.saveTokens(response.accessToken, response.refreshToken)
            val me = api.getMe("Bearer ${response.accessToken}")
            _currentUser = UserModel(
                name = me.name,
                email = me.email,
                major = me.department,
                university = "Universidad de los Andes",
            )
            _currentUser
        } catch (e: Exception) {
            // Fallback mock mientras el backend no está disponible
            if (email.trim().lowercase().endsWith("@uniandes.edu.co") && password.length >= 4) {
                _currentUser = UserModel(
                    name = "Estudiante Uniandes",
                    email = email.trim().lowercase(),
                    major = "Ingeniería de Sistemas",
                    university = "Universidad de los Andes",
                )
                _currentUser
            } else null
        }
    }

    override fun logout() {
        _currentUser = null
        TokenManager.clear()
    }

    override fun currentUser(): UserModel? = _currentUser
}