package com.example.goatly.data.repository

import android.content.Context
import com.example.goatly.data.local.GoatlyDatabase
import com.example.goatly.data.local.OfferEntity
import com.example.goatly.data.model.OfferModel
import com.example.goatly.data.network.ApiService
import com.example.goatly.data.network.OfferCache
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiOfferRepository(
    private val api: ApiService,
    private val context: Context? = null
) : OfferRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun getAll(): List<OfferModel> {
        throw UnsupportedOperationException("Use getAllSuspend instead")
    }

    override fun add(offer: OfferModel): List<OfferModel> {
        throw UnsupportedOperationException("Not supported from student app")
    }

    suspend fun getAllSuspend(): List<OfferModel> {

        // Sprint 3: In-memory Cache — Level 1: fresh cache hit
        if (OfferCache.isValid()) {
            android.util.Log.d("GOATLY", "Serving offers from in-memory cache")
            return OfferCache.get()
        }
        // Sprint 3: In-memory Cache — END Level 1

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

            // Sprint 3: In-memory Cache — Level 2: update in-memory cache
            OfferCache.set(offers)
            android.util.Log.d("GOATLY", "Offers fetched from network and cached (${offers.size} items)")
            // Sprint 3: In-memory Cache — END Level 2

            // Sprint 3: Local Storage — persist to Room database
            context?.let { ctx ->
                val db = GoatlyDatabase.getInstance(ctx)
                val entities = offers.map { offer ->
                    OfferEntity(
                        id = offer.id,
                        title = offer.title,
                        category = offer.category,
                        valueCop = offer.valueCop,
                        dateTimeMillis = offer.dateTime.time,
                        durationHours = offer.durationHours,
                        isOnSite = offer.isOnSite,
                        latitude = offer.latitude,
                        longitude = offer.longitude
                    )
                }
                db.offerDao().insertAll(entities)
                android.util.Log.d("GOATLY", "Offers persisted to Room (${entities.size} rows)")
            }
            // Sprint 3: Local Storage — END persist

            offers
        } catch (e: Exception) {
            android.util.Log.e("GOATLY", "Network error loading offers: ${e.message}", e)

            // Sprint 3: In-memory Cache — Level 3: stale in-memory fallback
            if (OfferCache.get().isNotEmpty()) {
                android.util.Log.d("GOATLY", "Network failed — serving stale in-memory cache")
                return OfferCache.get()
            }
            // Sprint 3: In-memory Cache — END Level 3

            // Sprint 3: Local Storage — Level 4: Room database fallback
            context?.let { ctx ->
                val db = GoatlyDatabase.getInstance(ctx)
                val cached = db.offerDao().getAll()
                if (cached.isNotEmpty()) {
                    android.util.Log.d("GOATLY", "Network failed — serving ${cached.size} offers from Room")
                    return cached.map { entity ->
                        OfferModel(
                            id = entity.id,
                            title = entity.title,
                            category = entity.category,
                            valueCop = entity.valueCop,
                            dateTime = Date(entity.dateTimeMillis),
                            durationHours = entity.durationHours,
                            isOnSite = entity.isOnSite,
                            latitude = entity.latitude,
                            longitude = entity.longitude
                        )
                    }
                }
            }
            // Sprint 3: Local Storage — END Level 4

            // Level 5: Mock fallback — last resort
            android.util.Log.d("GOATLY", "All fallbacks exhausted — using mock data")
            MockOfferRepository().getAll()
        }
    }
}