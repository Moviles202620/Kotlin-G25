package com.example.goatly.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SHELL = "student"
    const val OFFER_DETAIL = "student/offer/{offerId}"
    const val EDIT_PROFILE = "profile/edit"
    const val SETTINGS = "profile/settings"
    const val CHANGE_PASSWORD = "profile/change-password"

    fun offerDetail(offerId: String) = "student/offer/$offerId"
}
