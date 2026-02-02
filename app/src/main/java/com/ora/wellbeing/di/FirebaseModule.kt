package com.ora.wellbeing.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour fournir les instances Firebase.
 *
 * FIX(build-debug-android): Creation du module pour resoudre l'erreur:
 * "FirebaseFirestore cannot be provided without an @Provides-annotated method"
 *
 * Ce module fournit:
 * - FirebaseFirestore: Base de donnees cloud Firestore
 * - FirebaseStorage: Stockage cloud pour les medias (images, videos, audio)
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * Fournit l'instance FirebaseFirestore.
     *
     * Utilise par:
     * - ContentRepositoryImpl (sync lessons)
     * - ProgramRepositoryImpl (sync programs)
     * - DailyJournalRepositoryImpl (journal entries)
     * - UserProgramRepositoryImpl (user program enrollments)
     * - RecommendationRepositoryImpl (personalized recommendations)
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    /**
     * Fournit l'instance FirebaseStorage.
     *
     * Utilise pour:
     * - Telechargement des images de contenu
     * - Telechargement des videos/audio offline
     * - Upload des photos de profil utilisateur
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }
}
