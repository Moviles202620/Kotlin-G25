package com.example.goatly.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.goatly.data.model.UserModel

object TokenManager {
    private const val PREFS_NAME = "goatly_prefs"
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    // Sprint 3: Eventual Connectivity — cached user profile keys
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_MAJOR = "user_major"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_LANGUAGE = "user_language"
    private const val KEY_USER_DARK_MODE = "user_dark_mode"
    // Sprint 3: Eventual Connectivity — END

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

    // Sprint 3: Eventual Connectivity — save user profile locally for offline restore
    fun saveUserProfile(user: UserModel) {
        prefs?.edit()
            ?.putString(KEY_USER_NAME, user.name)
            ?.putString(KEY_USER_EMAIL, user.email)
            ?.putString(KEY_USER_MAJOR, user.major)
            ?.putString(KEY_USER_ROLE, user.role)
            ?.putString(KEY_USER_LANGUAGE, user.language)
            ?.putBoolean(KEY_USER_DARK_MODE, user.isDarkMode)
            ?.apply()
    }

    fun getUserProfile(): UserModel? {
        val name = prefs?.getString(KEY_USER_NAME, null) ?: return null
        val email = prefs?.getString(KEY_USER_EMAIL, null) ?: return null
        return UserModel(
            name = name,
            email = email,
            major = prefs?.getString(KEY_USER_MAJOR, "") ?: "",
            role = prefs?.getString(KEY_USER_ROLE, "student") ?: "student",
            university = "Universidad de los Andes",
            language = prefs?.getString(KEY_USER_LANGUAGE, "es") ?: "es",
            isDarkMode = prefs?.getBoolean(KEY_USER_DARK_MODE, false) ?: false
        )
    }

    fun clearUserProfile() {
        prefs?.edit()
            ?.remove(KEY_USER_NAME)
            ?.remove(KEY_USER_EMAIL)
            ?.remove(KEY_USER_MAJOR)
            ?.remove(KEY_USER_ROLE)
            ?.remove(KEY_USER_LANGUAGE)
            ?.remove(KEY_USER_DARK_MODE)
            ?.apply()
    }
    // Sprint 3: Eventual Connectivity — END
}