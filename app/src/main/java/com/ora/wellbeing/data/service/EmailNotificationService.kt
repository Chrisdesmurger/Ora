package com.ora.wellbeing.data.service

import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.local.dao.EmailPreferencesDao
import com.ora.wellbeing.data.remote.api.OraWebAppApi
import com.ora.wellbeing.data.remote.model.EmailType
import com.ora.wellbeing.data.remote.model.OnboardingCompleteRequest
import com.ora.wellbeing.data.remote.model.SendEmailRequest
import com.ora.wellbeing.data.remote.model.WelcomeEmailRequest
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for sending email notifications via OraWebApp API.
 *
 * This service:
 * - Wraps the OraWebAppApi for convenient email sending
 * - Implements retry logic with exponential backoff (3 attempts: 1s, 2s, 4s)
 * - Checks user email preferences before sending (when applicable)
 * - Handles errors gracefully without crashing the app
 * - Logs all operations with Timber
 *
 * Usage:
 * ```kotlin
 * @Inject lateinit var emailService: EmailNotificationService
 *
 * // After user registration
 * emailService.sendWelcomeEmail(uid = user.uid, email = user.email, firstName = "John")
 *
 * // After completing onboarding
 * emailService.sendOnboardingCompleteEmail(
 *     uid = user.uid,
 *     email = user.email,
 *     firstName = "John",
 *     recommendations = listOf("meditation", "yoga", "sleep")
 * )
 * ```
 *
 * @see OraWebAppApi for the underlying API endpoints
 * @see EmailPreferencesDao for checking user preferences
 */
@Singleton
class EmailNotificationService @Inject constructor(
    private val oraWebAppApi: OraWebAppApi,
    private val firebaseAuth: FirebaseAuth,
    private val emailPreferencesDao: EmailPreferencesDao
) {

    companion object {
        private const val TAG = "EmailNotificationService"

        // Retry configuration
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_DELAY_MS = 1000L
        private const val BACKOFF_MULTIPLIER = 2.0

        // Default language for emails
        private const val DEFAULT_LANGUAGE = "fr"
    }

    // ============================================================
    // Public Email Sending Methods
    // ============================================================

    /**
     * Send a welcome email to a newly registered user.
     *
     * Called after successful Firebase Auth registration.
     * This email does not check preferences as it's the first email.
     *
     * @param uid Firebase user ID
     * @param email User's email address
     * @param firstName User's first name (optional, defaults to "Ami" if null)
     * @return true if email was sent successfully, false otherwise
     */
    suspend fun sendWelcomeEmail(
        uid: String,
        email: String,
        firstName: String?
    ): Boolean {
        Timber.tag(TAG).d("Sending welcome email to $email (uid: $uid)")

        return retryWithBackoff {
            val request = WelcomeEmailRequest(
                userId = uid,
                email = email,
                firstName = firstName ?: "Ami",
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendWelcomeEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("Welcome email sent successfully to $email (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send welcome email: $errorMessage")
                throw EmailSendException("Failed to send welcome email: $errorMessage")
            }
        }
    }

    /**
     * Send an onboarding completion email with personalized recommendations.
     *
     * Called when user completes the onboarding questionnaire.
     * This email does not check preferences as it's part of onboarding.
     *
     * @param uid Firebase user ID
     * @param email User's email address
     * @param firstName User's first name (optional)
     * @param recommendations List of recommended content types based on user's goals
     * @return true if email was sent successfully, false otherwise
     */
    suspend fun sendOnboardingCompleteEmail(
        uid: String,
        email: String,
        firstName: String?,
        recommendations: List<String>?
    ): Boolean {
        Timber.tag(TAG).d("Sending onboarding complete email to $email (uid: $uid)")

        return retryWithBackoff {
            val request = OnboardingCompleteRequest(
                userId = uid,
                email = email,
                firstName = firstName ?: "Ami",
                selectedGoals = recommendations ?: emptyList(),
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendOnboardingCompleteEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("Onboarding complete email sent to $email (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send onboarding complete email: $errorMessage")
                throw EmailSendException("Failed to send onboarding complete email: $errorMessage")
            }
        }
    }

    /**
     * Send a streak milestone email celebrating user's consistency.
     *
     * Called when user reaches a streak milestone (7, 14, 30, 60, 100 days, etc.).
     * Checks user preferences before sending (streakReminders must be enabled).
     *
     * @param uid Firebase user ID
     * @param streakDays Number of consecutive days
     * @return true if email was sent successfully or skipped due to preferences, false on error
     */
    suspend fun sendStreakMilestoneEmail(
        uid: String,
        streakDays: Int
    ): Boolean {
        Timber.tag(TAG).d("Sending streak milestone email for $streakDays days (uid: $uid)")

        // Check user preferences
        if (!shouldSendStreakEmail(uid)) {
            Timber.tag(TAG).d("Streak email skipped - user preference disabled (uid: $uid)")
            return true // Return true as it's an intentional skip
        }

        val email = getCurrentUserEmail() ?: run {
            Timber.tag(TAG).w("Cannot send streak email - no authenticated user")
            return false
        }

        return retryWithBackoff {
            val request = SendEmailRequest(
                userId = uid,
                email = email,
                emailType = EmailType.STREAK_MILESTONE,
                templateData = mapOf(
                    "streak_days" to streakDays,
                    "milestone_type" to getMilestoneType(streakDays)
                ),
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("Streak milestone email sent for $streakDays days (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send streak milestone email: $errorMessage")
                throw EmailSendException("Failed to send streak milestone email: $errorMessage")
            }
        }
    }

    /**
     * Send a program completion email celebrating user's achievement.
     *
     * Called when user completes all lessons in a program.
     * Checks user preferences before sending (achievementNotifications must be enabled).
     *
     * @param uid Firebase user ID
     * @param programId ID of the completed program
     * @param programTitle Title of the completed program
     * @return true if email was sent successfully or skipped due to preferences, false on error
     */
    suspend fun sendProgramCompleteEmail(
        uid: String,
        programId: String,
        programTitle: String
    ): Boolean {
        Timber.tag(TAG).d("Sending program complete email for '$programTitle' (uid: $uid)")

        // Check user preferences
        if (!shouldSendAchievementEmail(uid)) {
            Timber.tag(TAG).d("Program complete email skipped - user preference disabled (uid: $uid)")
            return true
        }

        val email = getCurrentUserEmail() ?: run {
            Timber.tag(TAG).w("Cannot send program complete email - no authenticated user")
            return false
        }

        return retryWithBackoff {
            val request = SendEmailRequest(
                userId = uid,
                email = email,
                emailType = EmailType.PROGRAM_COMPLETE,
                templateData = mapOf(
                    "program_id" to programId,
                    "program_title" to programTitle
                ),
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("Program complete email sent for '$programTitle' (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send program complete email: $errorMessage")
                throw EmailSendException("Failed to send program complete email: $errorMessage")
            }
        }
    }

    /**
     * Send a first completion email when user completes their first content item.
     *
     * Called when user finishes their first meditation, yoga session, etc.
     * Checks user preferences before sending (achievementNotifications must be enabled).
     *
     * @param uid Firebase user ID
     * @param contentType Type of content completed (meditation, yoga, etc.)
     * @param contentTitle Title of the completed content
     * @return true if email was sent successfully or skipped due to preferences, false on error
     */
    suspend fun sendFirstCompletionEmail(
        uid: String,
        contentType: String,
        contentTitle: String
    ): Boolean {
        Timber.tag(TAG).d("Sending first completion email for '$contentTitle' ($contentType) (uid: $uid)")

        // Check user preferences
        if (!shouldSendAchievementEmail(uid)) {
            Timber.tag(TAG).d("First completion email skipped - user preference disabled (uid: $uid)")
            return true
        }

        val email = getCurrentUserEmail() ?: run {
            Timber.tag(TAG).w("Cannot send first completion email - no authenticated user")
            return false
        }

        return retryWithBackoff {
            val request = SendEmailRequest(
                userId = uid,
                email = email,
                emailType = EmailType.FIRST_COMPLETION,
                templateData = mapOf(
                    "content_type" to contentType,
                    "content_title" to contentTitle
                ),
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("First completion email sent for '$contentTitle' (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send first completion email: $errorMessage")
                throw EmailSendException("Failed to send first completion email: $errorMessage")
            }
        }
    }

    /**
     * Send a first journal entry email when user writes their first gratitude entry.
     *
     * Called when user creates their first journal entry.
     * Checks user preferences before sending (engagementEmails must be enabled).
     *
     * @param uid Firebase user ID
     * @return true if email was sent successfully or skipped due to preferences, false on error
     */
    suspend fun sendFirstJournalEntryEmail(uid: String): Boolean {
        Timber.tag(TAG).d("Sending first journal entry email (uid: $uid)")

        // Check user preferences
        if (!shouldSendEngagementEmail(uid)) {
            Timber.tag(TAG).d("First journal entry email skipped - user preference disabled (uid: $uid)")
            return true
        }

        val email = getCurrentUserEmail() ?: run {
            Timber.tag(TAG).w("Cannot send first journal entry email - no authenticated user")
            return false
        }

        return retryWithBackoff {
            val request = SendEmailRequest(
                userId = uid,
                email = email,
                emailType = EmailType.FIRST_JOURNAL_ENTRY,
                templateData = null,
                language = getPreferredLanguage(uid)
            )

            val response = oraWebAppApi.sendEmail(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag(TAG).i("First journal entry email sent (messageId: ${response.body()?.messageId})")
                true
            } else {
                val errorMessage = response.body()?.error ?: response.errorBody()?.string() ?: "Unknown error"
                Timber.tag(TAG).e("Failed to send first journal entry email: $errorMessage")
                throw EmailSendException("Failed to send first journal entry email: $errorMessage")
            }
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Get the current authenticated user's email.
     */
    private fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    /**
     * Get user's preferred language for emails.
     * Falls back to DEFAULT_LANGUAGE if not found.
     */
    private suspend fun getPreferredLanguage(uid: String): String {
        return try {
            emailPreferencesDao.getEmailPreferences(uid)?.language ?: DEFAULT_LANGUAGE
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to get email preferences for language, using default")
            DEFAULT_LANGUAGE
        }
    }

    /**
     * Check if streak reminder emails should be sent for this user.
     */
    private suspend fun shouldSendStreakEmail(uid: String): Boolean {
        return try {
            emailPreferencesDao.getEmailPreferences(uid)?.streakReminders ?: true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to check streak email preference, defaulting to true")
            true
        }
    }

    /**
     * Check if achievement notification emails should be sent for this user.
     */
    private suspend fun shouldSendAchievementEmail(uid: String): Boolean {
        return try {
            // Using engagementEmails as a proxy for achievement notifications
            emailPreferencesDao.getEmailPreferences(uid)?.engagementEmails ?: true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to check achievement email preference, defaulting to true")
            true
        }
    }

    /**
     * Check if engagement emails should be sent for this user.
     */
    private suspend fun shouldSendEngagementEmail(uid: String): Boolean {
        return try {
            emailPreferencesDao.getEmailPreferences(uid)?.engagementEmails ?: true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to check engagement email preference, defaulting to true")
            true
        }
    }

    /**
     * Determine the milestone type based on streak days.
     */
    private fun getMilestoneType(streakDays: Int): String {
        return when {
            streakDays >= 365 -> "one_year"
            streakDays >= 100 -> "hundred_days"
            streakDays >= 60 -> "sixty_days"
            streakDays >= 30 -> "thirty_days"
            streakDays >= 14 -> "two_weeks"
            streakDays >= 7 -> "one_week"
            else -> "starting"
        }
    }

    // ============================================================
    // Retry Logic
    // ============================================================

    /**
     * Execute an action with exponential backoff retry logic.
     *
     * Retries up to MAX_RETRY_ATTEMPTS times with delays of:
     * - 1st retry: 1 second
     * - 2nd retry: 2 seconds
     * - 3rd retry: 4 seconds
     *
     * @param action The suspend function to execute
     * @return true if action succeeded, false if all retries failed
     */
    private suspend fun retryWithBackoff(
        action: suspend () -> Boolean
    ): Boolean {
        var currentDelay = INITIAL_DELAY_MS
        var attempt = 0

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                return action()
            } catch (e: EmailSendException) {
                attempt++
                if (attempt >= MAX_RETRY_ATTEMPTS) {
                    Timber.tag(TAG).e("Email send failed after $MAX_RETRY_ATTEMPTS attempts: ${e.message}")
                    return false
                }
                Timber.tag(TAG).w("Email send attempt $attempt failed, retrying in ${currentDelay}ms: ${e.message}")
                delay(currentDelay)
                currentDelay = (currentDelay * BACKOFF_MULTIPLIER).toLong()
            } catch (e: Exception) {
                // For unexpected exceptions, don't retry
                Timber.tag(TAG).e(e, "Unexpected error sending email, not retrying")
                return false
            }
        }

        return false
    }

    /**
     * Exception thrown when email sending fails.
     * Used internally for retry logic.
     */
    private class EmailSendException(message: String) : Exception(message)
}
