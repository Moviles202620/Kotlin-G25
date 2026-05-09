package com.example.goatly.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.model.OfferModel
import com.example.goatly.data.network.LocationManager
import com.example.goatly.data.repository.ApiApplicationRepository
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val offerRepository: ApiOfferRepository = RepositoryProvider.offerRepository,
    private val applicationRepository: ApiApplicationRepository = RepositoryProvider.applicationRepository
) : ViewModel() {

    data class OfferUiItem(
        val id: String,
        val title: String,
        val category: String,
        val categoryAndValue: String,
        val whenAndDuration: String,
        val location: String,
        val isOnSite: Boolean,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val distanceMeters: Double? = null,
        val distanceText: String? = null,
        // Isabella — Sprint 4: fecha para filtrar ofertas pasadas
        val dateTime: Date = Date(),
        // Isabella — Sprint 4: relación visual con aplicaciones
        val hasApplied: Boolean = false
    )

    enum class SortOrder { CLOSEST_FIRST, FARTHEST_FIRST }

    data class DistanceFilter(
        val showRemote: Boolean = true,
        val maxDistanceMeters: Double? = null,
        val sortOrder: SortOrder = SortOrder.CLOSEST_FIRST
    )

    private val _allOffers = MutableStateFlow<List<OfferUiItem>>(emptyList())

    private val _offers = MutableStateFlow<List<OfferUiItem>>(emptyList())
    val offers: StateFlow<List<OfferUiItem>> = _offers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _distanceFilter = MutableStateFlow(DistanceFilter())
    val distanceFilter: StateFlow<DistanceFilter> = _distanceFilter.asStateFlow()

    private val _userLat = MutableStateFlow<Double?>(null)
    private val _userLng = MutableStateFlow<Double?>(null)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            val now = Date()

            try {
                val all = offerRepository.getAllSuspend()

                // Isabella — Sprint 4: relación visual con aplicaciones
                // Fetch applied offer IDs to show badge in OfferCard
                val appliedOfferIds = try {
                    applicationRepository.getAllSuspend().map { it.offerId }.toSet()
                } catch (e: Exception) {
                    Log.w("GOATLY_HOME", "Could not load applications: ${e.message}")
                    emptySet()
                }

                val mapped = all.map { o ->
                    val dist = if (o.isOnSite && _userLat.value != null && _userLng.value != null &&
                        o.latitude != null && o.longitude != null) {
                        LocationManager.distanceInMeters(_userLat.value!!, _userLng.value!!, o.latitude, o.longitude)
                    } else null

                    OfferUiItem(
                        id = o.id,
                        title = o.title,
                        category = o.category,
                        categoryAndValue = "${o.category} • \$${o.valueCop} COP",
                        whenAndDuration = "${fmt.format(o.dateTime)} • ${o.durationHours}h",
                        location = if (o.isOnSite) "Presencial" else "Remoto",
                        isOnSite = o.isOnSite,
                        latitude = o.latitude,
                        longitude = o.longitude,
                        distanceMeters = dist,
                        distanceText = dist?.let { LocationManager.formatDistance(it) },
                        dateTime = o.dateTime,
                        hasApplied = o.id in appliedOfferIds
                    )
                }

                _allOffers.value = mapped
                _categories.value = mapped.map { it.category }.distinct().sorted()
                applyFilters()
                Log.d("GOATLY_HOME", "Loaded ${mapped.size} offers, ${appliedOfferIds.size} already applied")

            } catch (e: Exception) {
                Log.e("GOATLY_HOME", "Error loading offers: ${e.message}")
                if (e is java.net.UnknownHostException || e is java.net.ConnectException || e is java.net.SocketTimeoutException) {
                    _error.value = "Sin conexión a internet. Mostrando ofertas guardadas."
                } else {
                    _error.value = "Error al cargar ofertas. Intenta de nuevo."
                }
            }

            _isLoading.value = false
        }
    }

    fun selectCategory(cat: String?) {
        _selectedCategory.value = cat
        applyFilters()
    }

    fun applyDistanceFilter(filter: DistanceFilter) {
        _distanceFilter.value = filter
        applyFilters()
    }

    fun loadUserLocationAndFilter(context: Context) {
        LocationManager.getCurrentLocation(context) { lat, lng ->
            _userLat.value = lat
            _userLng.value = lng
            refresh()
        }
    }

    private fun applyFilters() {
        val cat = _selectedCategory.value
        val filter = _distanceFilter.value
        val now = Date()

        var result = _allOffers.value

        // Isabella — Sprint 4: filtrar ofertas cuya fecha ya pasó
        result = result.filter { it.dateTime.after(now) }
        Log.d("GOATLY_HOME", "After date filter: ${result.size} upcoming offers")

        if (cat != null) result = result.filter { it.category == cat }
        if (!filter.showRemote) result = result.filter { it.isOnSite }

        if (filter.maxDistanceMeters != null) {
            result = result.filter { offer ->
                if (offer.isOnSite) {
                    offer.distanceMeters?.let { it <= filter.maxDistanceMeters } ?: true
                } else {
                    filter.showRemote
                }
            }
        }

        result = result.sortedWith(compareBy {
            when {
                !it.isOnSite -> if (filter.sortOrder == SortOrder.CLOSEST_FIRST) Double.MAX_VALUE else Double.MIN_VALUE
                it.distanceMeters == null -> if (filter.sortOrder == SortOrder.CLOSEST_FIRST) Double.MAX_VALUE else Double.MIN_VALUE
                filter.sortOrder == SortOrder.CLOSEST_FIRST -> it.distanceMeters
                else -> -it.distanceMeters
            }
        })

        _offers.value = result
    }
}