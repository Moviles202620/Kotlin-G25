package com.example.goatly.data.repository

import com.example.goatly.data.model.UserModel

/**
 * AuthRepository
 * Contrato de autenticación. El ViewModel depende de esta interfaz,
 * lo que permite cambiar la implementación (mock → real) sin tocar la UI.
 */
interface AuthRepository {
    fun login(email: String, password: String): UserModel?
    fun logout()
    fun currentUser(): UserModel?
}
