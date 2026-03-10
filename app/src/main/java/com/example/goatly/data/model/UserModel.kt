package com.example.goatly.data.model

data class UserModel(
    val name: String,
    val email: String,
    val major: String,
    val university: String,
    val semester: Int = 0
)
