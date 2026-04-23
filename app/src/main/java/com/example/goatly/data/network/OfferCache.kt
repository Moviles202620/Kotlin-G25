package com.example.goatly.data.network

import com.example.goatly.data.model.OfferModel

// Sprint 3: In-memory Cache — START
// Singleton that stores the last successful offers list in memory with a timestamp.
// If the data is fresher than CACHE_TTL_MS, the repository serves it directly
// without making a network call — reducing latency and supporting offline fallback.
object OfferCache {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes

    private var cachedOffers: List<OfferModel>? = null
    private var lastFetchedAt: Long = 0L

    // Returns true if cache exists and has not expired
    fun isValid(): Boolean {
        val age = System.currentTimeMillis() - lastFetchedAt
        return cachedOffers != null && age < CACHE_TTL_MS
    }

    // Retrieve cached offers — only call after checking isValid()
    fun get(): List<OfferModel> = cachedOffers ?: emptyList()

    // Store a fresh list from the backend
    fun set(offers: List<OfferModel>) {
        cachedOffers = offers
        lastFetchedAt = System.currentTimeMillis()
    }

    // Force invalidate — call this if the user manually refreshes
    fun invalidate() {
        cachedOffers = null
        lastFetchedAt = 0L
    }
}
// Sprint 3: In-memory Cache — END