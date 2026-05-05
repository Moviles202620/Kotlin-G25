package com.example.goatly.data.network

import com.example.goatly.data.model.OfferModel

// Sprint 3: In-memory LRU Cache
// Singleton LRU cache for offers. Uses LinkedHashMap with accessOrder=true so the
// least-recently-accessed entry is evicted first when the cache exceeds MAX_SIZE (50).
// Each entry also carries a timestamp for TTL-based expiration (5 minutes).
// This means the cache manages both recency (LRU) and freshness (TTL) independently.
object OfferCache {

    private const val CACHE_TTL_MS = 5 * 60 * 1000L  // 5 minutes
    private const val MAX_SIZE = 50

    // accessOrder=true: on every get(), the accessed entry moves to the tail.
    // removeEldestEntry evicts the head (least recently accessed) when size > MAX_SIZE.
    private val cache = object : LinkedHashMap<String, Pair<OfferModel, Long>>(
        16, 0.75f, true
    ) {
        override fun removeEldestEntry(
            eldest: Map.Entry<String, Pair<OfferModel, Long>>
        ) = size > MAX_SIZE
    }

    // True if the specific offer is cached and its TTL has not expired
    fun isValid(offerId: String): Boolean {
        val entry = cache[offerId] ?: return false
        return System.currentTimeMillis() - entry.second < CACHE_TTL_MS
    }

    // True if there is at least one non-expired entry — used to decide
    // whether the full list can be served from cache
    fun hasValidEntries(): Boolean {
        if (cache.isEmpty()) return false
        return cache.values.any { System.currentTimeMillis() - it.second < CACHE_TTL_MS }
    }

    // Get a single offer by id — returns null if missing or expired
    fun get(offerId: String): OfferModel? {
        val entry = cache[offerId] ?: return null
        return if (System.currentTimeMillis() - entry.second < CACHE_TTL_MS) {
            entry.first
        } else {
            cache.remove(offerId)
            null
        }
    }

    // Get all non-expired offers — used for Level 1 fresh cache hit
    fun getAll(): List<OfferModel> =
        cache.values
            .filter { System.currentTimeMillis() - it.second < CACHE_TTL_MS }
            .map { it.first }

    // Get all entries including expired ones — used for Level 3 stale fallback
    // when network fails and TTL has passed but Room is also empty
    fun getAllStale(): List<OfferModel> = cache.values.map { it.first }

    // Store offers after a successful network fetch — keyed by offer id
    fun putAll(offers: List<OfferModel>) {
        val now = System.currentTimeMillis()
        offers.forEach { cache[it.id] = Pair(it, now) }
    }

    // Force full invalidation — call when user manually refreshes
    fun invalidate() = cache.clear()
}