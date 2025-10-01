package com.ora.wellbeing.di

import com.ora.wellbeing.data.local.dao.*
import com.ora.wellbeing.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideContentRepository(
        contentDao: ContentDao,
        userActivityDao: UserActivityDao
    ): ContentRepository {
        return ContentRepository(contentDao, userActivityDao)
    }

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalDao: JournalDao
    ): JournalRepository {
        return JournalRepository(journalDao)
    }

    @Provides
    @Singleton
    fun provideUserActivityRepository(
        userActivityDao: UserActivityDao
    ): UserActivityRepository {
        return UserActivityRepository(userActivityDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDao: SettingsDao,
        notificationPreferenceDao: NotificationPreferenceDao
    ): SettingsRepository {
        return SettingsRepository(settingsDao, notificationPreferenceDao)
    }
}