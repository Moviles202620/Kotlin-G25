package com.example.goatly.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SHELL = "student"
    const val OFFER_DETAIL = "student/offer/{offerId}"

    fun offerDetail(offerId: String) = "student/offer/$offerId"
}
