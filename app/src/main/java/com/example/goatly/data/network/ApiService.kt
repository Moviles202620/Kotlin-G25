package com.example.goatly.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

// ── DTOs ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class LoginRequest(val email: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginUserData(
    val id: Int,
    val name: String,
    val email: String,
    val department: String,
    val role: String,
    val language: String,
    @field:Json(name = "is_dark_mode") val isDarkMode: Boolean
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "refresh_token") val refreshToken: String,
    val user: LoginUserData
)

@JsonClass(generateAdapter = true)
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val department: String,
    val role: String,
    val language: String,
    @field:Json(name = "is_dark_mode") val isDarkMode: Boolean
)

@JsonClass(generateAdapter = true)
data class OfferResponse(
    val id: Int,
    @field:Json(name = "staff_id") val staffId: Int,
    val title: String,
    val description: String,
    val category: String?,
    @field:Json(name = "value_cop") val valueCop: Int,
    @field:Json(name = "duration_hours") val durationHours: Int,
    @field:Json(name = "is_on_site") val isOnSite: Boolean,
    @field:Json(name = "date_time") val dateTime: String,
    val latitude: Double?,
    val longitude: Double?
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val department: String,
    val role: String = "student"
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val name: String,
    val department: String,
    val language: String,
    @field:Json(name = "is_dark_mode") val isDarkMode: Boolean
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @field:Json(name = "current_password") val currentPassword: String,
    @field:Json(name = "new_password") val newPassword: String,
    @field:Json(name = "confirm_password") val confirmPassword: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordResponse(val detail: String)

@JsonClass(generateAdapter = true)
data class OfferSummaryDto(
    val id: Int,
    val title: String,
    @field:Json(name = "value_cop") val valueCop: Int,
    @field:Json(name = "duration_hours") val durationHours: Int,
    @field:Json(name = "date_time") val dateTime: String,
    @field:Json(name = "is_on_site") val isOnSite: Boolean
)

@JsonClass(generateAdapter = true)
data class MyApplicationItemDto(
    val id: Int,
    @field:Json(name = "offer_id") val offerId: Int,
    val status: String,
    @field:Json(name = "created_at") val createdAt: String,
    val offer: OfferSummaryDto
)

@JsonClass(generateAdapter = true)
data class ApplicationStatsDto(
    val total: Int,
    val pending: Int,
    val accepted: Int,
    val rejected: Int
)

@JsonClass(generateAdapter = true)
data class MyApplicationsResponseDto(
    val applications: List<MyApplicationItemDto>,
    val stats: ApplicationStatsDto
)

// ── Endpoints ─────────────────────────────────────────────────────────────────

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): UserResponse

    @GET("offers/my")
    suspend fun getOffers(@Header("Authorization") token: String): List<OfferResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @PUT("users/me")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): UserResponse

    @PUT("users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): ChangePasswordResponse

    @GET("applications/my")
    suspend fun getMyApplications(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null
    ): MyApplicationsResponseDto
}
