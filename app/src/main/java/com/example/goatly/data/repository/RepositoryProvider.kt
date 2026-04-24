package com.example.goatly.data.repository

import android.content.Context
import com.example.goatly.data.network.RetrofitClient

object RepositoryProvider {
    val authRepository: ApiAuthRepository = ApiAuthRepository(RetrofitClient.api)
    val applicationRepository: ApiApplicationRepository = ApiApplicationRepository(RetrofitClient.api)

    // Sprint 3: Local Storage — offerRepository needs context for Room access
    private var _offerRepository: ApiOfferRepository? = null
    val offerRepository: ApiOfferRepository
        get() = _offerRepository ?: ApiOfferRepository(RetrofitClient.api)

    fun init(context: Context) {
        _offerRepository = ApiOfferRepository(RetrofitClient.api, context)
    }
    // Sprint 3: Local Storage — END
}