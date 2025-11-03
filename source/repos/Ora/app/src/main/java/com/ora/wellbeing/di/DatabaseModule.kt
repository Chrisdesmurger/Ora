package com.ora.wellbeing.di

import android.content.Context
import androidx.room.Room
import com.ora.wellbeing.data.local.database.OraDatabase
import com.ora.wellbeing.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt pour la configuration de la base de données
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Fournit l'instance de la base de données Room
     */
    @Provides
    @Singleton
    fun provideOraDatabase(
        @ApplicationContext context: Context
    ): OraDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = OraDatabase::class.java,
            name = "ora_database"
        )
            .addMigrations(
                // Migrations seront ajoutées ici au besoin
            )
            .fallbackToDestructiveMigration() // Uniquement pour le développement
            .build()
    }

    /**
     * Fournit le DAO pour les utilisateurs
     */
    @Provides
    fun provideUserDao(database: OraDatabase): UserDao {
        return database.userDao()
    }

    /**
     * Fournit le DAO pour les contenus
     */
    @Provides
    fun provideContentDao(database: OraDatabase): ContentDao {
        return database.contentDao()
    }

    /**
     * Fournit le DAO pour les programmes
     */
    @Provides
    fun provideProgramDao(database: OraDatabase): ProgramDao {
        return database.programDao()
    }

    /**
     * Fournit le DAO pour les entrées de journal
     */
    @Provides
    fun provideJournalDao(database: OraDatabase): JournalDao {
        return database.journalDao()
    }

    /**
     * Fournit le DAO pour les activités utilisateur
     */
    @Provides
    fun provideUserActivityDao(database: OraDatabase): UserActivityDao {
        return database.userActivityDao()
    }


    /**
     * Fournit le DAO pour les favoris utilisateur
     */
    @Provides
    fun provideUserFavoriteDao(database: OraDatabase): UserFavoriteDao {
        return database.userFavoriteDao()
    }

    /**
     * Fournit le DAO pour les statistiques utilisateur
     */
    @Provides
    fun provideUserStatsDao(database: OraDatabase): UserStatsDao {
        return database.userStatsDao()
    }

    /**
     * Fournit le DAO pour les paramètres
     */
    @Provides
    fun provideSettingsDao(database: OraDatabase): SettingsDao {
        return database.settingsDao()
    }

    /**
     * Fournit le DAO pour les préférences de notification
     */
    @Provides
    fun provideNotificationPreferenceDao(database: OraDatabase): NotificationPreferenceDao {
        return database.notificationPreferenceDao()
    }
}
