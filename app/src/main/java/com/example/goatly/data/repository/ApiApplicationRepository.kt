package com.example.goatly.data.repository

import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.ApplyRequest
import com.example.goatly.data.network.TokenManager
import java.util.Date

class ApiApplicationRepository(private val api: ApiService) : ApplicationRepository {

    override fun getAll(): List<ApplicationModel> {
        throw UnsupportedOperationException("Use getAllSuspend instead")
    }

    override fun updateStatus(appId: String, newStatus: ApplicationStatus): List<ApplicationModel> {
        throw UnsupportedOperationException("Not supported from student app")
    }

    suspend fun getAllSuspend(): List<ApplicationModel> {
        val token = TokenManager.getAccessToken() ?: return emptyList()
        return try {
            val response = api.getMyApplications("Bearer $token")
            response.applications.map { item ->
                ApplicationModel(
                    id = item.id.toString(),
                    applicantName = "",
                    applicantInitials = "",
                    offerId = item.offerId.toString(),
                    offerTitle = item.offer.title,
                    createdAt = Date(),
                    status = when (item.status) {
                        "accepted" -> ApplicationStatus.ACCEPTED
                        "rejected" -> ApplicationStatus.REJECTED
                        else       -> ApplicationStatus.PENDING
                    }
                )
            }
        } catch (e: Exception) {
            MockApplicationRepository().getAll()
        }
    }

    suspend fun apply(offerId: Int): Boolean {
        val token = TokenManager.getAccessToken() ?: return false
        return try {
            api.applyToOffer("Bearer $token", ApplyRequest(offerId))
            true
        } catch (e: Exception) {
            false
        }
    }
}