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
     * 1. Add new field to Content table:
     *    - needTags (List<String> stored as JSON, for "Ton besoin du jour" filtering)
     *
     * Context: Issue #33 - Daily needs section
     * This field stores tags for filtering content by daily needs categories:
     * - anti-stress: stress_relief, anxiety_relief, calm
     * - energie-matinale: morning_energy, energizing, wake_up
     * - relaxation: relaxation, deep_relaxation, unwind
     * - pratique-du-soir: evening_practice, sleep_preparation, bedtime
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add needTags column to content table (stored as JSON array)
            db.execSQL(
                """
                ALTER TABLE content
                ADD COLUMN needTags TEXT NOT NULL DEFAULT '[]'
                """.trimIndent()
            )
        }
    }
}
