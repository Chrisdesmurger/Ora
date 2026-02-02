package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.PendingEmailEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for pending emails (offline queue).
 *
 * Provides methods to manage the email queue for offline support.
 * Emails are queued when the device is offline or sending fails,
 * and retried when connectivity is restored.
 *
 * Related to Issue #50: Room entities for email preferences and offline queue.
 */
@Dao
interface PendingEmailDao {

    /**
     * Get all pending emails ordered by creation time.
     */
    @Query("SELECT * FROM pending_emails ORDER BY createdAt ASC")
    suspend fun getAllPendingEmails(): List<PendingEmailEntity>

    /**
     * Get all pending emails as a reactive Flow.
     */
    @Query("SELECT * FROM pending_emails ORDER BY createdAt ASC")
    fun getAllPendingEmailsFlow(): Flow<List<PendingEmailEntity>>

    /**
     * Get pending emails by type.
     */
    @Query("SELECT * FROM pending_emails WHERE emailType = :emailType ORDER BY createdAt ASC")
    suspend fun getPendingEmailsByType(emailType: String): List<PendingEmailEntity>

    /**
     * Get pending emails that can be retried (retry count below threshold).
     */
    @Query("SELECT * FROM pending_emails WHERE retryCount < :maxRetries ORDER BY createdAt ASC")
    suspend fun getPendingEmailsForRetry(maxRetries: Int = 3): List<PendingEmailEntity>

    /**
     * Get the oldest pending email (FIFO processing).
     */
    @Query("SELECT * FROM pending_emails ORDER BY createdAt ASC LIMIT 1")
    suspend fun getOldestPendingEmail(): PendingEmailEntity?

    /**
     * Get the count of pending emails.
     */
    @Query("SELECT COUNT(*) FROM pending_emails")
    suspend fun getPendingEmailCount(): Int

    /**
     * Get the count of pending emails as a reactive Flow.
     */
    @Query("SELECT COUNT(*) FROM pending_emails")
    fun getPendingEmailCountFlow(): Flow<Int>

    /**
     * Insert a new pending email to the queue.
     * Returns the auto-generated ID.
     */
    @Insert
    suspend fun insertPendingEmail(email: PendingEmailEntity): Long

    /**
     * Insert multiple pending emails.
     */
    @Insert
    suspend fun insertPendingEmails(emails: List<PendingEmailEntity>)

    /**
     * Update a pending email (typically to increment retry count).
     */
    @Update
    suspend fun updatePendingEmail(email: PendingEmailEntity)

    /**
     * Increment retry count for a pending email.
     */
    @Query("UPDATE pending_emails SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)

    /**
     * Delete a pending email (after successful send or max retries).
     */
    @Delete
    suspend fun deletePendingEmail(email: PendingEmailEntity)

    /**
     * Delete a pending email by ID.
     */
    @Query("DELETE FROM pending_emails WHERE id = :id")
    suspend fun deletePendingEmailById(id: Long)

    /**
     * Delete all pending emails of a specific type.
     */
    @Query("DELETE FROM pending_emails WHERE emailType = :emailType")
    suspend fun deletePendingEmailsByType(emailType: String)

    /**
     * Delete all pending emails that have exceeded max retries.
     */
    @Query("DELETE FROM pending_emails WHERE retryCount >= :maxRetries")
    suspend fun deleteFailedEmails(maxRetries: Int = 3)

    /**
     * Delete all pending emails (clear the queue).
     */
    @Query("DELETE FROM pending_emails")
    suspend fun deleteAllPendingEmails()

    /**
     * Delete old pending emails (older than specified timestamp).
     */
    @Query("DELETE FROM pending_emails WHERE createdAt < :timestamp")
    suspend fun deleteOldPendingEmails(timestamp: Long)
}
