package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user email preferences.
 *
 * Stores email notification preferences for different types of emails:
 * - Welcome emails: Sent after registration
 * - Engagement emails: Streak reminders, achievement notifications
 * - Marketing emails: Promotional content, new features
 * - Streak reminders: Daily/weekly streak notifications
 * - Weekly digest: Summary of weekly activity
 *
 * Related to Issue #50: Room entities for email preferences and offline queue.
 */
@Entity(tableName = "email_preferences")
data class EmailPreferencesEntity(
    @PrimaryKey
    val uid: String,
    val welcomeEmails: Boolean = true,
    val engagementEmails: Boolean = true,
    val marketingEmails: Boolean = false,
    val streakReminders: Boolean = true,
    val weeklyDigest: Boolean = false,
    val language: String = "fr",
    val lastSyncedAt: Long = 0
)
