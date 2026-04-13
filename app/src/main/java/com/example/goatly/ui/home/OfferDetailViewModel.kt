package com.example.goatly.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.LocationManager
import com.example.goatly.data.repository.ApiApplicationRepository
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

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

    private var locationCallback: LocationCallback? = null
    private var offerLat: Double = 4.6015
    private var offerLng: Double = -74.0657

    fun load(offerId: String, context: Context) {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val offer = offerRepository.getAllSuspend().find { it.id == offerId } ?: return@launch

            offerLat = offer.latitude ?: 4.6015
            offerLng = offer.longitude ?: -74.0657

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
                startTrackingLocation(context)
            }
        }
    }

    private fun startTrackingLocation(context: Context) {
        locationCallback = LocationManager.startLocationUpdates(context) { userLat, userLng ->
            val meters = LocationManager.distanceInMeters(userLat, userLng, offerLat, offerLng)
            _state.value = _state.value.copy(
                distanceText = LocationManager.formatDistance(meters),
                userLatitude = userLat,
                userLongitude = userLng
            )
        }
    }

    fun stopTracking(context: Context) {
        locationCallback?.let { LocationManager.stopLocationUpdates(context, it) }
        locationCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        locationCallback = null
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
}