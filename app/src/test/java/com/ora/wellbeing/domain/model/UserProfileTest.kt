package com.ora.wellbeing.domain.model

import org.junit.Assert.*
import org.junit.Test

/**
 * FIX(user-dynamic): Tests unitaires pour UserProfile
 *
 * Couverture:
 * - Création par défaut
 * - Propriété isPremium
 * - Fonction displayName()
 * - Edge cases (noms vides, locales, plan tiers)
 * - Validation
 */
class UserProfileTest {

    private val testUid = "test-user-456"

    // ============================================
    // Tests de création par défaut
    // ============================================

    @Test
    fun `createDefault creates profile with free tier`() {
        // FIX(user-dynamic): Nouveau user est toujours "free"
        val profile = UserProfile.createDefault(testUid)

        assertEquals(testUid, profile.uid)
        assertNull(profile.firstName)
        assertNull(profile.photoUrl)
        assertEquals("free", profile.planTier)
        assertNull(profile.locale)
        assertNull(profile.lastSyncAt)
        assertTrue(profile.createdAt > 0)
    }

    @Test
    fun `createDefault with firstName sets name correctly`() {
        // FIX(user-dynamic): Nom fourni lors de la création
        val profile = UserProfile.createDefault(testUid, firstName = "Clara")

        assertEquals(testUid, profile.uid)
        assertEquals("Clara", profile.firstName)
        assertEquals("free", profile.planTier)
    }

    @Test
    fun `createDefault without firstName has null name`() {
        // FIX(user-dynamic): Création sans nom est valide (anonyme)
        val profile = UserProfile.createDefault(testUid, firstName = null)

        assertNull(profile.firstName)
    }

    @Test
    fun `createDefault generates valid timestamp`() {
        // FIX(user-dynamic): Timestamp de création doit être raisonnable
        val beforeCreation = System.currentTimeMillis()
        val profile = UserProfile.createDefault(testUid)
        val afterCreation = System.currentTimeMillis()

        assertTrue(profile.createdAt >= beforeCreation)
        assertTrue(profile.createdAt <= afterCreation)
    }

    // ============================================
    // Tests de la propriété isPremium
    // ============================================

    @Test
    fun `isPremium returns false for free tier`() {
        // FIX(user-dynamic): Free tier n'est pas premium
        val profile = UserProfile(
            uid = testUid,
            planTier = "free"
        )

        assertFalse(profile.isPremium)
    }

    @Test
    fun `isPremium returns true for premium tier`() {
        // FIX(user-dynamic): Premium tier doit être détecté
        val profile = UserProfile(
            uid = testUid,
            planTier = "premium"
        )

        assertTrue(profile.isPremium)
    }

    // ============================================
    // Tests de displayName()
    // ============================================

    @Test
    fun `displayName returns firstName when set`() {
        // FIX(user-dynamic): Nom affiché si disponible
        val profile = UserProfile(
            uid = testUid,
            firstName = "Clara"
        )

        assertEquals("Clara", profile.displayName())
    }

    @Test
    fun `displayName returns Invité when firstName is null`() {
        // FIX(user-dynamic): "Invité" par défaut si pas de nom
        val profile = UserProfile(
            uid = testUid,
            firstName = null
        )

        assertEquals("Invité", profile.displayName())
    }

    @Test
    fun `displayName handles special characters`() {
        // FIX(user-dynamic): Caractères spéciaux et accents sont valides
        val profile = UserProfile(
            uid = testUid,
            firstName = "Éloïse-Marie"
        )

        assertEquals("Éloïse-Marie", profile.displayName())
    }

    @Test
    fun `displayName handles long name`() {
        // FIX(user-dynamic): Noms longs ne sont pas tronqués
        val longName = "Jean-Philippe-Alexandre-Maxim"
        val profile = UserProfile(
            uid = testUid,
            firstName = longName
        )

        assertEquals(longName, profile.displayName())
    }

    // ============================================
    // Tests de locale
    // ============================================

    @Test
    fun `locale can be set to fr`() {
        // FIX(user-dynamic): Locale française
        val profile = UserProfile(
            uid = testUid,
            locale = "fr"
        )

        assertEquals("fr", profile.locale)
    }

    @Test
    fun `locale can be set to en`() {
        // FIX(user-dynamic): Locale anglaise
        val profile = UserProfile(
            uid = testUid,
            locale = "en"
        )

        assertEquals("en", profile.locale)
    }

    @Test
    fun `locale null means system default`() {
        // FIX(user-dynamic): null = suivre paramètres système
        val profile = UserProfile(
            uid = testUid,
            locale = null
        )

        assertNull(profile.locale)
    }

    // ============================================
    // Tests de photoUrl
    // ============================================

    @Test
    fun `photoUrl can be null`() {
        // FIX(user-dynamic): Photo optionnelle
        val profile = UserProfile(
            uid = testUid,
            photoUrl = null
        )

        assertNull(profile.photoUrl)
    }

    @Test
    fun `photoUrl can be set to valid URL`() {
        // FIX(user-dynamic): URL de photo valide
        val url = "https://example.com/avatar.jpg"
        val profile = UserProfile(
            uid = testUid,
            photoUrl = url
        )

        assertEquals(url, profile.photoUrl)
    }

    @Test
    fun `photoUrl can be Firebase Storage URL`() {
        // FIX(user-dynamic): Format Firebase Storage
        val firebaseUrl = "gs://ora-wellbeing.appspot.com/avatars/user123.jpg"
        val profile = UserProfile(
            uid = testUid,
            photoUrl = firebaseUrl
        )

        assertEquals(firebaseUrl, profile.photoUrl)
    }

    // ============================================
    // Tests de lastSyncAt
    // ============================================

    @Test
    fun `lastSyncAt can be null for never synced`() {
        // FIX(user-dynamic): Jamais synchronisé
        val profile = UserProfile(
            uid = testUid,
            lastSyncAt = null
        )

        assertNull(profile.lastSyncAt)
    }

    @Test
    fun `lastSyncAt can be set to timestamp`() {
        // FIX(user-dynamic): Timestamp de dernière sync
        val syncTime = System.currentTimeMillis()
        val profile = UserProfile(
            uid = testUid,
            lastSyncAt = syncTime
        )

        assertEquals(syncTime, profile.lastSyncAt)
    }

    // ============================================
    // Tests de scénarios complets
    // ============================================

    @Test
    fun `complete free user profile`() {
        // FIX(user-dynamic): Profil complet utilisateur gratuit
        val profile = UserProfile(
            uid = testUid,
            firstName = "Clara",
            photoUrl = "https://example.com/clara.jpg",
            planTier = "free",
            createdAt = 1704067200000L,
            locale = "fr",
            lastSyncAt = 1704153600000L
        )

        assertEquals(testUid, profile.uid)
        assertEquals("Clara", profile.firstName)
        assertEquals("Clara", profile.displayName())
        assertFalse(profile.isPremium)
        assertEquals("fr", profile.locale)
        assertNotNull(profile.lastSyncAt)
    }

    @Test
    fun `complete premium user profile`() {
        // FIX(user-dynamic): Profil complet utilisateur premium
        val profile = UserProfile(
            uid = testUid,
            firstName = "Jean",
            photoUrl = "https://example.com/jean.jpg",
            planTier = "premium",
            createdAt = 1704067200000L,
            locale = "en",
            lastSyncAt = 1704153600000L
        )

        assertEquals(testUid, profile.uid)
        assertEquals("Jean", profile.firstName)
        assertEquals("Jean", profile.displayName())
        assertTrue(profile.isPremium)
        assertEquals("en", profile.locale)
    }

    @Test
    fun `minimal anonymous profile`() {
        // FIX(user-dynamic): Profil minimal anonyme
        val profile = UserProfile(
            uid = testUid,
            planTier = "free"
        )

        assertEquals(testUid, profile.uid)
        assertNull(profile.firstName)
        assertEquals("Invité", profile.displayName())
        assertFalse(profile.isPremium)
        assertNull(profile.photoUrl)
        assertNull(profile.locale)
    }

    @Test
    fun `profile upgrade from free to premium`() {
        // FIX(user-dynamic): Upgrade plan
        val freeProfile = UserProfile(
            uid = testUid,
            firstName = "Clara",
            planTier = "free",
            createdAt = 1704067200000L
        )

        val premiumProfile = freeProfile.copy(planTier = "premium")

        assertFalse(freeProfile.isPremium)
        assertTrue(premiumProfile.isPremium)
        assertEquals(freeProfile.uid, premiumProfile.uid)
        assertEquals(freeProfile.firstName, premiumProfile.firstName)
        assertEquals(freeProfile.createdAt, premiumProfile.createdAt)
    }

    @Test
    fun `profile locale change`() {
        // FIX(user-dynamic): Changement de langue
        val frenchProfile = UserProfile(
            uid = testUid,
            firstName = "Clara",
            locale = "fr"
        )

        val englishProfile = frenchProfile.copy(locale = "en")

        assertEquals("fr", frenchProfile.locale)
        assertEquals("en", englishProfile.locale)
        assertEquals(frenchProfile.uid, englishProfile.uid)
    }

    @Test
    fun `profile with empty firstName is valid`() {
        // FIX(user-dynamic): String vide est différent de null
        val profile = UserProfile(
            uid = testUid,
            firstName = ""
        )

        assertEquals("", profile.firstName)
        assertEquals("", profile.displayName())
    }
}
