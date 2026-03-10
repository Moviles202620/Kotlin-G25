package com.example.goatly.data.mock

import com.example.goatly.data.model.ApplicationModel
import com.example.goatly.data.model.ApplicationStatus
import com.example.goatly.data.model.OfferModel
import java.util.Date
import java.util.concurrent.TimeUnit

object MockDataSource {

    fun getOffers(): List<OfferModel> {
        val now = Date()
        return listOf(
            OfferModel(id = "of1", title = "Monitoría de Cálculo", category = "Académico", valueCop = 60000, dateTime = Date(now.time + TimeUnit.DAYS.toMillis(2)), durationHours = 2, isOnSite = true),
            OfferModel(id = "of2", title = "Asistente de Biblioteca", category = "Administrativo", valueCop = 50000, dateTime = Date(now.time + TimeUnit.DAYS.toMillis(4)), durationHours = 3, isOnSite = false),
            OfferModel(id = "of3", title = "Apoyo en Eventos Institucionales", category = "Eventos", valueCop = 45000, dateTime = Date(now.time + TimeUnit.DAYS.toMillis(7)), durationHours = 4, isOnSite = true),
            OfferModel(id = "of4", title = "Diseño de Material Didáctico", category = "Académico", valueCop = 70000, dateTime = Date(now.time + TimeUnit.DAYS.toMillis(5)), durationHours = 3, isOnSite = false),
            OfferModel(id = "of5", title = "Logística de Laboratorio", category = "Logística", valueCop = 55000, dateTime = Date(now.time + TimeUnit.DAYS.toMillis(3)), durationHours = 2, isOnSite = true)
        )
    }

    fun getMyApplications(): List<ApplicationModel> {
        val now = Date()
        return listOf(
            ApplicationModel(id = "ap1", applicantName = "Estudiante Uniandes", applicantInitials = "EU", offerId = "of1", offerTitle = "Monitoría de Cálculo", createdAt = Date(now.time - TimeUnit.HOURS.toMillis(3)), status = ApplicationStatus.PENDING),
            ApplicationModel(id = "ap2", applicantName = "Estudiante Uniandes", applicantInitials = "EU", offerId = "of2", offerTitle = "Asistente de Biblioteca", createdAt = Date(now.time - TimeUnit.DAYS.toMillis(1)), status = ApplicationStatus.ACCEPTED),
            ApplicationModel(id = "ap3", applicantName = "Estudiante Uniandes", applicantInitials = "EU", offerId = "of3", offerTitle = "Apoyo en Eventos Institucionales", createdAt = Date(now.time - TimeUnit.DAYS.toMillis(3)), status = ApplicationStatus.REJECTED)
        )
    }
}
