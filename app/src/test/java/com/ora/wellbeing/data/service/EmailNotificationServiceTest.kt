package com.ora.wellbeing.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ora.wellbeing.data.local.dao.EmailPreferencesDao
import com.ora.wellbeing.data.local.entities.EmailPreferencesEntity
import com.ora.wellbeing.data.remote.api.OraWebAppApi
import com.ora.wellbeing.data.remote.model.EmailResponse
import com.ora.wellbeing.data.remote.model.EmailType
import com.ora.wellbeing.data.remote.model.OnboardingCompleteRequest
import com.ora.wellbeing.data.remote.model.SendEmailRequest
import com.ora.wellbeing.data.remote.model.WelcomeEmailRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for EmailNotificationService.
 *
 * Tests cover:
 * - Welcome email sending (success, failure, retry)
 * - Onboarding complete email with recommendations
 * - Streak milestone email with preference checking
 * - Program complete email with details
 * - First journal entry email with FirebaseAuth email lookup
 * - Retry logic with exponential backoff
 * - User preference respect (opt-out scenarios)
 *
 * Related to Issue #56: Tests unitaires EmailNotificationService
 */
class EmailNotificationServiceTest {

    // Mocks
    private lateinit var oraWebAppApi: OraWebAppApi
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emailPreferencesDao: EmailPreferencesDao
    private lateinit var firebaseUser: FirebaseUser

    // System under test
    private lateinit var emailNotificationService: EmailNotificationService

    // Test data
    private val testUid = "test_user_123"
    private val testEmail = "test@ora.com"
    private val testFirstName = "Jean"
    private val testMessageId = "msg_abc123"

    private val defaultPreferences = EmailPreferencesEntity(
        uid = testUid,
        welcomeEmails = true,
        engagementEmails = true,
        marketingEmails = false,
        streakReminders = true,
        weeklyDigest = false,
        language = "fr"
    )

    private val successResponse = EmailResponse(
        success = true,
        messageId = testMessageId,
        error = null
    )

    private val errorResponse = EmailResponse(
        success = false,
        messageId = null,
        error = "API error"
    )

    @Before
    fun setup() {
        oraWebAppApi = mockk()
        firebaseAuth = mockk()
        emailPreferencesDao = mockk()
        firebaseUser = mockk()

        // Default Firebase Auth setup
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.email } returns testEmail

        // Default preferences setup
        coEvery { emailPreferencesDao.getEmailPreferences(any()) } returns defaultPreferences

        emailNotificationService = EmailNotificationService(
            oraWebAppApi = oraWebAppApi,
            firebaseAuth = firebaseAuth,
            emailPreferencesDao = emailPreferencesDao
        )
    }

    // ============================================================
    // Welcome Email Tests
    // ============================================================

    @Test
    fun `sendWelcomeEmail success calls API and returns true`() = runTest {
        // Given
        val requestSlot = slot<WelcomeEmailRequest>()
        coEvery { oraWebAppApi.sendWelcomeEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { oraWebAppApi.sendWelcomeEmail(any()) }

        // Verify request content
        assertEquals(testUid, requestSlot.captured.userId)
        assertEquals(testEmail, requestSlot.captured.email)
        assertEquals(testFirstName, requestSlot.captured.firstName)
        assertEquals("fr", requestSlot.captured.language)
    }

    @Test
    fun `sendWelcomeEmail with null firstName uses default name`() = runTest {
        // Given
        val requestSlot = slot<WelcomeEmailRequest>()
        coEvery { oraWebAppApi.sendWelcomeEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = null
        )

        // Then
        assertTrue(result)
        assertEquals("Ami", requestSlot.captured.firstName)
    }

    @Test
    fun `sendWelcomeEmail networkError retries with backoff and eventually fails`() = runTest {
        // Given - API always returns error
        coEvery { oraWebAppApi.sendWelcomeEmail(any()) } returns Response.success(errorResponse)

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then - Should have retried 3 times total
        assertFalse(result)
        coVerify(exactly = 3) { oraWebAppApi.sendWelcomeEmail(any()) }
    }

    @Test
    fun `sendWelcomeEmail networkError succeeds on second attempt`() = runTest {
        // Given - First call fails, second succeeds
        coEvery { oraWebAppApi.sendWelcomeEmail(any()) } returnsMany listOf(
            Response.success(errorResponse),
            Response.success(successResponse)
        )

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then
        assertTrue(result)
        coVerify(exactly = 2) { oraWebAppApi.sendWelcomeEmail(any()) }
    }

    @Test
    fun `sendWelcomeEmail with HTTP error retries`() = runTest {
        // Given - HTTP 500 error
        coEvery { oraWebAppApi.sendWelcomeEmail(any()) } returns Response.error(
            500,
            "Internal Server Error".toResponseBody(null)
        )

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then
        assertFalse(result)
        coVerify(exactly = 3) { oraWebAppApi.sendWelcomeEmail(any()) }
    }

    @Test
    fun `sendWelcomeEmail uses user preferred language`() = runTest {
        // Given
        val frenchPrefs = defaultPreferences.copy(language = "en")
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns frenchPrefs

        val requestSlot = slot<WelcomeEmailRequest>()
        coEvery { oraWebAppApi.sendWelcomeEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then
        assertEquals("en", requestSlot.captured.language)
    }

    @Test
    fun `sendWelcomeEmail uses default language when preferences not found`() = runTest {
        // Given
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns null

        val requestSlot = slot<WelcomeEmailRequest>()
        coEvery { oraWebAppApi.sendWelcomeEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then
        assertEquals("fr", requestSlot.captured.language)
    }

    // ============================================================
    // Onboarding Complete Email Tests
    // ============================================================

    @Test
    fun `sendOnboardingCompleteEmail success includes recommendations`() = runTest {
        // Given
        val recommendations = listOf("meditation", "yoga", "sleep")
        val requestSlot = slot<OnboardingCompleteRequest>()
        coEvery { oraWebAppApi.sendOnboardingCompleteEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendOnboardingCompleteEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName,
            recommendations = recommendations
        )

        // Then
        assertTrue(result)
        assertEquals(testUid, requestSlot.captured.userId)
        assertEquals(testEmail, requestSlot.captured.email)
        assertEquals(testFirstName, requestSlot.captured.firstName)
        assertEquals(recommendations, requestSlot.captured.selectedGoals)
        assertEquals("fr", requestSlot.captured.language)
    }

    @Test
    fun `sendOnboardingCompleteEmail with null recommendations uses empty list`() = runTest {
        // Given
        val requestSlot = slot<OnboardingCompleteRequest>()
        coEvery { oraWebAppApi.sendOnboardingCompleteEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendOnboardingCompleteEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName,
            recommendations = null
        )

        // Then
        assertTrue(result)
        assertEquals(emptyList<String>(), requestSlot.captured.selectedGoals)
    }

    @Test
    fun `sendOnboardingCompleteEmail with null firstName uses default name`() = runTest {
        // Given
        val requestSlot = slot<OnboardingCompleteRequest>()
        coEvery { oraWebAppApi.sendOnboardingCompleteEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        emailNotificationService.sendOnboardingCompleteEmail(
            uid = testUid,
            email = testEmail,
            firstName = null,
            recommendations = listOf("yoga")
        )

        // Then
        assertEquals("Ami", requestSlot.captured.firstName)
    }

    @Test
    fun `sendOnboardingCompleteEmail failure retries and returns false`() = runTest {
        // Given
        coEvery { oraWebAppApi.sendOnboardingCompleteEmail(any()) } returns Response.success(errorResponse)

        // When
        val result = emailNotificationService.sendOnboardingCompleteEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName,
            recommendations = listOf("meditation")
        )

        // Then
        assertFalse(result)
        coVerify(exactly = 3) { oraWebAppApi.sendOnboardingCompleteEmail(any()) }
    }

    // ============================================================
    // Streak Milestone Email Tests
    // ============================================================

    @Test
    fun `sendStreakMilestoneEmail success calls API with correct days`() = runTest {
        // Given
        val streakDays = 7
        val requestSlot = slot<SendEmailRequest>()
        coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendStreakMilestoneEmail(
            uid = testUid,
            streakDays = streakDays
        )

        // Then
        assertTrue(result)
        assertEquals(testUid, requestSlot.captured.userId)
        assertEquals(testEmail, requestSlot.captured.email)
        assertEquals(EmailType.STREAK_MILESTONE, requestSlot.captured.emailType)
        assertEquals(streakDays, requestSlot.captured.templateData?.get("streak_days"))
        assertEquals("one_week", requestSlot.captured.templateData?.get("milestone_type"))
    }

    @Test
    fun `sendStreakMilestoneEmail userOptedOut does not send and returns true`() = runTest {
        // Given - User has disabled streak reminders
        val disabledPrefs = defaultPreferences.copy(streakReminders = false)
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns disabledPrefs

        // When
        val result = emailNotificationService.sendStreakMilestoneEmail(
            uid = testUid,
            streakDays = 7
        )

        // Then - Should return true (intentional skip) but not call API
        assertTrue(result)
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `sendStreakMilestoneEmail without authenticated user returns false`() = runTest {
        // Given - No authenticated user
        every { firebaseAuth.currentUser } returns null

        // When
        val result = emailNotificationService.sendStreakMilestoneEmail(
            uid = testUid,
            streakDays = 7
        )

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `sendStreakMilestoneEmail maps milestone types correctly`() = runTest {
        // Given
        val milestoneTests = listOf(
            7 to "one_week",
            14 to "two_weeks",
            30 to "thirty_days",
            60 to "sixty_days",
            100 to "hundred_days",
            365 to "one_year",
            3 to "starting"
        )

        for ((days, expectedType) in milestoneTests) {
            val requestSlot = slot<SendEmailRequest>()
            coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

            // When
            emailNotificationService.sendStreakMilestoneEmail(uid = testUid, streakDays = days)

            // Then
            assertEquals(expectedType, requestSlot.captured.templateData?.get("milestone_type"),
                "Expected $expectedType for $days days")
        }
    }

    @Test
    fun `sendStreakMilestoneEmail defaults to enabled when preferences not found`() = runTest {
        // Given - Preferences not found (returns null)
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns null
        coEvery { oraWebAppApi.sendEmail(any()) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendStreakMilestoneEmail(
            uid = testUid,
            streakDays = 7
        )

        // Then - Should send email (default to enabled)
        assertTrue(result)
        coVerify(exactly = 1) { oraWebAppApi.sendEmail(any()) }
    }

    // ============================================================
    // Program Complete Email Tests
    // ============================================================

    @Test
    fun `sendProgramCompleteEmail success includes program details`() = runTest {
        // Given
        val programId = "program_123"
        val programTitle = "7 Days of Meditation"
        val requestSlot = slot<SendEmailRequest>()
        coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendProgramCompleteEmail(
            uid = testUid,
            programId = programId,
            programTitle = programTitle
        )

        // Then
        assertTrue(result)
        assertEquals(testUid, requestSlot.captured.userId)
        assertEquals(testEmail, requestSlot.captured.email)
        assertEquals(EmailType.PROGRAM_COMPLETE, requestSlot.captured.emailType)
        assertEquals(programId, requestSlot.captured.templateData?.get("program_id"))
        assertEquals(programTitle, requestSlot.captured.templateData?.get("program_title"))
    }

    @Test
    fun `sendProgramCompleteEmail userOptedOut does not send`() = runTest {
        // Given - User has disabled engagement emails (achievement notifications)
        val disabledPrefs = defaultPreferences.copy(engagementEmails = false)
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns disabledPrefs

        // When
        val result = emailNotificationService.sendProgramCompleteEmail(
            uid = testUid,
            programId = "program_123",
            programTitle = "7 Days of Meditation"
        )

        // Then
        assertTrue(result) // Intentional skip returns true
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `sendProgramCompleteEmail without authenticated user returns false`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = emailNotificationService.sendProgramCompleteEmail(
            uid = testUid,
            programId = "program_123",
            programTitle = "7 Days of Meditation"
        )

        // Then
        assertFalse(result)
    }

    // ============================================================
    // First Completion Email Tests
    // ============================================================

    @Test
    fun `sendFirstCompletionEmail success includes content details`() = runTest {
        // Given
        val contentType = "meditation"
        val contentTitle = "Morning Calm"
        val requestSlot = slot<SendEmailRequest>()
        coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendFirstCompletionEmail(
            uid = testUid,
            contentType = contentType,
            contentTitle = contentTitle
        )

        // Then
        assertTrue(result)
        assertEquals(EmailType.FIRST_COMPLETION, requestSlot.captured.emailType)
        assertEquals(contentType, requestSlot.captured.templateData?.get("content_type"))
        assertEquals(contentTitle, requestSlot.captured.templateData?.get("content_title"))
    }

    @Test
    fun `sendFirstCompletionEmail userOptedOut does not send`() = runTest {
        // Given
        val disabledPrefs = defaultPreferences.copy(engagementEmails = false)
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns disabledPrefs

        // When
        val result = emailNotificationService.sendFirstCompletionEmail(
            uid = testUid,
            contentType = "yoga",
            contentTitle = "Sun Salutation"
        )

        // Then
        assertTrue(result) // Intentional skip
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    // ============================================================
    // First Journal Entry Email Tests
    // ============================================================

    @Test
    fun `sendFirstJournalEntryEmail fetches email from FirebaseAuth`() = runTest {
        // Given
        val requestSlot = slot<SendEmailRequest>()
        coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        val result = emailNotificationService.sendFirstJournalEntryEmail(uid = testUid)

        // Then
        assertTrue(result)
        assertEquals(testEmail, requestSlot.captured.email)
        assertEquals(EmailType.FIRST_JOURNAL_ENTRY, requestSlot.captured.emailType)
    }

    @Test
    fun `sendFirstJournalEntryEmail without authenticated user returns false`() = runTest {
        // Given
        every { firebaseAuth.currentUser } returns null

        // When
        val result = emailNotificationService.sendFirstJournalEntryEmail(uid = testUid)

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `sendFirstJournalEntryEmail userOptedOut does not send`() = runTest {
        // Given
        val disabledPrefs = defaultPreferences.copy(engagementEmails = false)
        coEvery { emailPreferencesDao.getEmailPreferences(testUid) } returns disabledPrefs

        // When
        val result = emailNotificationService.sendFirstJournalEntryEmail(uid = testUid)

        // Then
        assertTrue(result) // Intentional skip
        coVerify(exactly = 0) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `sendFirstJournalEntryEmail with null template data`() = runTest {
        // Given
        val requestSlot = slot<SendEmailRequest>()
        coEvery { oraWebAppApi.sendEmail(capture(requestSlot)) } returns Response.success(successResponse)

        // When
        emailNotificationService.sendFirstJournalEntryEmail(uid = testUid)

        // Then - Template data should be null for this email type
        assertEquals(null, requestSlot.captured.templateData)
    }

    // ============================================================
    // Retry Logic Edge Cases
    // ============================================================

    @Test
    fun `retry logic handles exception from preferences dao gracefully`() = runTest {
        // Given - DAO throws exception
        coEvery { emailPreferencesDao.getEmailPreferences(any()) } throws RuntimeException("Database error")
        coEvery { oraWebAppApi.sendEmail(any()) } returns Response.success(successResponse)

        // When - Should default to sending (fallback to true)
        val result = emailNotificationService.sendStreakMilestoneEmail(
            uid = testUid,
            streakDays = 7
        )

        // Then - Email should still be sent
        assertTrue(result)
        coVerify(exactly = 1) { oraWebAppApi.sendEmail(any()) }
    }

    @Test
    fun `unexpected exception during API call does not retry`() = runTest {
        // Given - API throws unexpected exception (not EmailSendException)
        coEvery { oraWebAppApi.sendWelcomeEmail(any()) } throws RuntimeException("Network unavailable")

        // When
        val result = emailNotificationService.sendWelcomeEmail(
            uid = testUid,
            email = testEmail,
            firstName = testFirstName
        )

        // Then - Should fail immediately without retrying
        assertFalse(result)
        coVerify(exactly = 1) { oraWebAppApi.sendWelcomeEmail(any()) }
    }
}
