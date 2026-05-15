package com.example.goatly.data.network

import android.content.Context

object AppliedOffersCache {
    private const val PREFS_NAME = "goatly_applied_offers"
    private const val KEY_APPLIED = "applied_offer_ids"

    fun markAsApplied(context: Context, offerId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_APPLIED, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(offerId)
        prefs.edit().putStringSet(KEY_APPLIED, current).apply()
    }

    fun hasApplied(context: Context, offerId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_APPLIED, emptySet())?.contains(offerId) == true
    }
}