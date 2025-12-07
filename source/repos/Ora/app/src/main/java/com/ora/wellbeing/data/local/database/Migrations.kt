package com.ora.wellbeing.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room Database Migrations
 *
 * Contains all schema migrations for OraDatabase.
 * Follow the principle: NEVER use fallbackToDestructiveMigration() in production.
 */
object Migrations {

    /**
     * Migration 1 -> 2
     *
     * Changes:
     * 1. Add new fields to Content table:
     *    - programId (nullable String)
     *    - order (Int with default 0)
     *    - status (String with default "ready")
     *    - updatedAt (Long with default current time)
     *
     * 2. Create new programs table with columns:
     *    - id, title, description, category, level
     *    - durationDays, thumbnailUrl, instructor
     *    - isActive, isPremiumOnly
     *    - participantCount, rating
     *    - lessonIds (JSON array of strings)
     *    - tags (JSON array of strings)
     *    - createdAt, updatedAt, lastSyncedAt
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: Add new columns to content table with default values
            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN programId TEXT DEFAULT NULL
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN `order` INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN status TEXT NOT NULL DEFAULT 'ready'
                """.trimIndent()
            )

            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                """.trimIndent()
            )

            // Step 2: Create programs table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS programs (
                    id TEXT NOT NULL PRIMARY KEY,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    category TEXT NOT NULL,
                    level TEXT NOT NULL,
                    durationDays INTEGER NOT NULL,
                    thumbnailUrl TEXT,
                    instructor TEXT,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    isPremiumOnly INTEGER NOT NULL DEFAULT 0,
                    participantCount INTEGER NOT NULL DEFAULT 0,
                    rating REAL NOT NULL DEFAULT 0.0,
                    lessonIds TEXT NOT NULL DEFAULT '[]',
                    tags TEXT NOT NULL DEFAULT '[]',
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    lastSyncedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
                """.trimIndent()
            )

            // Step 3: Create indexes for performance
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_content_programId
                ON content(programId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_content_status
                ON content(status)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_programs_category
                ON programs(category)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_programs_level
                ON programs(level)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_programs_isActive
                ON programs(isActive)
                """.trimIndent()
            )
        }
    }

    /**
     * Migration 2 -> 3
     *
     * Changes:
     * 1. Add new field to Content table:
     *    - previewImageUrl (nullable String)
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add previewImageUrl column to content table
            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN previewImageUrl TEXT DEFAULT NULL
                """.trimIndent()
            )
        }
    }

    /**
     * Migration 3 -> 4
     *
     * Changes:
     * Create massage player tables for advanced features:
     * 1. massage_sessions - Session history and analytics
     * 2. massage_preferences - User preferences per zone
     * 3. massage_progress - Resume functionality
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: Create massage_sessions table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS massage_sessions (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    practiceId TEXT,
                    startedAt INTEGER NOT NULL,
                    completedAt INTEGER,
                    totalDurationMs INTEGER NOT NULL,
                    zonesCompleted INTEGER NOT NULL,
                    totalZones INTEGER NOT NULL,
                    completedZoneIds TEXT NOT NULL,
                    averagePressureLevel TEXT NOT NULL,
                    rating INTEGER,
                    notes TEXT,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    usedCircuitMode INTEGER NOT NULL DEFAULT 0,
                    usedVoiceInstructions INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
                """.trimIndent()
            )

            // Step 2: Create massage_preferences table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS massage_preferences (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    zoneId TEXT NOT NULL,
                    customDurationMs INTEGER NOT NULL,
                    preferredPressureLevel TEXT NOT NULL,
                    preferredRepetitions INTEGER NOT NULL DEFAULT 3,
                    pauseBetweenZonesMs INTEGER NOT NULL DEFAULT 5000,
                    isFavoriteZone INTEGER NOT NULL DEFAULT 0,
                    customNotes TEXT,
                    hapticFeedbackEnabled INTEGER NOT NULL DEFAULT 1,
                    voiceInstructionsEnabled INTEGER NOT NULL DEFAULT 1,
                    lastMassagedAt INTEGER,
                    totalMassageCount INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
                """.trimIndent()
            )

            // Step 3: Create massage_progress table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS massage_progress (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    practiceId TEXT NOT NULL,
                    currentZoneIndex INTEGER NOT NULL,
                    zoneTimeRemainingMs INTEGER NOT NULL,
                    zoneRepetitionsRemaining INTEGER NOT NULL,
                    completedZoneIds TEXT NOT NULL,
                    zoneStates TEXT NOT NULL,
                    currentPressureLevel TEXT NOT NULL,
                    mediaPositionMs INTEGER NOT NULL,
                    showBodyMap INTEGER NOT NULL DEFAULT 1,
                    circuitModeActive INTEGER NOT NULL DEFAULT 0,
                    voiceInstructionsActive INTEGER NOT NULL DEFAULT 0,
                    sessionDurationMs INTEGER NOT NULL,
                    sessionStartedAt INTEGER NOT NULL,
                    pausedAt INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
                )
                """.trimIndent()
            )

            // Step 4: Create indexes for performance
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_sessions_userId
                ON massage_sessions(userId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_sessions_practiceId
                ON massage_sessions(practiceId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_sessions_startedAt
                ON massage_sessions(startedAt)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_preferences_userId
                ON massage_preferences(userId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_preferences_zoneId
                ON massage_preferences(zoneId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS index_massage_preferences_userId_zoneId
                ON massage_preferences(userId, zoneId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_progress_userId
                ON massage_progress(userId)
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_massage_progress_practiceId
                ON massage_progress(practiceId)
                """.trimIndent()
            )
        }
    }
}
