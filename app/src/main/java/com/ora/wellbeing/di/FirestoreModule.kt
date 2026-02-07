package com.ora.wellbeing.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ora.wellbeing.data.repository.impl.GratitudeRepositoryImpl
import com.ora.wellbeing.data.service.EmailNotificationService
import com.ora.wellbeing.domain.repository.GratitudeRepository
import com.ora.wellbeing.data.repository.UserStatsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

/**
 * Module Hilt pour les instances Firebase et les repositories
 * qui ne sont pas fournis via DomainRepositoryModule (@Binds).
 *
 * Les repositories suivants sont lies via @Binds dans DomainRepositoryModule:
 * - ContentRepository
 * - DailyJournalRepository
 * - ProgramRepository
 * - UserProgramRepository
 * - RecommendationRepository
 * - FirestoreUserProfileRepository
 * - FirestoreUserStatsRepository
 */
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /**
     * Fournit l'instance Firebase Firestore avec configuration offline-first
     * - Cache persistant active (10MB)
     * - Synchronisation automatique en arriere-plan
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        Timber.d("provideFirebaseFirestore: Initializing Firestore with offline persistence")

        val firestore = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(10 * 1024 * 1024L)
            .build()

        firestore.firestoreSettings = settings

        Timber.i("provideFirebaseFirestore: Firestore configure avec cache offline 10MB")
        return firestore
    }

    /**
     * Fournit l'instance Firebase Storage pour l'upload de photos de profil
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        Timber.d("provideFirebaseStorage: Initializing Firebase Storage")
        return Firebase.storage
    }

    /**
     * Fournit le repository pour les entrees de gratitude
     */
    @Provides
    @Singleton
    fun provideGratitudeRepository(
        firestore: FirebaseFirestore
    ): GratitudeRepository {
        Timber.d("provideGratitudeRepository: Creating repository")
        return GratitudeRepositoryImpl(firestore)
    }

    /**
     * Fournit le repository pour les statistiques de pratique detaillees
     */
    @Provides
    @Singleton
    fun providePracticeStatsRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        userStatsRepository: UserStatsRepository,
        emailNotificationService: EmailNotificationService
    ): com.ora.wellbeing.data.repository.PracticeStatsRepository {
        Timber.d("providePracticeStatsRepository: Creating repository with email service")
        return com.ora.wellbeing.data.repository.PracticeStatsRepository(
            firestore,
            auth,
            userStatsRepository,
            emailNotificationService
        )
    }
}
