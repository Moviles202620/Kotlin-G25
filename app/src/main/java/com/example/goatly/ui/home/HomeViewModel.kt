package com.example.goatly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.repository.ApiOfferRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel(
    private val offerRepository: ApiOfferRepository = RepositoryProvider.offerRepository
) : ViewModel() {

    data class OfferUiItem(
        val id: String,
        val title: String,
        val category: String,
        val categoryAndValue: String,
        val whenAndDuration: String,
        val location: String,
        val isOnSite: Boolean
    )

    private val _offers = MutableStateFlow<List<OfferUiItem>>(emptyList())
    val offers: StateFlow<List<OfferUiItem>> = _offers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val categories = listOf("Académico", "Administrativo", "Eventos", "Logística")

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            val all = offerRepository.getAllSuspend()
            _offers.value = all
                .filter { o -> _selectedCategory.value == null || o.category == _selectedCategory.value }
                .map { o ->
                    OfferUiItem(
                        id = o.id,
                        title = o.title,
                        category = o.category,
                        categoryAndValue = "${o.category} • \$${o.valueCop} COP",
                        whenAndDuration = "${fmt.format(o.dateTime)} • ${o.durationHours}h",
                        location = if (o.isOnSite) "Presencial" else "Remoto",
                        isOnSite = o.isOnSite
                    )
                }
            _isLoading.value = false
        }
    }

    fun selectCategory(cat: String?) {
        _selectedCategory.value = cat
        refresh()
    }
}