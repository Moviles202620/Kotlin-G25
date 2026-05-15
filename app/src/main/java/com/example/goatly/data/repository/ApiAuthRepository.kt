package com.example.goatly.data.repository

import com.example.goatly.data.model.UserModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.LoginRequest
import com.example.goatly.data.network.RegisterRequest
import com.example.goatly.data.network.TokenManager
import retrofit2.HttpException

class ApiAuthRepository(private val api: ApiService) : AuthRepository {

    private var _currentUser: UserModel? = null

    override fun login(email: String, password: String): UserModel? {
        throw UnsupportedOperationException("Use loginSuspend instead")
    }

    suspend fun loginSuspend(email: String, password: String): UserModel? {
        val normalizedEmail = email.trim().lowercase()
        return try {
            val response = api.login(LoginRequest(normalizedEmail, password))
            TokenManager.saveTokens(response.accessToken, response.refreshToken)
            val me = response.user
            if (me.role == "staff") throw Exception("STAFF_ROLE")
            _currentUser = UserModel(
                name = me.name,
                email = me.email,
                major = me.department,
                role = me.role,
                university = "Universidad de los Andes",
                language = me.language,
                isDarkMode = me.isDarkMode,
            )
            _currentUser
        } catch (e: Exception) {
            // Relanzar STAFF_ROLE para que AuthViewModel lo maneje
            if (e.message == "STAFF_ROLE") throw e
            // Credenciales incorrectas — retornar null
            if (e is HttpException && e.code() == 401) return null
            // Isabella — Sprint 3: EVC — relanzar errores de red para que AuthViewModel
            // muestre el mensaje correcto en vez de "credenciales incorrectas"
            if (isNetworkException(e)) {
                android.util.Log.e("GoatlyNet", "LOGIN: network error, rethrowing: ${e::class.simpleName}: ${e.message}")
                throw e
            }
            android.util.Log.e("GoatlyNet", "LOGIN: FAILED: ${e::class.simpleName}: ${e.message}", e)
            null
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
            val me = response.user
            _currentUser = UserModel(
                name = me.name,
                email = me.email,
                major = me.department,
                role = me.role,
                university = "Universidad de los Andes",
                language = me.language,
                isDarkMode = me.isDarkMode,
            )
            _currentUser
        } catch (e: Exception) {
            // Isabella — Sprint 4: EVC — relanzar errores de red para que AuthViewModel
            // muestre el mensaje correcto en registro sin internet
            if (isNetworkException(e)) {
                android.util.Log.e("GoatlyNet", "REGISTER: network error, rethrowing: ${e::class.simpleName}: ${e.message}")
                throw e
            }
            android.util.Log.e("GoatlyNet", "REGISTER: FAILED: ${e::class.simpleName}: ${e.message}", e)
            null
        }
    }

    private fun isNetworkException(e: Exception): Boolean {
        return e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.net.SocketTimeoutException ||
                e is java.io.IOException ||
                e.cause is java.net.UnknownHostException ||
                e.cause is java.net.ConnectException ||
                e.cause is java.io.IOException ||
                e.message?.contains("Unable to resolve host") == true ||
                e.message?.contains("Failed to connect") == true ||
                e.message?.contains("ENETUNREACH") == true ||
                e.message?.contains("timeout") == true
    }

    override fun logout() {
        _currentUser = null
        TokenManager.clear()
    }

    override fun currentUser(): UserModel? = _currentUser
}