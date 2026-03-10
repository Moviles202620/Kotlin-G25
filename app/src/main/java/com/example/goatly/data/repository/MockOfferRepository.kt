package com.example.goatly.data.repository

import com.example.goatly.data.mock.MockDataSource
import com.example.goatly.data.model.OfferModel

/**
 * MockOfferRepository
 * Equivalente a la lista _offers en app_state.dart de Flutter.
 */
class MockOfferRepository : OfferRepository {

    private val _offers: MutableList<OfferModel> =
        MockDataSource.getOffers().toMutableList()

    override fun getAll(): List<OfferModel> = _offers.toList()

    override fun add(offer: OfferModel): List<OfferModel> {
        _offers.add(0, offer)  // inserta al inicio, igual que en Flutter
        return _offers.toList()
    }
}
