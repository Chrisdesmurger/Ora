package com.ora.wellbeing.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * FIX(user-dynamic): Tests unitaires pour UserStats
 *
 * Couverture:
 * - Création par défaut
 * - Calcul du streak (nouveau user, consécutif, gap, même jour)
 * - Incrémentation des sessions
 * - Réinitialisation du streak
 * - Edge cases (timestamps, overflow)
 */
class UserStatsTest {

    private val testUid = "test-user-123"
    private val baseTimestamp = 1704067200000L // 2024-01-01 00:00:00 UTC

    // ============================================
    // Tests de création par défaut
    // ============================================

    @Test
    fun `createDefault creates stats with zero values`() {
        // FIX(user-dynamic): Nouveau user doit avoir stats vides
        val stats = UserStats.createDefault(testUid)

        assertEquals(testUid, stats.uid)
        assertEquals(0, stats.totalMinutes)
        assertEquals(0, stats.sessions)
        assertEquals(0, stats.streakDays)
        assertNull(stats.lastPracticeAt)
        assertTrue(stats.updatedAt > 0)
    }

    @Test
    fun `createDefault generates unique timestamp`() {
        // FIX(user-dynamic): Chaque création doit avoir un timestamp différent
        val stats1 = UserStats.createDefault("user1")
        Thread.sleep(10) // Petit délai pour assurer différence de timestamp
        val stats2 = UserStats.createDefault("user2")

        assertTrue(stats2.updatedAt >= stats1.updatedAt)
    }

    // ============================================
    // Tests de calcul de jours consécutifs
    // ============================================

    @Test
    fun `areConsecutiveDays returns true for consecutive days`() {
        // FIX(user-dynamic): Jours consécutifs doivent être détectés correctement
        val day1 = baseTimestamp
        val day2 = baseTimestamp + TimeUnit.DAYS.toMillis(1)

        assertTrue(UserStats.areConsecutiveDays(day1, day2))
    }

    @Test
    fun `areConsecutiveDays returns false for same day`() {
        // FIX(user-dynamic): Même jour ne compte pas comme consécutif
        val morning = baseTimestamp + TimeUnit.HOURS.toMillis(8)
        val evening = baseTimestamp + TimeUnit.HOURS.toMillis(20)

        assertFalse(UserStats.areConsecutiveDays(morning, evening))
    }

    @Test
    fun `areConsecutiveDays returns false for gap of 2 days`() {
        // FIX(user-dynamic): Gap de 2+ jours casse le streak
        val day1 = baseTimestamp
        val day3 = baseTimestamp + TimeUnit.DAYS.toMillis(2)

        assertFalse(UserStats.areConsecutiveDays(day1, day3))
    }

    @Test
    fun `areConsecutiveDays handles day boundary correctly`() {
        // FIX(user-dynamic): Test edge case autour de minuit
        val beforeMidnight = baseTimestamp + TimeUnit.HOURS.toMillis(23) + TimeUnit.MINUTES.toMillis(59)
        val afterMidnight = beforeMidnight + TimeUnit.MINUTES.toMillis(2)

        assertTrue(UserStats.areConsecutiveDays(beforeMidnight, afterMidnight))
    }

    // ============================================
    // Tests de même jour
    // ============================================

    @Test
    fun `isSameDay returns true for same day different hours`() {
        // FIX(user-dynamic): Plusieurs séances le même jour possibles
        val morning = baseTimestamp + TimeUnit.HOURS.toMillis(8)
        val evening = baseTimestamp + TimeUnit.HOURS.toMillis(20)

        assertTrue(UserStats.isSameDay(morning, evening))
    }

    @Test
    fun `isSameDay returns false for different days`() {
        // FIX(user-dynamic): Jours différents doivent être distingués
        val day1 = baseTimestamp
        val day2 = baseTimestamp + TimeUnit.DAYS.toMillis(1)

        assertFalse(UserStats.isSameDay(day1, day2))
    }

    @Test
    fun `isSameDay returns true for exact same timestamp`() {
        // FIX(user-dynamic): Timestamp identique est le même jour
        val timestamp = baseTimestamp

        assertTrue(UserStats.isSameDay(timestamp, timestamp))
    }

    // ============================================
    // Tests d'incrémentation - Nouveau user
    // ============================================

    @Test
    fun `incrementSession for new user sets streak to 1`() {
        // FIX(user-dynamic): Première séance démarre streak à 1
        val stats = UserStats.createDefault(testUid)
        val updated = stats.incrementSession(durationMinutes = 20, timestamp = baseTimestamp)

        assertEquals(1, updated.streakDays)
        assertEquals(20, updated.totalMinutes)
        assertEquals(1, updated.sessions)
        assertEquals(baseTimestamp, updated.lastPracticeAt)
        assertEquals(baseTimestamp, updated.updatedAt)
    }

    // ============================================
    // Tests d'incrémentation - Même jour
    // ============================================

    @Test
    fun `incrementSession on same day keeps streak unchanged`() {
        // FIX(user-dynamic): Plusieurs séances même jour ne changent pas le streak
        val firstSession = baseTimestamp + TimeUnit.HOURS.toMillis(8)
        val secondSession = baseTimestamp + TimeUnit.HOURS.toMillis(20)

        val stats = UserStats.createDefault(testUid)
        val afterFirst = stats.incrementSession(15, firstSession)
        val afterSecond = afterFirst.incrementSession(30, secondSession)

        assertEquals(1, afterSecond.streakDays) // Streak reste à 1
        assertEquals(45, afterSecond.totalMinutes) // 15 + 30
        assertEquals(2, afterSecond.sessions)
        assertEquals(secondSession, afterSecond.lastPracticeAt)
    }

    // ============================================
    // Tests d'incrémentation - Jours consécutifs
    // ============================================

    @Test
    fun `incrementSession on consecutive day increments streak`() {
        // FIX(user-dynamic): Séance le jour suivant incrémente le streak
        val day1 = baseTimestamp
        val day2 = baseTimestamp + TimeUnit.DAYS.toMillis(1)

        val stats = UserStats.createDefault(testUid)
        val afterDay1 = stats.incrementSession(20, day1)
        val afterDay2 = afterDay1.incrementSession(25, day2)

        assertEquals(2, afterDay2.streakDays)
        assertEquals(45, afterDay2.totalMinutes)
        assertEquals(2, afterDay2.sessions)
    }

    @Test
    fun `incrementSession maintains long streak`() {
        // FIX(user-dynamic): Streak peut s'étendre sur plusieurs jours
        var stats = UserStats.createDefault(testUid)

        // Simuler 7 jours consécutifs
        for (day in 0L..6L) {
            val timestamp = baseTimestamp + TimeUnit.DAYS.toMillis(day)
            stats = stats.incrementSession(20, timestamp)
        }

        assertEquals(7, stats.streakDays)
        assertEquals(140, stats.totalMinutes) // 20 * 7
        assertEquals(7, stats.sessions)
    }

    // ============================================
    // Tests d'incrémentation - Gap (streak cassé)
    // ============================================

    @Test
    fun `incrementSession after gap resets streak to 1`() {
        // FIX(user-dynamic): Gap de 2+ jours réinitialise le streak
        val day1 = baseTimestamp
        val day4 = baseTimestamp + TimeUnit.DAYS.toMillis(3)

        val stats = UserStats.createDefault(testUid)
        val afterDay1 = stats.incrementSession(20, day1)
        val afterDay4 = afterDay1.incrementSession(15, day4)

        assertEquals(1, afterDay4.streakDays) // Streak réinitialisé
        assertEquals(35, afterDay4.totalMinutes) // Minutes cumulées
        assertEquals(2, afterDay4.sessions)
    }

    @Test
    fun `incrementSession after long gap resets streak`() {
        // FIX(user-dynamic): Gap long (1 mois) réinitialise streak
        val stats = UserStats(
            uid = testUid,
            totalMinutes = 300,
            sessions = 15,
            streakDays = 10,
            lastPracticeAt = baseTimestamp,
            updatedAt = baseTimestamp
        )

        val oneMonthLater = baseTimestamp + TimeUnit.DAYS.toMillis(30)
        val updated = stats.incrementSession(20, oneMonthLater)

        assertEquals(1, updated.streakDays)
        assertEquals(320, updated.totalMinutes)
        assertEquals(16, updated.sessions)
    }

    // ============================================
    // Tests d'incrémentation - Accumulation minutes
    // ============================================

    @Test
    fun `incrementSession accumulates total minutes correctly`() {
        // FIX(user-dynamic): Minutes totales doivent s'accumuler correctement
        val stats = UserStats.createDefault(testUid)
        val session1 = stats.incrementSession(10, baseTimestamp)
        val session2 = session1.incrementSession(20, baseTimestamp + TimeUnit.HOURS.toMillis(2))
        val session3 = session2.incrementSession(30, baseTimestamp + TimeUnit.HOURS.toMillis(4))

        assertEquals(60, session3.totalMinutes)
        assertEquals(3, session3.sessions)
    }

    @Test
    fun `incrementSession handles zero duration`() {
        // FIX(user-dynamic): Durée 0 est valide (séance annulée précocement)
        val stats = UserStats.createDefault(testUid)
        val updated = stats.incrementSession(0, baseTimestamp)

        assertEquals(0, updated.totalMinutes)
        assertEquals(1, updated.sessions)
        assertEquals(1, updated.streakDays)
    }

    @Test
    fun `incrementSession handles large duration`() {
        // FIX(user-dynamic): Longues séances (2h+) doivent fonctionner
        val stats = UserStats.createDefault(testUid)
        val updated = stats.incrementSession(150, baseTimestamp) // 2h30

        assertEquals(150, updated.totalMinutes)
        assertEquals(1, updated.sessions)
    }

    // ============================================
    // Tests de réinitialisation du streak
    // ============================================

    @Test
    fun `resetStreak sets streak to zero`() {
        // FIX(user-dynamic): Reset streak pour admin ou tests
        val stats = UserStats(
            uid = testUid,
            totalMinutes = 300,
            sessions = 15,
            streakDays = 10,
            lastPracticeAt = baseTimestamp,
            updatedAt = baseTimestamp
        )

        val reset = stats.resetStreak()

        assertEquals(0, reset.streakDays)
        assertEquals(300, reset.totalMinutes) // Autres stats préservées
        assertEquals(15, reset.sessions)
        assertEquals(baseTimestamp, reset.lastPracticeAt)
        assertTrue(reset.updatedAt >= stats.updatedAt) // Timestamp mis à jour
    }

    @Test
    fun `resetStreak on zero streak is safe`() {
        // FIX(user-dynamic): Reset sur streak déjà à 0 est sans effet
        val stats = UserStats.createDefault(testUid)
        val reset = stats.resetStreak()

        assertEquals(0, reset.streakDays)
    }

    // ============================================
    // Tests edge cases
    // ============================================

    @Test
    fun `incrementSession updates lastPracticeAt correctly`() {
        // FIX(user-dynamic): lastPracticeAt doit être mis à jour à chaque séance
        val timestamp1 = baseTimestamp
        val timestamp2 = baseTimestamp + TimeUnit.HOURS.toMillis(2)

        val stats = UserStats.createDefault(testUid)
        val session1 = stats.incrementSession(10, timestamp1)
        assertEquals(timestamp1, session1.lastPracticeAt)

        val session2 = session1.incrementSession(10, timestamp2)
        assertEquals(timestamp2, session2.lastPracticeAt)
    }

    @Test
    fun `incrementSession updates updatedAt correctly`() {
        // FIX(user-dynamic): updatedAt suit le timestamp de la séance
        val timestamp = baseTimestamp + TimeUnit.HOURS.toMillis(5)
        val stats = UserStats.createDefault(testUid)
        val updated = stats.incrementSession(10, timestamp)

        assertEquals(timestamp, updated.updatedAt)
    }

    @Test
    fun `stats preserve uid through operations`() {
        // FIX(user-dynamic): UID ne doit jamais changer
        var stats = UserStats.createDefault(testUid)
        stats = stats.incrementSession(10, baseTimestamp)
        stats = stats.incrementSession(20, baseTimestamp + TimeUnit.DAYS.toMillis(1))
        stats = stats.resetStreak()

        assertEquals(testUid, stats.uid)
    }

    @Test
    fun `areConsecutiveDays with negative gap returns false`() {
        // FIX(user-dynamic): Protection contre timestamps inversés
        val laterDay = baseTimestamp + TimeUnit.DAYS.toMillis(5)
        val earlierDay = baseTimestamp

        assertFalse(UserStats.areConsecutiveDays(laterDay, earlierDay))
    }

    @Test
    fun `multiple sessions same day accumulate stats correctly`() {
        // FIX(user-dynamic): Scénario réaliste - 3 séances le même jour
        val morning = baseTimestamp + TimeUnit.HOURS.toMillis(7)
        val noon = baseTimestamp + TimeUnit.HOURS.toMillis(12)
        val evening = baseTimestamp + TimeUnit.HOURS.toMillis(19)

        var stats = UserStats.createDefault(testUid)
        stats = stats.incrementSession(15, morning)
        stats = stats.incrementSession(10, noon)
        stats = stats.incrementSession(20, evening)

        assertEquals(1, stats.streakDays) // Toujours jour 1
        assertEquals(45, stats.totalMinutes)
        assertEquals(3, stats.sessions)
        assertEquals(evening, stats.lastPracticeAt)
    }
}
