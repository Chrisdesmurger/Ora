package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.EmailPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for email preferences.
 *
 * Provides methods to read and write email preferences for users.
 * Uses Flow for reactive updates when preferences change.
 *
 * Related to Issue #50: Room entities for email preferences and offline queue.
 */
@Dao
interface EmailPreferencesDao {

    /**
     * Get email preferences for a user (one-shot query).
     */
    @Query("SELECT * FROM email_preferences WHERE uid = :uid LIMIT 1")
    suspend fun getEmailPreferences(uid: String): EmailPreferencesEntity?

    /**
     * Get email preferences as a reactive Flow.
     */
    @Query("SELECT * FROM email_preferences WHERE uid = :uid LIMIT 1")
    fun getEmailPreferencesFlow(uid: String): Flow<EmailPreferencesEntity?>

    /**
     * Insert or replace email preferences.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailPreferences(preferences: EmailPreferencesEntity)

    /**
     * Update existing email preferences.
     */
    @Update
    suspend fun updateEmailPreferences(preferences: EmailPreferencesEntity)

    /**
     * Update welcome emails preference.
     */
    @Query("UPDATE email_preferences SET welcomeEmails = :enabled, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateWelcomeEmails(uid: String, enabled: Boolean, syncedAt: Long = System.currentTimeMillis())

    /**
     * Update engagement emails preference.
     */
    @Query("UPDATE email_preferences SET engagementEmails = :enabled, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateEngagementEmails(uid: String, enabled: Boolean, syncedAt: Long = System.currentTimeMillis())

    /**
     * Update marketing emails preference.
     */
    @Query("UPDATE email_preferences SET marketingEmails = :enabled, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateMarketingEmails(uid: String, enabled: Boolean, syncedAt: Long = System.currentTimeMillis())

    /**
     * Update streak reminders preference.
     */
    @Query("UPDATE email_preferences SET streakReminders = :enabled, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateStreakReminders(uid: String, enabled: Boolean, syncedAt: Long = System.currentTimeMillis())

    /**
     * Update weekly digest preference.
     */
    @Query("UPDATE email_preferences SET weeklyDigest = :enabled, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateWeeklyDigest(uid: String, enabled: Boolean, syncedAt: Long = System.currentTimeMillis())

    /**
     * Update preferred language for emails.
     */
    @Query("UPDATE email_preferences SET language = :language, lastSyncedAt = :syncedAt WHERE uid = :uid")
    suspend fun updateLanguage(uid: String, language: String, syncedAt: Long = System.currentTimeMillis())

    /**
     * Delete email preferences for a user.
     */
    @Query("DELETE FROM email_preferences WHERE uid = :uid")
    suspend fun deleteEmailPreferences(uid: String)

    /**
     * Check if email preferences exist for a user.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM email_preferences WHERE uid = :uid)")
    suspend fun hasEmailPreferences(uid: String): Boolean
}
