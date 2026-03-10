package com.example.goatly.data.repository

import com.example.goatly.data.model.UserModel

/**
 * MockAuthRepository
 * Implementación de AuthRepository con lógica mock.
 * Equivalente a la lógica de login() en app_state.dart de Flutter.
 */
class MockAuthRepository : AuthRepository {

    private var _currentUser: UserModel? = null

    override fun login(email: String, password: String): UserModel? {
        val e = email.trim().lowercase()
        if (!e.endsWith("@uniandes.edu.co")) return null
        if (password.trim().length < 4) return null

        _currentUser = UserModel(
            name = "Estudiante Uniandes",
            email = e,
            major = "Ingeniería de Sistemas",
            university = "Universidad de los Andes",
            semester = 7
        )
        return _currentUser
    }

    override fun logout() {
        _currentUser = null
    }

    override fun currentUser(): UserModel? = _currentUser
}
