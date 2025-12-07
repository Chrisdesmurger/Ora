package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing user massage preferences
 *
 * Stores personalized settings for each body zone including
 * preferred duration, pressure level, and custom notes.
 */
@Entity(tableName = "massage_preferences")
data class MassagePreferenceEntity(
    @PrimaryKey
    val id: String,

    /** User ID for multi-user support */
    val userId: String,

    /** Body zone ID (neck, shoulders, back, arms, hands, etc.) */
    val zoneId: String,

    /** Custom duration for this zone in milliseconds */
    val customDurationMs: Long,

    /** Preferred pressure level (LOW, MEDIUM, HIGH) */
    val preferredPressureLevel: String,

    /** Number of repetitions preferred */
    val preferredRepetitions: Int = 3,

    /** Pause duration between zones in milliseconds */
    val pauseBetweenZonesMs: Long = 5000L,

    /** Whether this zone is marked as favorite */
    val isFavoriteZone: Boolean = false,

    /** Custom notes for this zone */
    val customNotes: String? = null,

    /** Whether haptic feedback is enabled for this zone */
    val hapticFeedbackEnabled: Boolean = true,

    /** Whether voice instructions are enabled for this zone */
    val voiceInstructionsEnabled: Boolean = true,

    /** Last time this zone was massaged */
    val lastMassagedAt: Long? = null,

    /** Total times this zone has been massaged */
    val totalMassageCount: Int = 0,

    /** Created timestamp */
    val createdAt: Long = System.currentTimeMillis(),

    /** Updated timestamp */
    val updatedAt: Long = System.currentTimeMillis()
)
