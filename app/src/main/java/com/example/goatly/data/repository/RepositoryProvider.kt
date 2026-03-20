package com.example.goatly.data.repository

import com.example.goatly.data.network.RetrofitClient

object RepositoryProvider {
    val authRepository: ApiAuthRepository = ApiAuthRepository(RetrofitClient.api)
    val offerRepository: ApiOfferRepository = ApiOfferRepository(RetrofitClient.api)
    val applicationRepository: ApiApplicationRepository = ApiApplicationRepository(RetrofitClient.api)
}