package com.example.goatly.data.local.cache

import com.example.goatly.data.network.MyApplicationItemDto

class ApplicationsCache {

    private val ttlMs = 15 * 60 * 1000L  // 15 minutos

    private val cache = object : LinkedHashMap<String, Pair<MyApplicationItemDto, Long>>(
        16, 0.75f, true  // accessOrder = true → LRU
    ) {
        override fun removeEldestEntry(
            eldest: Map.Entry<String, Pair<MyApplicationItemDto, Long>>
        ) = size > 20
    }

    fun get(id: String): MyApplicationItemDto? {
        val entry = cache[id] ?: return null
        val (item, timestamp) = entry
        return if (System.currentTimeMillis() - timestamp < ttlMs) {
            item
        } else {
            cache.remove(id)
            null
        }
    }

    fun put(id: String, item: MyApplicationItemDto) {
        cache[id] = Pair(item, System.currentTimeMillis())
    }

    fun putAll(items: List<MyApplicationItemDto>) {
        items.forEach { put(it.id.toString(), it) }
    }

    fun invalidate() = cache.clear()

    companion object {
        val instance = ApplicationsCache()
    }
}
