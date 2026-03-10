package com.example.goatly.data.repository

/**
 * RepositoryProvider
 *
 * Único lugar del proyecto que conoce qué implementación concreta usar.
 * Los ViewModels solo dependen de las interfaces (AuthRepository, etc.),
 * nunca de Mock* directamente.
 *
 * Para pasar de mock a producción, cambia las asignaciones aquí
 * sin tocar ningún ViewModel ni ninguna pantalla:
 *
 *   authRepository = ApiAuthRepository(retrofit)        // en lugar de MockAuthRepository
 *   applicationRepository = RoomApplicationRepository(db) // en lugar de MockApplicationRepository
 */
object RepositoryProvider {

    // Las instancias son singletons en memoria para que todos los ViewModels
    // compartan el mismo estado (equivalente al ChangeNotifierProvider global de Flutter)
    val authRepository: AuthRepository = MockAuthRepository()
    val applicationRepository: ApplicationRepository = MockApplicationRepository()
    val offerRepository: OfferRepository = MockOfferRepository()
}
