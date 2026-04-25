package com.example.goatly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// Sprint 3: Local Storage — Room DAO
// Data Access Object for offer persistence.
// Provides insert, query, and delete operations on the local offers table.
@Dao
interface OfferDao {

    // Insert or replace all offers — called after every successful network fetch
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<OfferEntity>)

    // Get all cached offers ordered by most recently cached
    @Query("SELECT * FROM offers ORDER BY cachedAt DESC")
    suspend fun getAll(): List<OfferEntity>

    // Get a single offer by ID
    @Query("SELECT * FROM offers WHERE id = :offerId")
    suspend fun getById(offerId: String): OfferEntity?

    // Clear all cached offers — called when forced refresh
    @Query("DELETE FROM offers")
    suspend fun clearAll()

    // Count cached offers — used to check if cache is populated
    @Query("SELECT COUNT(*) FROM offers")
    suspend fun count(): Int
}
// Sprint 3: Local Storage — END