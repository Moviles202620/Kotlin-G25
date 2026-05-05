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

    // ============================================================
    // Isabella — Sprint 3: Eventual Connectivity — 4-level fallback
    // ============================================================
    suspend fun getAllSuspend(): List<OfferModel> {

        // Isabella — Sprint 3: Caching — Level 1: LRU fresh cache hit
        // If entries exist and TTL has not expired, serve directly without network call
        if (OfferCache.hasValidEntries()) {
            android.util.Log.d("GOATLY", "Serving offers from LRU cache")
            return OfferCache.getAll()
        }

        return try {
            // Isabella — Sprint 3: Eventual Connectivity — Level 2: network fetch
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

            // Isabella — Sprint 3: Caching — populate LRU after successful fetch
            OfferCache.putAll(offers)
            android.util.Log.d("GOATLY", "Offers fetched from network and stored in LRU cache (${offers.size} items)")

            // Isabella — Sprint 3: Local Storage — persist to Room after successful fetch
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

            offers
        } catch (e: Exception) {
            android.util.Log.e("GOATLY", "Network error loading offers: ${e.message}", e)

            // Isabella — Sprint 3: Caching — Level 3: stale LRU fallback
            // Serve expired entries rather than nothing when network fails
            val stale = OfferCache.getAllStale()
            if (stale.isNotEmpty()) {
                android.util.Log.d("GOATLY", "Network failed — serving stale LRU cache (${stale.size} items)")
                return stale
            }

            // Isabella — Sprint 3: Local Storage — Level 4: Room database fallback
            // Room persists offers across sessions — survives app restarts without network
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

            // Level 5: Mock fallback — last resort
            android.util.Log.d("GOATLY", "All fallbacks exhausted — using mock data")
            MockOfferRepository().getAll()
        }
    }
}