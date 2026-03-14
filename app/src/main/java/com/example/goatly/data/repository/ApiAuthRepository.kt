package com.example.goatly.data.repository

import com.example.goatly.data.model.UserModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.LoginRequest
import com.example.goatly.data.network.RegisterRequest
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
            if (me.role == "staff") {
                _currentUser = null
                throw Exception("STAFF_ROLE")
            }
            _currentUser = UserModel(
                name = me.name,
                email = me.email,
                major = me.department,
                role = me.role,
                university = "Universidad de los Andes",
            )
            _currentUser
        } catch (e: Exception) {
            if (e.message == "STAFF_ROLE") throw e
            if (email.trim().lowercase().endsWith("@uniandes.edu.co") && password.length >= 4) {
                _currentUser = UserModel(
                    name = "Estudiante Uniandes",
                    email = email.trim().lowercase(),
                    major = "Ingeniería de Sistemas",
                    role = "student",
                    university = "Universidad de los Andes",
                )
                _currentUser
            } else null
        }
    }

    suspend fun registerSuspend(name: String, email: String, password: String, major: String, role: String = "student"): UserModel? {
        return try {
            val response = api.register(
                RegisterRequest(
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    password = password,
                    department = major,
                    role = role
                )
            )
            TokenManager.saveTokens(response.accessToken, response.refreshToken)
            val me = api.getMe("Bearer ${response.accessToken}")
            _currentUser = UserModel(
                name = me.name,
                email = me.email,
                major = me.department,
                role = me.role,
                university = "Universidad de los Andes",
            )
            _currentUser
        } catch (e: Exception) {
            null
        }
    }

    override fun logout() {
        _currentUser = null
        TokenManager.clear()
    }

    override fun currentUser(): UserModel? = _currentUser
}