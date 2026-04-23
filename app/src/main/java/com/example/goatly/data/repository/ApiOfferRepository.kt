package com.example.goatly.data.repository

import com.example.goatly.data.model.OfferModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.OfferCache
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

        // Sprint 3: In-memory Cache — cache hit check
        // If valid cached data exists, return it immediately without a network call
        if (OfferCache.isValid()) {
            android.util.Log.d("GOATLY", "Serving offers from in-memory cache")
            return OfferCache.get()
        }
        // Sprint 3: In-memory Cache — END cache hit check

        return try {
            val offers = api.getAllOffers().map { offer ->
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

            // Sprint 3: In-memory Cache — store fresh data
            OfferCache.set(offers)
            android.util.Log.d("GOATLY", "Offers fetched from network and cached (${offers.size} items)")
            // Sprint 3: In-memory Cache — END store

            offers
        } catch (e: Exception) {
            android.util.Log.e("GOATLY", "Network error loading offers: ${e.message}", e)

            // Sprint 3: In-memory Cache — stale cache fallback
            // If network fails but we have any cached data (even expired), serve it
            if (OfferCache.get().isNotEmpty()) {
                android.util.Log.d("GOATLY", "Network failed — serving stale cache as fallback")
                OfferCache.get()
            } else {
                // Sprint 3: In-memory Cache — last resort mock fallback
                MockOfferRepository().getAll()
            }
            // Sprint 3: In-memory Cache — END fallback
        }
    }
}