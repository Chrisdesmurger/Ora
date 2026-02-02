package com.ora.wellbeing.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Email type enum representing different email templates available in OraWebApp.
 * These correspond to the Resend email templates configured in the Next.js backend.
 */
enum class EmailType {
    @SerializedName("welcome")
    WELCOME,

    @SerializedName("onboarding_complete")
    ONBOARDING_COMPLETE,

    @SerializedName("weekly_summary")
    WEEKLY_SUMMARY,

    @SerializedName("streak_reminder")
    STREAK_REMINDER,

    @SerializedName("achievement_unlocked")
    ACHIEVEMENT_UNLOCKED,

    @SerializedName("password_reset")
    PASSWORD_RESET,

    @SerializedName("account_verification")
    ACCOUNT_VERIFICATION
}

/**
 * Request model for sending welcome email to new users.
 * Triggered after successful user registration.
 */
data class WelcomeEmailRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("language")
    val language: String = "fr"
)

/**
 * Request model for sending onboarding completion email.
 * Triggered when user completes the onboarding flow.
 */
data class OnboardingCompleteRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("selected_goals")
    val selectedGoals: List<String>,

    @SerializedName("preferred_time")
    val preferredTime: String? = null,

    @SerializedName("language")
    val language: String = "fr"
)

/**
 * Generic request model for sending any type of email.
 * Provides flexibility for custom email scenarios.
 */
data class SendEmailRequest(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("email_type")
    val emailType: EmailType,

    @SerializedName("template_data")
    val templateData: Map<String, Any>? = null,

    @SerializedName("language")
    val language: String = "fr"
)

/**
 * Response model for email sending operations.
 * Contains status and optional message ID from Resend.
 */
data class EmailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message_id")
    val messageId: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * Response model for fetching user email preferences.
 * Controls which types of emails the user wants to receive.
 */
data class EmailPreferencesResponse(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("marketing_emails")
    val marketingEmails: Boolean = true,

    @SerializedName("weekly_summary")
    val weeklySummary: Boolean = true,

    @SerializedName("streak_reminders")
    val streakReminders: Boolean = true,

    @SerializedName("achievement_notifications")
    val achievementNotifications: Boolean = true,

    @SerializedName("product_updates")
    val productUpdates: Boolean = true,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request model for updating user email preferences.
 * Allows partial updates - only include fields that should change.
 */
data class EmailPreferencesUpdate(
    @SerializedName("marketing_emails")
    val marketingEmails: Boolean? = null,

    @SerializedName("weekly_summary")
    val weeklySummary: Boolean? = null,

    @SerializedName("streak_reminders")
    val streakReminders: Boolean? = null,

    @SerializedName("achievement_notifications")
    val achievementNotifications: Boolean? = null,

    @SerializedName("product_updates")
    val productUpdates: Boolean? = null
)
