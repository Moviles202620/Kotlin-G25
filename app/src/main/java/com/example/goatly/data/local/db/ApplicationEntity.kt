package com.example.goatly.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val offerId: String,
    val status: String,
    val createdAt: String,
    val offerTitle: String?,
    val offerValueCop: Double?,
    val offerDurationHours: Double?,
    val offerDateTime: String?,
    val offerIsOnSite: Boolean?,
    val career: String?,
    val semester: Int?,
    val gpa: Float?,
    val availability: String?,
    val motivationLetter: String?,
    val applicantName: String?,
    val isCompleted: Boolean,
    val completedAt: String?,
    val rating: Float?,
    val ratingFeedback: String?,
    val ratingPunctuality: Float?,
    val ratingQuality: Float?,
    val ratingAttitude: Float?,
    val cachedAt: Long = System.currentTimeMillis()
)
