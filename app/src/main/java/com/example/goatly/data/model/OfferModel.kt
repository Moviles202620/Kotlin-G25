package com.example.goatly.data.model

import java.util.Date

data class OfferModel(
    val id: String,
    val title: String,
    val category: String,
    val valueCop: Int,
    val dateTime: Date,
    val durationHours: Int,
    val isOnSite: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)
