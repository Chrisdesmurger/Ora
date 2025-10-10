package com.ora.wellbeing.di

import android.content.Context
import com.ora.wellbeing.data.config.FeatureFlagManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FIX(user-dynamic): Module Hilt pour configuration et feature flags
 *
 * Fournit:
 * - FeatureFlagManager singleton pour toute l'app
 */
@Module
@InstallIn(SingletonComponent::class)
object ConfigModule {

    @Provides
    @Singleton
    fun provideFeatureFlagManager(
        @ApplicationContext context: Context
    ): FeatureFlagManager {
        return FeatureFlagManager(context)
    }
}
