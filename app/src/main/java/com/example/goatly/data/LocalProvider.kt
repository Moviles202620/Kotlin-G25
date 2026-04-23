package com.example.goatly.data

import android.content.Context
import com.example.goatly.data.local.cache.ApplicationsCache
import com.example.goatly.data.local.datastore.UserPreferencesDataStore
import com.example.goatly.data.local.db.AppDatabase
import com.example.goatly.data.network.RetrofitClient

object LocalProvider {
    lateinit var db: AppDatabase
    lateinit var userPrefs: UserPreferencesDataStore
    val appsCache = ApplicationsCache.instance

    fun init(context: Context) {
        db = AppDatabase.getInstance(context)
        userPrefs = UserPreferencesDataStore.getInstance(context)
        RetrofitClient.init(context)
    }
}
