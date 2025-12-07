package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing massage session history
 *
 * Tracks completed massage sessions with zone details,
 * durations, and user feedback for analytics and recommendations.
 */
@Entity(tableName = "massage_sessions")
data class MassageSessionEntity(
    @PrimaryKey
    val id: String,

    /** User ID for multi-user support */
    val userId: String,

    /** Related practice/content ID */
    val practiceId: String?,

    /** Session start timestamp */
    val startedAt: Long,

    /** Session end timestamp */
    val completedAt: Long?,

    /** Total session duration in milliseconds */
    val totalDurationMs: Long,

    /** Number of zones completed */
    val zonesCompleted: Int,

    /** Total zones in the session */
    val totalZones: Int,

    /** JSON array of completed zone IDs */
    val completedZoneIds: String,

    /** Average pressure level used (LOW, MEDIUM, HIGH) */
    val averagePressureLevel: String,

    /** User rating (1-5 stars, nullable) */
    val rating: Int? = null,

    /** User notes/feedback */
    val notes: String? = null,

    /** Whether session was fully completed */
    val isCompleted: Boolean = false,

    /** Whether circuit mode was used */
    val usedCircuitMode: Boolean = false,

    /** Whether voice instructions were enabled */
    val usedVoiceInstructions: Boolean = false,

    /** Created timestamp */
    val createdAt: Long = System.currentTimeMillis()
)
