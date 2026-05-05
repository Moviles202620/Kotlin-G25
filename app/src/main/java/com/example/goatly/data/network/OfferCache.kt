package com.example.goatly.data.network

import android.util.Log
import com.example.goatly.data.model.OfferModel

// Sprint 3: In-memory LRU Cache
// Singleton LRU cache for offers. Uses LinkedHashMap with accessOrder=true so the
// least-recently-accessed entry is evicted first when the cache exceeds MAX_SIZE (50).
// Each entry also carries a timestamp for TTL-based expiration (5 minutes).
// This means the cache manages both recency (LRU) and freshness (TTL) independently.
object OfferCache {

    private const val TAG = "GOATLY_LRU"
    private const val CACHE_TTL_MS = 5 * 60 * 1000L  // 5 minutes
    private const val MAX_SIZE = 50

    // accessOrder=true: on every get(), the accessed entry moves to the tail.
    // removeEldestEntry evicts the head (least recently accessed) when size > MAX_SIZE.
    private val cache = object : LinkedHashMap<String, Pair<OfferModel, Long>>(
        16, 0.75f, true
    ) {
        override fun removeEldestEntry(
            eldest: Map.Entry<String, Pair<OfferModel, Long>>
        ): Boolean {
            val shouldEvict = size > MAX_SIZE
            if (shouldEvict) {
                Log.d(TAG, "LRU eviction triggered — removing least recently accessed offer: id=${eldest.key} title='${eldest.value.first.title}'")
            }
            return shouldEvict
        }
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
    // Accessing an entry via get() moves it to the tail (most recently used)
    fun get(offerId: String): OfferModel? {
        val entry = cache[offerId] ?: return null
        return if (System.currentTimeMillis() - entry.second < CACHE_TTL_MS) {
            Log.d(TAG, "LRU single get — cache hit for offer id=$offerId (moved to MRU position)")
            entry.first
        } else {
            Log.d(TAG, "LRU single get — entry expired for offer id=$offerId, removing")
            cache.remove(offerId)
            null
        }
    }

    // Get all non-expired offers — used for Level 1 fresh cache hit
    fun getAll(): List<OfferModel> {
        val valid = cache.values
            .filter { System.currentTimeMillis() - it.second < CACHE_TTL_MS }
            .map { it.first }
        val expired = cache.size - valid.size
        Log.d(TAG, "LRU getAll — ${valid.size} fresh entries served, $expired expired entries skipped, capacity=$MAX_SIZE")
        return valid
    }

    // Get all entries including expired ones — used for Level 3 stale fallback
    fun getAllStale(): List<OfferModel> {
        val all = cache.values.map { it.first }
        Log.d(TAG, "LRU getAllStale — serving ${all.size} entries (including expired) as offline fallback")
        return all
    }

    // Store offers after a successful network fetch — keyed by offer id
    fun putAll(offers: List<OfferModel>) {
        val now = System.currentTimeMillis()
        offers.forEach { cache[it.id] = Pair(it, now) }
        Log.d(TAG, "LRU putAll — stored ${offers.size} offers, cache size now=${cache.size}/$MAX_SIZE")
    }

    // Force full invalidation — call when user manually refreshes
    fun invalidate() {
        val size = cache.size
        cache.clear()
        Log.d(TAG, "LRU invalidated — cleared $size entries")
    }
}