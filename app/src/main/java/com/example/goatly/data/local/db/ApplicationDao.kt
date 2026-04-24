package com.example.goatly.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    suspend fun getAll(): List<ApplicationEntity>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getByStatus(status: String): List<ApplicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<ApplicationEntity>)

    @Query("DELETE FROM applications")
    suspend fun clearAll()

    // BQ: average rating breakdown by dimension across completed applications
    @Query("""
        SELECT
            AVG(rating) as avgOverall,
            AVG(ratingPunctuality) as avgPunctuality,
            AVG(ratingQuality) as avgQuality,
            AVG(ratingAttitude) as avgAttitude,
            COUNT(*) as ratedCount
        FROM applications
        WHERE isCompleted = 1 AND rating IS NOT NULL
    """)
    suspend fun getRatingBreakdown(): RatingBreakdown?
}

data class RatingBreakdown(
    val avgOverall: Float,
    val avgPunctuality: Float,
    val avgAttitude: Float,
    val avgQuality: Float,
    val ratedCount: Int
)
