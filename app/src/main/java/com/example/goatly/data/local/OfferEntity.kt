package com.example.goatly.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Sprint 3: Local Storage — Room Entity
// Represents a job offer stored in the local Room database.
// Used as a persistent cache that survives app restarts.
@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val valueCop: Int,
    val dateTimeMillis: Long,
    val durationHours: Int,
    val isOnSite: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val cachedAt: Long = System.currentTimeMillis()
)
// Sprint 3: Local Storage — END