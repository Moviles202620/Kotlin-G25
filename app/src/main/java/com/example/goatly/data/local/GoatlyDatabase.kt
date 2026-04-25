package com.example.goatly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Sprint 3: Local Storage — Room Database
// Singleton Room database instance for the Goatly student app.
// Stores offers locally for offline access and caching.
@Database(entities = [OfferEntity::class], version = 1, exportSchema = true)
abstract class GoatlyDatabase : RoomDatabase() {

    abstract fun offerDao(): OfferDao

    companion object {
        @Volatile
        private var INSTANCE: GoatlyDatabase? = null

        fun getInstance(context: Context): GoatlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoatlyDatabase::class.java,
                    "goatly_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// Sprint 3: Local Storage — END