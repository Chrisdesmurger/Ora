package com.ora.wellbeing.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.ora.wellbeing.data.local.dao.*
import com.ora.wellbeing.data.local.entities.*

@Database(
    entities = [
        User::class,
        JournalEntry::class,
        Content::class,
        ProgramEntity::class,
        UserActivity::class,
        UserFavorite::class,
        UserStats::class,
        Settings::class,
        NotificationPreference::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OraDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun contentDao(): ContentDao
    abstract fun programDao(): ProgramDao
    abstract fun userActivityDao(): UserActivityDao
    abstract fun userFavoriteDao(): UserFavoriteDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun settingsDao(): SettingsDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: OraDatabase? = null

        fun getDatabase(context: Context): OraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OraDatabase::class.java,
                    "ora_database"
                )
                    .addMigrations(
                        Migrations.MIGRATION_1_2,
                        Migrations.MIGRATION_2_3,
                        Migrations.MIGRATION_3_4
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}