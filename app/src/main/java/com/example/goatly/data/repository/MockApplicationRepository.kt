package com.example.goatly.data.repository

import com.example.goatly.data.mock.MockDataSource
import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus

class MockApplicationRepository : ApplicationRepository {

    private val _applications: MutableList<ApplicationModel> =
        MockDataSource.getMyApplications().toMutableList()

    override fun getAll(): List<ApplicationModel> = _applications.toList()

    override fun updateStatus(appId: String, newStatus: ApplicationStatus): List<ApplicationModel> {
        val idx = _applications.indexOfFirst { it.id == appId }
        if (idx != -1) _applications[idx] = _applications[idx].copy(status = newStatus)
        return _applications.toList()
    }

    fun addApplication(app: ApplicationModel): List<ApplicationModel> {
        if (_applications.none { it.offerId == app.offerId }) {
            _applications.add(0, app)
        }
        return _applications.toList()
    }

    fun hasApplied(offerId: String): Boolean =
        _applications.any { it.offerId == offerId }
}
