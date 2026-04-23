package com.example.goatly.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_DEPARTMENT = stringPreferencesKey("department")
        val KEY_EMAIL = stringPreferencesKey("email")

        @Volatile private var INSTANCE: UserPreferencesDataStore? = null

        fun getInstance(context: Context): UserPreferencesDataStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesDataStore(context.applicationContext).also { INSTANCE = it }
            }
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_LANGUAGE] ?: "es" }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_DARK_MODE] ?: false }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_NAME] ?: "" }

    val departmentFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_DEPARTMENT] ?: "" }

    suspend fun saveUserPreferences(
        name: String,
        email: String,
        department: String,
        language: String,
        isDarkMode: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_EMAIL] = email
            prefs[KEY_DEPARTMENT] = department
            prefs[KEY_LANGUAGE] = language
            prefs[KEY_DARK_MODE] = isDarkMode
        }
    }

    suspend fun hasData(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[KEY_USER_NAME]?.isNotEmpty() == true
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
