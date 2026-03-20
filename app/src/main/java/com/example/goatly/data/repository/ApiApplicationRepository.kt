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
            api.getMyApplications("Bearer $token").map { resp ->
                ApplicationModel(
                    id = resp.id.toString(),
                    applicantName = resp.studentName,
                    applicantInitials = resp.studentName.split(" ").take(2)
                        .joinToString("") { it.first().uppercase() },
                    offerId = resp.offerId.toString(),
                    offerTitle = "Oferta #${resp.offerId}",
                    createdAt = Date(),
                    status = when (resp.status) {
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