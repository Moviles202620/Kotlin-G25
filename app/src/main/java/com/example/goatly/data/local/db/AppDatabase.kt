package com.example.goatly.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.goatly.data.network.MyApplicationItemDto
import com.example.goatly.data.network.OfferSummaryDto

@Database(entities = [ApplicationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun applicationDao(): ApplicationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "goatly.db"
                ).build().also { INSTANCE = it }
            }
    }
}

// Mapper: DTO → Entity
fun MyApplicationItemDto.toEntity() = ApplicationEntity(
    id = id.toString(),
    offerId = offerId.toString(),
    status = status,
    createdAt = createdAt,
    offerTitle = offer.title,
    offerValueCop = offer.valueCop.toDouble(),
    offerDurationHours = offer.durationHours.toDouble(),
    offerDateTime = offer.dateTime,
    offerIsOnSite = offer.isOnSite,
    career = career,
    semester = semester,
    gpa = gpa,
    availability = availability,
    motivationLetter = motivationLetter,
    applicantName = applicantName,
    isCompleted = isCompleted,
    completedAt = completedAt,
    rating = rating,
    ratingFeedback = ratingFeedback,
    ratingPunctuality = ratingPunctuality,
    ratingQuality = ratingQuality,
    ratingAttitude = ratingAttitude
)

// Mapper: Entity → DTO (para restaurar desde cache)
fun ApplicationEntity.toDto() = MyApplicationItemDto(
    id = id.toInt(),
    offerId = offerId.toInt(),
    status = status,
    createdAt = createdAt,
    offer = OfferSummaryDto(
        id = offerId.toInt(),
        title = offerTitle ?: "",
        valueCop = offerValueCop?.toInt() ?: 0,
        durationHours = offerDurationHours?.toInt() ?: 0,
        dateTime = offerDateTime ?: "",
        isOnSite = offerIsOnSite ?: false
    ),
    career = career,
    semester = semester,
    gpa = gpa,
    availability = availability,
    motivationLetter = motivationLetter,
    applicantName = applicantName,
    isCompleted = isCompleted,
    completedAt = completedAt,
    rating = rating,
    ratingFeedback = ratingFeedback,
    ratingPunctuality = ratingPunctuality,
    ratingQuality = ratingQuality,
    ratingAttitude = ratingAttitude
)
