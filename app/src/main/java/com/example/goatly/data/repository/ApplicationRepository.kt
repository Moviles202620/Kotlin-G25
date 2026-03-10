package com.example.goatly.data.repository

import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus

/**
 * ApplicationRepository
 * Contrato para operaciones sobre ApplicationModel.
 */
interface ApplicationRepository {
    fun getAll(): List<ApplicationModel>
    fun updateStatus(appId: String, newStatus: ApplicationStatus): List<ApplicationModel>
}
