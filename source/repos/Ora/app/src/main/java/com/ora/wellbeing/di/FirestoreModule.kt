package com.ora.wellbeing.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.ora.wellbeing.data.repository.impl.ContentRepositoryImpl
import com.ora.wellbeing.data.repository.impl.FirestoreUserProfileRepositoryImpl
import com.ora.wellbeing.data.repository.impl.FirestoreUserStatsRepositoryImpl
import com.ora.wellbeing.data.repository.impl.GratitudeRepositoryImpl
import com.ora.wellbeing.data.repository.impl.ProgramRepositoryImpl
import com.ora.wellbeing.data.repository.impl.UserProgramRepositoryImpl
import com.ora.wellbeing.domain.repository.ContentRepository
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import com.ora.wellbeing.domain.repository.GratitudeRepository
import com.ora.wellbeing.domain.repository.ProgramRepository
import com.ora.wellbeing.domain.repository.UserProgramRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

// FIX(user-dynamic): Module Hilt pour Firestore et repositories utilisateur
// Fournit instances Firestore avec cache offline activé

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    /**
     * Fournit l'instance Firebase Firestore avec configuration offline-first
     * - Cache persistant activé (10MB)
     * - Synchronisation automatique en arrière-plan
     * - Gestion des write pending
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        Timber.d("provideFirebaseFirestore: Initializing Firestore with offline persistence")

        val firestore = Firebase.firestore

        // FIX(user-dynamic): Configuration offline selon user_data_contract.yaml
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) // Active le cache offline
            .setCacheSizeBytes(10 * 1024 * 1024L) // 10MB de cache (selon contrat)
            .build()

        firestore.firestoreSettings = settings

        Timber.i("provideFirebaseFirestore: Firestore configuré avec cache offline 10MB")
        return firestore
    }

    /**
     * Fournit le repository Firestore pour les profils utilisateur
     */
    @Provides
    @Singleton
    fun provideFirestoreUserProfileRepository(
        firestore: FirebaseFirestore
    ): FirestoreUserProfileRepository {
        Timber.d("provideFirestoreUserProfileRepository: Creating repository")
        return FirestoreUserProfileRepositoryImpl(firestore)
    }

    /**
     * Fournit le repository Firestore pour les statistiques utilisateur
     */
    @Provides
    @Singleton
    fun provideFirestoreUserStatsRepository(
        firestore: FirebaseFirestore
    ): FirestoreUserStatsRepository {
        Timber.d("provideFirestoreUserStatsRepository: Creating repository")
        return FirestoreUserStatsRepositoryImpl(firestore)
    }

    /**
     * Fournit le repository pour les entrées de gratitude
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
     * Fournit le repository pour le catalogue de programmes
     */
    @Provides
    @Singleton
    fun provideProgramRepository(
        firestore: FirebaseFirestore
    ): ProgramRepository {
        Timber.d("provideProgramRepository: Creating repository")
        return ProgramRepositoryImpl(firestore)
    }

    /**
     * Fournit le repository pour les inscriptions utilisateur aux programmes
     */
    @Provides
    @Singleton
    fun provideUserProgramRepository(
        firestore: FirebaseFirestore
    ): UserProgramRepository {
        Timber.d("provideUserProgramRepository: Creating repository")
        return UserProgramRepositoryImpl(firestore)
    }

    /**
     * Fournit le repository pour le catalogue de contenu (méditations, vidéos yoga)
     */
    @Provides
    @Singleton
    fun provideContentRepository(
        firestore: FirebaseFirestore
    ): ContentRepository {
        Timber.d("provideContentRepository: Creating repository")
        return ContentRepositoryImpl(firestore)
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
}
