package com.example.goatly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.MockApplicationRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import com.example.goatly.data.network.LocationManager

class OfferDetailViewModel(
    private val offerRepository: ApiOfferRepository = RepositoryProvider.offerRepository,
    private val applicationRepository: MockApplicationRepository = RepositoryProvider.applicationRepository as MockApplicationRepository
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

    fun load(offerId: String, context: Context) {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val offer = offerRepository.getAllSuspend().find { it.id == offerId } ?: return@launch

            val offerLat = offer.latitude ?: 4.6015
            val offerLng = offer.longitude ?: -74.0657

            _state.value = OfferDetailUiState(
                id = offer.id,
                title = offer.title,
                category = offer.category,
                valueCop = "\$${offer.valueCop} COP",
                dateTime = fmt.format(offer.dateTime),
                durationHours = "${offer.durationHours} horas",
                location = if (offer.isOnSite) "Presencial" else "Remoto",
                hasApplied = applicationRepository.hasApplied(offerId),
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
        val app = ApplicationModel(
            id = "ap_${System.currentTimeMillis()}",
            applicantName = applicantName,
            applicantInitials = applicantName.split(" ").take(2).joinToString("") { it.first().uppercase() },
            offerId = offerId,
            offerTitle = _state.value.title,
            createdAt = Date(),
            status = ApplicationStatus.PENDING
        )
        applicationRepository.addApplication(app)
        _state.value = _state.value.copy(hasApplied = true)
    }
}