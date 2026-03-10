package com.example.goatly.data.network

import com.squareup.moshi.Json
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ── DTOs ──────────────────────────────────────────────────────────────────────

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val department: String,
    val language: String,
    @Json(name = "is_dark_mode") val isDarkMode: Boolean
)

data class OfferResponse(
    val id: Int,
    @Json(name = "staff_id") val staffId: Int,
    val title: String,
    val description: String,
    val category: String?,
    @Json(name = "value_cop") val valueCop: Int,
    @Json(name = "duration_hours") val durationHours: Int,
    @Json(name = "is_on_site") val isOnSite: Boolean,
    @Json(name = "date_time") val dateTime: String,
    val latitude: Double?,
    val longitude: Double?
)

// ── Endpoints ─────────────────────────────────────────────────────────────────

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserResponse

    @GET("offers/my")
    suspend fun getOffers(@Header("Authorization") token: String): List<OfferResponse>
}