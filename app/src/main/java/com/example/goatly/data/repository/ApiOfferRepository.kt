package com.example.goatly.data.repository

import com.example.goatly.data.model.OfferModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.TokenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiOfferRepository(private val api: ApiService) : OfferRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun getAll(): List<OfferModel> {
        throw UnsupportedOperationException("Use getAllSuspend instead")
    }

    override fun add(offer: OfferModel): List<OfferModel> {
        throw UnsupportedOperationException("Not supported from student app")
    }

    suspend fun getAllSuspend(): List<OfferModel> {
        return try {
            api.getAllOffers().map { offer ->
                OfferModel(
                    id = offer.id.toString(),
                    title = offer.title,
                    category = offer.category ?: "General",
                    valueCop = offer.valueCop,
                    dateTime = try { dateFormat.parse(offer.dateTime) ?: Date() } catch (e: Exception) { Date() },
                    durationHours = offer.durationHours,
                    isOnSite = offer.isOnSite,
                    latitude = offer.latitude,
                    longitude = offer.longitude
                )
            }
        } catch (e: Exception) {
            MockOfferRepository().getAll()
        }
    }
}