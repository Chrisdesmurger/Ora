package com.ora.wellbeing.di

import android.content.Context
import com.ora.wellbeing.core.localization.LocalizationProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Localization dependencies.
 *
 * **Issue #39**: Internationalization (i18n) support
 */
@Module
@InstallIn(SingletonComponent::class)
object LocalizationModule {

    @Provides
    @Singleton
    fun provideLocalizationProvider(
        @ApplicationContext context: Context
    ): LocalizationProvider {
        return LocalizationProvider(context)
    }
}
