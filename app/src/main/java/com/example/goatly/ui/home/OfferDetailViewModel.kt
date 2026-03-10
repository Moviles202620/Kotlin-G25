package com.example.goatly.ui.home

import androidx.lifecycle.ViewModel
import com.example.goatly.data.repository.MockApplicationRepository
import com.example.goatly.data.repository.OfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OfferDetailViewModel(
    private val offerRepository: OfferRepository = RepositoryProvider.offerRepository,
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
        val found: Boolean = false
    )

    private val _state = MutableStateFlow(OfferDetailUiState())
    val state: StateFlow<OfferDetailUiState> = _state.asStateFlow()

    fun load(offerId: String) {
        val offer = offerRepository.getAll().find { it.id == offerId } ?: return
        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        _state.value = OfferDetailUiState(
            id = offer.id,
            title = offer.title,
            category = offer.category,
            valueCop = "\$${offer.valueCop} COP",
            dateTime = fmt.format(offer.dateTime),
            durationHours = "${offer.durationHours} horas",
            location = if (offer.isOnSite) "Presencial" else "Remoto",
            hasApplied = applicationRepository.hasApplied(offerId),
            found = true
        )
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
