package com.ora.wellbeing.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FIX(auth): Module Hilt pour l'authentification Firebase
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Fournit l'instance FirebaseAuth
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
