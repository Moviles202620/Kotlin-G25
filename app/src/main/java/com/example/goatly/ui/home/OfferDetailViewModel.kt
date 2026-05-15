package com.example.goatly.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.network.ApplyRequest
import com.example.goatly.data.network.CalendarSyncManager
import com.example.goatly.data.network.RetrofitClient
import com.example.goatly.data.network.TokenManager
import com.example.goatly.data.network.LocationManager
import com.example.goatly.data.repository.ApiApplicationRepository
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import com.google.android.gms.location.LocationCallback
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.goatly.data.network.AppliedOffersCache

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
        val userLongitude: Double? = null,
        // Isabella — Sprint 3: Calendar Sync — state fields
        val isAddedToCalendar: Boolean = false,
        val isCalendarPending: Boolean = false,
        val offerDateMillis: Long = 0L,
        val offerDurationHours: Int = 0,
        val offerLocationText: String = "",
        // Isabella — Sprint 3: BQ12 — Average GPA state fields
        val avgGpa: Float? = null,
        val totalApplicants: Int = 0
    )

    private val _state = MutableStateFlow(OfferDetailUiState())
    val state: StateFlow<OfferDetailUiState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
            } catch (_: Exception) {
                // Si no hay internet, consultar cache local
                AppliedOffersCache.hasApplied(context, offerId)
            }


            // Isabella — Sprint 3: Calendar Sync — load persisted calendar state
            val isSynced = CalendarSyncManager.isOfferSynced(context, offer.id)
            val isPending = CalendarSyncManager.isOfferPending(context, offer.id)
            Log.d("GOATLY_CAL", "load — offer ${offer.id} calendar state: synced=$isSynced pending=$isPending")

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
                distanceText = null,
                offerDateMillis = offer.dateTime.time,
                offerDurationHours = offer.durationHours,
                offerLocationText = if (offer.isOnSite) "Presencial - Universidad de los Andes" else "Remoto",
                isAddedToCalendar = isSynced,
                isCalendarPending = isPending
            )

            if (offer.isOnSite) {
                startTrackingLocation(context)
            }

            // Isabella — Sprint 3: BQ12 — load GPA analytics for this offer
            loadGpaForOffer(offerId)
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

    fun apply(offerId: String, userName: String, userCareer: String) {
        viewModelScope.launch {
            val offerIdInt = offerId.toIntOrNull() ?: return@launch
            val success = applicationRepository.apply(
                offerId = offerIdInt,
                offerTitle = _state.value.title,
                applicantName = userName,
                career = userCareer,
                motivationLetter = "Soy $userName y me interesa esta oferta para fortalecer mi experiencia en $userCareer y aportar a la Universidad de los Andes."
            )
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

    // ============================================================
    // Isabella — Sprint 3: Multi-threading — 3 Dispatchers en paralelo
    // ============================================================
    // Coroutine 1: async(Dispatchers.IO) → POST backend (network)
    // Coroutine 2: async(Dispatchers.IO) → CalendarContract insert (I/O)
    // Coroutine 3: withContext(Dispatchers.Main) → UI state update
    // Coroutines 1 and 2 run in PARALLEL — neither blocks the other
    fun applyAndSyncCalendar(
        context: Context,
        offerId: String,
        applicantName: String,
        career: String,
        semester: Int,
        gpa: Float,
        availability: String,
        motivationLetter: String,
        addToCalendar: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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

            // Isabella — Sprint 3: Multi-threading — Coroutine 1: Dispatchers.IO (network)
            // Runs in parallel with calendarJob — does not block the calendar insert
            Log.d("GOATLY_MT", "applyAndSyncCalendar — launching backend POST on Dispatchers.IO")
            val backendJob = async(Dispatchers.IO) {
                try {
                    RetrofitClient.api.applyToOffer("Bearer $token", request)
                    Log.d("GOATLY_MT", "applyAndSyncCalendar — backend POST success on Dispatchers.IO")
                    true
                } catch (e: Exception) {
                    Log.e("GOATLY_MT", "applyAndSyncCalendar — backend POST failed on Dispatchers.IO: ${e.message}")
                    false
                }
            }

            // Isabella — Sprint 3: Multi-threading — Coroutine 2: Dispatchers.IO (calendar I/O)
            // Runs in parallel with backendJob — calendar insert does not wait for backend
            Log.d("GOATLY_MT", "applyAndSyncCalendar — launching calendar insert on Dispatchers.IO (parallel)")
            val calendarJob = async(Dispatchers.IO) {
                try {
                    if (addToCalendar) {
                        val result = CalendarSyncManager.addOfferToCalendar(
                            context = context,
                            offerId = offerId,
                            title = _state.value.title,
                            dateTimeMillis = _state.value.offerDateMillis,
                            durationHours = _state.value.offerDurationHours,
                            location = _state.value.offerLocationText
                        )
                        Log.d("GOATLY_MT", "applyAndSyncCalendar — calendar insert on Dispatchers.IO result=$result")
                        result
                    } else {
                        Log.d("GOATLY_MT", "applyAndSyncCalendar — calendar insert skipped (user did not toggle)")
                        false
                    }
                } catch (e: Exception) {
                    Log.e("GOATLY_MT", "applyAndSyncCalendar — calendar insert failed on Dispatchers.IO: ${e.message}")
                    false
                }
            }

            val backendSuccess = backendJob.await()
            val calendarSuccess = calendarJob.await()

            // Isabella — Sprint 3: Multi-threading — Coroutine 3: Dispatchers.Main (UI update)
            // Only the main thread can update StateFlow observed by Compose
            Log.d("GOATLY_MT", "applyAndSyncCalendar — updating UI on Dispatchers.Main — backend=$backendSuccess calendar=$calendarSuccess")
            withContext(Dispatchers.Main) {
                if (backendSuccess) {
                    _state.value = _state.value.copy(
                        hasApplied = true,
                        isAddedToCalendar = addToCalendar && calendarSuccess,
                        isCalendarPending = addToCalendar && !calendarSuccess
                    )

                    // Guardar localmente para funcionar offline
                    AppliedOffersCache.markAsApplied(context, offerId)
                    _state.value = _state.value.copy(
                        hasApplied = true,
                        isAddedToCalendar = addToCalendar && calendarSuccess,
                        isCalendarPending = addToCalendar && !calendarSuccess
                    )

                    onSuccess()
                } else {
                    // Isabella — Sprint 3: Eventual Connectivity — apply sin crash offline
                    // Close dialog via onSuccess() then show friendly Snackbar — no crash, no freeze
                    onSuccess()
                    _error.value = "No hay conexión a internet. Intenta de nuevo cuando tengas red."
                }
            }

            _isLoading.value = false
        }
    }

    // ============================================================
    // Isabella — Sprint 3: BQ12 — Average GPA of applicants
    // ============================================================
    // Type 2: result is displayed directly in OfferDetailScreen as COMPETENCIA card.
    // Runs on Dispatchers.IO to avoid blocking the main thread during network call,
    // then switches to Dispatchers.Main to update StateFlow safely.
    private fun loadGpaForOffer(offerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("GOATLY_BQ", "loadGpaForOffer — fetching GPA data on Dispatchers.IO for offerId=$offerId")
            try {
                val offerIdInt = offerId.toIntOrNull() ?: return@launch
                val gpaData = RetrofitClient.api.getGpaByOffer()
                val offerGpa = gpaData.find { it.offerId == offerIdInt }
                Log.d("GOATLY_BQ", "loadGpaForOffer — result: avgGpa=${offerGpa?.averageGpa} totalApplicants=${offerGpa?.totalApplicants}")
                withContext(Dispatchers.Main) {
                    Log.d("GOATLY_BQ", "loadGpaForOffer — updating UI on Dispatchers.Main")
                    _state.value = _state.value.copy(
                        avgGpa = offerGpa?.averageGpa,
                        totalApplicants = offerGpa?.totalApplicants ?: 0
                    )
                }
            } catch (e: Exception) {
                Log.e("GOATLY_BQ", "loadGpaForOffer — failed: ${e.message}")
            }
        }
    }
}