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
}
