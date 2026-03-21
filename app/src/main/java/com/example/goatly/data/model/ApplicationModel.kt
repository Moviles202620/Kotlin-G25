package com.example.goatly.data.model

import java.util.Date

enum class ApplicationStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

data class ApplicationModel(
    val id: String,
    val applicantName: String,
    val applicantInitials: String,
    val offerId: String,
    val offerTitle: String,
    val createdAt: Date,
    val status: ApplicationStatus  // val + copy() en lugar de var mutable
)
