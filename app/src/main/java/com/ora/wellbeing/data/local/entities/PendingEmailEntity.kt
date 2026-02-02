package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing pending emails in an offline queue.
 *
 * When the device is offline or email sending fails, emails are stored
 * in this queue to be retried later when connectivity is restored.
 *
 * Fields:
 * - id: Auto-generated primary key
 * - emailType: Type of email (welcome, streak_reminder, weekly_digest, etc.)
 * - payload: JSON serialized email data (recipient, subject, template vars)
 * - createdAt: Timestamp when the email was queued
 * - retryCount: Number of retry attempts (for exponential backoff)
 *
 * Related to Issue #50: Room entities for email preferences and offline queue.
 */
@Entity(tableName = "pending_emails")
data class PendingEmailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val emailType: String,
    val payload: String, // JSON serialized email data
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)
