package com.example.goatly.data.repository

import com.example.goatly.data.model.OfferModel

/**
 * OfferRepository
 * Contrato para operaciones sobre OfferModel.
 */
interface OfferRepository {
    fun getAll(): List<OfferModel>
    fun add(offer: OfferModel): List<OfferModel>
}
