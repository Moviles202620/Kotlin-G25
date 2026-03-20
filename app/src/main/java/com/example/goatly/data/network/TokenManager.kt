package com.example.goatly.data.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "goatly_prefs"
    private const val KEY_ACCESS  = "access_token"
    private const val KEY_REFRESH = "refresh_token"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(access: String, refresh: String) {
        prefs?.edit()?.putString(KEY_ACCESS, access)?.putString(KEY_REFRESH, refresh)?.apply()
    }

    fun getAccessToken(): String? = prefs?.getString(KEY_ACCESS, null)
    fun getRefreshToken(): String? = prefs?.getString(KEY_REFRESH, null)

    fun clear() {
        prefs?.edit()?.remove(KEY_ACCESS)?.remove(KEY_REFRESH)?.apply()
    }

    fun isLoggedIn(): Boolean = getAccessToken() != null
}