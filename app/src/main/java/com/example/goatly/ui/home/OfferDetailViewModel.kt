package com.example.goatly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.ApplyRequest
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import com.example.goatly.data.repository.ApiApplicationRepository
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import android.content.Context
import com.example.goatly.data.network.LocationManager
import retrofit2.HttpException

class OfferDetailViewModel(
    private val offerRepository: ApiOfferRepository = RepositoryProvider.offerRepository,
    private val applicationRepository: ApiApplicationRepository = RepositoryProvider.applicationRepository
) : ViewModel() {

    data class OfferDetailUiState(
        val id: String = "",
        val title: String = "",
        val category: String = "",
        val valueCop: String = "",
        val dateTime: String = "",
        val durationHours: String = "",
        val location: String = "",
        val hasApplied: Boolean = false,
        val found: Boolean = false,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val isOnSite: Boolean = false,
        val distanceText: String? = null,
        val userLatitude: Double? = null,
        val userLongitude: Double? = null
    )

    private val _state = MutableStateFlow(OfferDetailUiState())
    val state: StateFlow<OfferDetailUiState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load(offerId: String, context: Context) {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val offer = offerRepository.getAllSuspend().find { it.id == offerId } ?: return@launch

            val offerLat = offer.latitude ?: 4.6015
            val offerLng = offer.longitude ?: -74.0657

            // Verificar si ya aplicó consultando el backend
            val alreadyApplied = try {
                applicationRepository.getAllSuspend().any { it.offerId == offerId }
            } catch (e: Exception) {
                false
            }

            _state.value = OfferDetailUiState(
                id = offer.id,
                title = offer.title,
                category = offer.category,
                valueCop = "\$${offer.valueCop} COP",
                dateTime = fmt.format(offer.dateTime),
                durationHours = "${offer.durationHours} horas",
                location = if (offer.isOnSite) "Presencial" else "Remoto",
                hasApplied = alreadyApplied,
                found = true,
                latitude = offerLat,
                longitude = offerLng,
                isOnSite = offer.isOnSite,
                distanceText = null
            )

            if (offer.isOnSite) {
                LocationManager.getCurrentLocation(context) { userLat, userLng ->
                    val meters = LocationManager.distanceInMeters(userLat, userLng, offerLat, offerLng)
                    _state.value = _state.value.copy(
                        distanceText = LocationManager.formatDistance(meters),
                        userLatitude = userLat,
                        userLongitude = userLng
                    )
                }
            }
        }
    }

    fun apply(offerId: String, applicantName: String) {
        viewModelScope.launch {
            val offerIdInt = offerId.toIntOrNull() ?: return@launch
            val success = applicationRepository.apply(offerIdInt)
            if (success) {
                _state.value = _state.value.copy(hasApplied = true)
            }
        }
    }

    fun applyWithDetails(
        offerId: String,
        applicantName: String,
        career: String,
        semester: Int,
        gpa: Float,
        availability: String,
        motivationLetter: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val offerIdInt = offerId.toIntOrNull() ?: run {
                    _error.value = "ID de oferta inválido"
                    _isLoading.value = false
                    return@launch
                }

                val token = TokenManager.getAccessToken() ?: run {
                    _error.value = "Sesión expirada"
                    _isLoading.value = false
                    return@launch
                }

                val request = ApplyRequest(
                    offerId = offerIdInt,
                    offerTitle = _state.value.title,
                    applicantName = applicantName,
                    career = career,
                    semester = semester,
                    gpa = gpa,
                    availability = availability,
                    motivationLetter = motivationLetter
                )

                RetrofitClient.api.applyToOffer("Bearer $token", request)
                _state.value = _state.value.copy(hasApplied = true)
                onSuccess()
            } catch (e: HttpException) {
                _error.value = when (e.code()) {
                    409 -> "Ya has aplicado a esta oferta"
                    else -> "Error al aplicar: ${e.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}