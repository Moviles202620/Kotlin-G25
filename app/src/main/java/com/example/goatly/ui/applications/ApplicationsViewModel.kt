package com.example.goatly.ui.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goatly.data.model.ApplicationStatus
import com.example.goatly.data.repository.ApiApplicationRepository
import com.example.goatly.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ApplicationsViewModel(
    private val applicationRepository: ApiApplicationRepository = RepositoryProvider.applicationRepository
) : ViewModel() {

    enum class StatusType { PENDING, ACCEPTED, REJECTED }

    data class ApplicationUiItem(
        val id: String,
        val offerTitle: String,
        val dateLabel: String,
        val statusLabel: String,
        val statusType: StatusType
    )

    private val _items = MutableStateFlow<List<ApplicationUiItem>>(emptyList())
    val items: StateFlow<List<ApplicationUiItem>> = _items.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val apps = applicationRepository.getAllSuspend()
            _items.value = apps.map { app ->
                ApplicationUiItem(
                    id = app.id,
                    offerTitle = app.offerTitle,
                    dateLabel = fmt.format(app.createdAt),
                    statusLabel = when (app.status) {
                        ApplicationStatus.PENDING  -> "PENDIENTE"
                        ApplicationStatus.ACCEPTED -> "ACEPTADA"
                        ApplicationStatus.REJECTED -> "RECHAZADA"
                    },
                    statusType = when (app.status) {
                        ApplicationStatus.PENDING  -> StatusType.PENDING
                        ApplicationStatus.ACCEPTED -> StatusType.ACCEPTED
                        ApplicationStatus.REJECTED -> StatusType.REJECTED
                    }
                )
            }
        }
    }
}