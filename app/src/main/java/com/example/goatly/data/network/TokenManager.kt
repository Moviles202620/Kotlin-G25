package com.example.goatly.data.network

object TokenManager {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    fun getAccessToken(): String? = accessToken
    fun getRefreshToken(): String? = refreshToken

    fun clear() {
        accessToken = null
        refreshToken = null
    }

    fun isLoggedIn(): Boolean = accessToken != null
}