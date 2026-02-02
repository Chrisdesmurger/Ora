package com.ora.wellbeing.data.remote.api

import com.ora.wellbeing.data.remote.model.EmailPreferencesResponse
import com.ora.wellbeing.data.remote.model.EmailPreferencesUpdate
import com.ora.wellbeing.data.remote.model.EmailResponse
import com.ora.wellbeing.data.remote.model.OnboardingCompleteRequest
import com.ora.wellbeing.data.remote.model.SendEmailRequest
import com.ora.wellbeing.data.remote.model.WelcomeEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API interface for communicating with OraWebApp (Next.js Admin Portal).
 *
 * This API handles email notifications via Resend integration.
 * All endpoints require Firebase authentication token in the Authorization header.
 *
 * Base URL: BuildConfig.ORA_WEBAPP_BASE_URL (configured per environment)
 *
 * @see OraWebAppModule for Hilt dependency injection setup
 * @see FirebaseAuthInterceptor for automatic token injection
 */
interface OraWebAppApi {

    // ============================================================
    // Email Endpoints
    // ============================================================

    /**
     * Send a welcome email to a newly registered user.
     *
     * Triggered after successful Firebase Auth registration.
     * The email contains onboarding tips and getting started guide.
     *
     * @param request User details for the welcome email
     * @return EmailResponse with success status and message ID
     */
    @POST("api/email/welcome")
    suspend fun sendWelcomeEmail(
        @Body request: WelcomeEmailRequest
    ): Response<EmailResponse>

    /**
     * Send an onboarding completion email.
     *
     * Triggered when user completes the onboarding questionnaire.
     * Contains personalized recommendations based on selected goals.
     *
     * @param request User details including selected goals
     * @return EmailResponse with success status and message ID
     */
    @POST("api/email/onboarding-complete")
    suspend fun sendOnboardingCompleteEmail(
        @Body request: OnboardingCompleteRequest
    ): Response<EmailResponse>

    /**
     * Send a generic email using specified template.
     *
     * Flexible endpoint for sending various email types:
     * - Weekly summary
     * - Streak reminders
     * - Achievement notifications
     * - Password reset
     * - Account verification
     *
     * @param request Email details including type and template data
     * @return EmailResponse with success status and message ID
     */
    @POST("api/email/send")
    suspend fun sendEmail(
        @Body request: SendEmailRequest
    ): Response<EmailResponse>

    // ============================================================
    // Email Preferences Endpoints
    // ============================================================

    /**
     * Get user's email notification preferences.
     *
     * Returns the current email subscription settings for the user.
     * Controls which types of emails the user wants to receive.
     *
     * @param userId Firebase Auth user ID
     * @return EmailPreferencesResponse with all preference settings
     */
    @GET("api/email/preferences/{userId}")
    suspend fun getEmailPreferences(
        @Path("userId") userId: String
    ): Response<EmailPreferencesResponse>

    /**
     * Update user's email notification preferences.
     *
     * Allows partial updates - only specified fields will be changed.
     * Useful for settings screen where user toggles individual preferences.
     *
     * @param userId Firebase Auth user ID
     * @param preferences Updated preference values (partial update supported)
     * @return EmailPreferencesResponse with updated settings
     */
    @PUT("api/email/preferences/{userId}")
    suspend fun updateEmailPreferences(
        @Path("userId") userId: String,
        @Body preferences: EmailPreferencesUpdate
    ): Response<EmailPreferencesResponse>

    // ============================================================
    // Health Check
    // ============================================================

    /**
     * Check if the OraWebApp API is available.
     *
     * Simple health check endpoint for connectivity testing.
     * Does not require authentication.
     *
     * @return Response with status 200 if API is healthy
     */
    @GET("api/health")
    suspend fun healthCheck(): Response<Unit>
}
