package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing massage session progress for resume functionality
 *
 * Allows users to pause and resume massage sessions later,
 * tracking exactly where they left off.
 */
@Entity(tableName = "massage_progress")
data class MassageProgressEntity(
    @PrimaryKey
    val id: String,

    /** User ID for multi-user support */
    val userId: String,

    /** Related practice/content ID */
    val practiceId: String,

    /** Current zone index (0-based) */
    val currentZoneIndex: Int,

    /** Time remaining on current zone in milliseconds */
    val zoneTimeRemainingMs: Long,

    /** Repetitions remaining on current zone */
    val zoneRepetitionsRemaining: Int,

    /** JSON array of completed zone IDs */
    val completedZoneIds: String,

    /** JSON map of zone states (zoneId -> state) */
    val zoneStates: String,

    /** Current pressure level */
    val currentPressureLevel: String,

    /** Video/media position in milliseconds */
    val mediaPositionMs: Long,

    /** Whether body map was visible */
    val showBodyMap: Boolean = true,

    /** Whether circuit mode was active */
    val circuitModeActive: Boolean = false,

    /** Whether voice instructions were active */
    val voiceInstructionsActive: Boolean = false,

    /** Total session duration so far in milliseconds */
    val sessionDurationMs: Long,

    /** Session start timestamp */
    val sessionStartedAt: Long,

    /** Last paused timestamp */
    val pausedAt: Long,

    /** Created timestamp */
    val createdAt: Long = System.currentTimeMillis(),

    /** Updated timestamp */
    val updatedAt: Long = System.currentTimeMillis()
)
