package com.ora.wellbeing.di

import com.ora.wellbeing.data.local.dao.*
import com.ora.wellbeing.data.repository.*
import com.ora.wellbeing.data.repository.impl.ContentRepositoryImpl
import com.ora.wellbeing.data.repository.impl.DailyJournalRepositoryImpl
import com.ora.wellbeing.data.repository.impl.FirestoreUserProfileRepositoryImpl
import com.ora.wellbeing.data.repository.impl.FirestoreUserStatsRepositoryImpl
import com.ora.wellbeing.data.repository.impl.ProgramRepositoryImpl
import com.ora.wellbeing.data.repository.impl.RecommendationRepositoryImpl
import com.ora.wellbeing.data.repository.impl.UserProgramRepositoryImpl
import com.ora.wellbeing.domain.repository.ContentRepository as DomainContentRepository
import com.ora.wellbeing.domain.repository.DailyJournalRepository
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import com.ora.wellbeing.domain.repository.ProgramRepository as DomainProgramRepository
import com.ora.wellbeing.domain.repository.RecommendationRepository
import com.ora.wellbeing.domain.repository.UserProgramRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour fournir les repositories de l'application.
 *
 * Ce module gere deux types de repositories:
 * 1. Repositories legacy (data/repository/) - classes concretes pour DAO local
 * 2. Repositories domain (domain/repository/) - interfaces implementees dans data/repository/impl/
 *
 * FIX(build-debug-android): Ajout des bindings pour les interfaces domain manquantes
 * qui causaient les erreurs Dagger/MissingBinding
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // ============================================================================
    // Legacy Repositories (data/repository/) - Classes concretes
    // ============================================================================

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao
    ): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideDataContentRepository(
        contentDao: ContentDao,
        userActivityDao: UserActivityDao
    ): com.ora.wellbeing.data.repository.ContentRepository {
        // FIX(build-debug-android): Renamed to avoid conflict with domain interface binding
        return com.ora.wellbeing.data.repository.ContentRepository(contentDao, userActivityDao)
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

/**
 * Module Hilt pour lier les interfaces domain aux implementations.
 *
 * FIX(build-debug-android): Nouveau module @Binds pour resoudre les erreurs:
 * - [Dagger/MissingBinding] domain.repository.ContentRepository
 * - [Dagger/MissingBinding] domain.repository.DailyJournalRepository
 * - [Dagger/MissingBinding] domain.repository.ProgramRepository
 * - [Dagger/MissingBinding] domain.repository.UserProgramRepository
 * - [Dagger/MissingBinding] domain.repository.RecommendationRepository
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainRepositoryModule {

    /**
     * Bind ContentRepository interface to ContentRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.ContentRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.ContentRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindContentRepository(
        impl: ContentRepositoryImpl
    ): DomainContentRepository

    /**
     * Bind DailyJournalRepository interface to DailyJournalRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.DailyJournalRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.DailyJournalRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindDailyJournalRepository(
        impl: DailyJournalRepositoryImpl
    ): DailyJournalRepository

    /**
     * Bind ProgramRepository interface to ProgramRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.ProgramRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.ProgramRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindProgramRepository(
        impl: ProgramRepositoryImpl
    ): DomainProgramRepository

    /**
     * Bind UserProgramRepository interface to UserProgramRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.UserProgramRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.UserProgramRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindUserProgramRepository(
        impl: UserProgramRepositoryImpl
    ): UserProgramRepository

    /**
     * Bind RecommendationRepository interface to RecommendationRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.RecommendationRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.RecommendationRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(
        impl: RecommendationRepositoryImpl
    ): RecommendationRepository

    /**
     * Bind FirestoreUserProfileRepository interface to FirestoreUserProfileRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.FirestoreUserProfileRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindFirestoreUserProfileRepository(
        impl: FirestoreUserProfileRepositoryImpl
    ): FirestoreUserProfileRepository

    /**
     * Bind FirestoreUserStatsRepository interface to FirestoreUserStatsRepositoryImpl
     * Interface: com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
     * Implementation: com.ora.wellbeing.data.repository.impl.FirestoreUserStatsRepositoryImpl
     */
    @Binds
    @Singleton
    abstract fun bindFirestoreUserStatsRepository(
        impl: FirestoreUserStatsRepositoryImpl
    ): FirestoreUserStatsRepository
}
