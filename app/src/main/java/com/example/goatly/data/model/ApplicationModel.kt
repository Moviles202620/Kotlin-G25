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
    val status: ApplicationStatus,
    // Sprint 3: BQ12 — GPA needed for average GPA near location computation
    val gpa: Float? = null
    // Sprint 3: BQ12 — END
)