package com.example.goatly.data.network

import android.util.Log
import com.example.goatly.data.model.OfferModel

// ============================================================
// Isabella — Sprint 3: Caching — LRU OfferCache
// ============================================================
// Singleton LRU cache for offers. Uses LinkedHashMap with accessOrder=true so the
// least-recently-accessed entry is evicted first when the cache exceeds MAX_SIZE.
// Each entry also carries a timestamp for TTL-based expiration (5 minutes).
// This means the cache manages both recency (LRU) and freshness (TTL) independently.
// Also tracks hit/miss statistics to measure cache effectiveness.
object OfferCache {

    private const val TAG = "GOATLY_LRU"
    private const val CACHE_TTL_MS = 5 * 60 * 1000L  // 5 minutes
    private const val MAX_SIZE = 100

    // Isabella — Sprint 3: Caching — LRU structure
    // accessOrder=true: on every get(), the accessed entry moves to the tail.
    // removeEldestEntry evicts the head (least recently accessed) when size > MAX_SIZE.
    // Constructor params: initialCapacity=16, loadFactor=0.75f, accessOrder=true
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

    // Isabella — Sprint 3: Caching — hit/miss statistics
    // Tracks how many times the cache served a response (hit) vs had to go to network (miss)
    private var hits = 0
    private var misses = 0

    private fun hitRate(): String {
        val total = hits + misses
        if (total == 0) return "0%"
        return "${(hits * 100 / total)}%"
    }

    // True if the specific offer is cached and its TTL has not expired
    fun isValid(offerId: String): Boolean {
        val entry = cache[offerId] ?: return false
        return System.currentTimeMillis() - entry.second < CACHE_TTL_MS
    }

    // Isabella — Sprint 3: Caching — fresh cache check
    // True if there is at least one non-expired entry — used to decide
    // whether the full list can be served from cache without a network call
    fun hasValidEntries(): Boolean {
        if (cache.isEmpty()) return false
        return cache.values.any { System.currentTimeMillis() - it.second < CACHE_TTL_MS }
    }

    // Isabella — Sprint 3: Caching — single get (LRU access order update)
    // Accessing an entry via get() moves it to the tail (most recently used position)
    fun get(offerId: String): OfferModel? {
        val entry = cache[offerId] ?: return null
        return if (System.currentTimeMillis() - entry.second < CACHE_TTL_MS) {
            hits++
            Log.d(TAG, "LRU single get — cache HIT for offer id=$offerId (moved to MRU position) | hits=$hits misses=$misses rate=${hitRate()}")
            entry.first
        } else {
            misses++
            Log.d(TAG, "LRU single get — cache MISS (expired) for offer id=$offerId | hits=$hits misses=$misses rate=${hitRate()}")
            cache.remove(offerId)
            null
        }
    }

    // Isabella — Sprint 3: Caching — get fresh entries only (Level 1 fallback)
    fun getAll(): List<OfferModel> {
        val valid = cache.values
            .filter { System.currentTimeMillis() - it.second < CACHE_TTL_MS }
            .map { it.first }
        val expired = cache.size - valid.size

        if (valid.isNotEmpty()) {
            hits++
            Log.d(TAG, "LRU cache HIT — served ${valid.size} fresh entries from memory, $expired expired skipped | hits=$hits misses=$misses rate=${hitRate()}")
        } else {
            misses++
            Log.d(TAG, "LRU cache MISS — no fresh entries available | hits=$hits misses=$misses rate=${hitRate()}")
        }

        return valid
    }

    // Isabella — Sprint 3: Eventual Connectivity — stale fallback (Level 3)
    // Returns all entries including expired ones — better to show stale data than nothing
    fun getAllStale(): List<OfferModel> {
        val all = cache.values.map { it.first }
        Log.d(TAG, "LRU getAllStale — serving ${all.size} entries (including expired) as offline fallback | hits=$hits misses=$misses rate=${hitRate()}")
        return all
    }

    // Isabella — Sprint 3: Caching — populate LRU after successful network fetch
    // Keyed by offer id so individual offers can be accessed and promoted in LRU order
    fun putAll(offers: List<OfferModel>) {
        val now = System.currentTimeMillis()
        offers.forEach { cache[it.id] = Pair(it, now) }
        Log.d(TAG, "LRU putAll — stored ${offers.size} offers, cache size now=${cache.size}/$MAX_SIZE | hits=$hits misses=$misses rate=${hitRate()}")
    }

    // Force full invalidation — call when user manually refreshes
    fun invalidate() {
        val size = cache.size
        cache.clear()
        hits = 0
        misses = 0
        Log.d(TAG, "LRU invalidated — cleared $size entries, stats reset")
    }

    // Isabella — Sprint 3: Caching — hit/miss stats snapshot for logging
    fun logStats() {
        Log.d(TAG, "LRU stats — hits=$hits misses=$misses rate=${hitRate()} size=${cache.size}/$MAX_SIZE")
    }
}