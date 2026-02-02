package com.ora.wellbeing.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Statistiques agrégées par type de pratique
 * Collection Firestore: "users/{uid}/practiceStats"
 *
 * Document ID = practiceType (yoga, meditation, pilates, breathing)
 *
 * Permet d'afficher:
 * - Temps total par pratique
 * - Nombre de sessions par pratique
 * - Progression dans le temps
 */
@IgnoreExtraProperties
class PracticeStats {

    @DocumentId
    var practiceType: String = "" // yoga, meditation, pilates, breathing

    // Stats globales
    var totalMinutes: Int = 0
    var totalSessions: Int = 0
    var completedSessions: Int = 0

    // Stats temporelles
    var minutesThisWeek: Int = 0
    var minutesThisMonth: Int = 0
    var sessionsThisWeek: Int = 0
    var sessionsThisMonth: Int = 0

    // Dernière pratique
    var lastPracticeAt: Long? = null
    var lastPracticeTitle: String? = null

    // Progression
    var favoriteLevel: String? = null // beginner/intermediate/advanced le plus pratiqué
    var averageDurationMinutes: Int = 0

    // Timestamps
    var updatedAt: Long = System.currentTimeMillis()

    // No-arg constructor for Firestore
    constructor()

    constructor(
        practiceType: String,
        totalMinutes: Int = 0,
        totalSessions: Int = 0,
        completedSessions: Int = 0,
        minutesThisWeek: Int = 0,
        minutesThisMonth: Int = 0,
        sessionsThisWeek: Int = 0,
        sessionsThisMonth: Int = 0,
        lastPracticeAt: Long? = null,
        lastPracticeTitle: String? = null,
        favoriteLevel: String? = null,
        averageDurationMinutes: Int = 0,
        updatedAt: Long = System.currentTimeMillis()
    ) : this() {
        this.practiceType = practiceType
        this.totalMinutes = totalMinutes
        this.totalSessions = totalSessions
        this.completedSessions = completedSessions
        this.minutesThisWeek = minutesThisWeek
        this.minutesThisMonth = minutesThisMonth
        this.sessionsThisWeek = sessionsThisWeek
        this.sessionsThisMonth = sessionsThisMonth
        this.lastPracticeAt = lastPracticeAt
        this.lastPracticeTitle = lastPracticeTitle
        this.favoriteLevel = favoriteLevel
        this.averageDurationMinutes = averageDurationMinutes
        this.updatedAt = updatedAt
    }

    @Exclude
    fun copy(
        practiceType: String = this.practiceType,
        totalMinutes: Int = this.totalMinutes,
        totalSessions: Int = this.totalSessions,
        completedSessions: Int = this.completedSessions,
        minutesThisWeek: Int = this.minutesThisWeek,
        minutesThisMonth: Int = this.minutesThisMonth,
        sessionsThisWeek: Int = this.sessionsThisWeek,
        sessionsThisMonth: Int = this.sessionsThisMonth,
        lastPracticeAt: Long? = this.lastPracticeAt,
        lastPracticeTitle: String? = this.lastPracticeTitle,
        favoriteLevel: String? = this.favoriteLevel,
        averageDurationMinutes: Int = this.averageDurationMinutes,
        updatedAt: Long = this.updatedAt
    ): PracticeStats {
        return PracticeStats(
            practiceType, totalMinutes, totalSessions, completedSessions,
            minutesThisWeek, minutesThisMonth, sessionsThisWeek, sessionsThisMonth,
            lastPracticeAt, lastPracticeTitle, favoriteLevel, averageDurationMinutes,
            updatedAt
        )
    }

    /**
     * Met à jour les stats après une nouvelle session
     */
    @Exclude
    fun addSession(session: PracticeSession, now: Long = System.currentTimeMillis()): PracticeStats {
        val isThisWeek = isWithinDays(session.completedAt, now, 7)
        val isThisMonth = isWithinDays(session.completedAt, now, 30)

        return copy(
            totalMinutes = totalMinutes + session.durationMinutes,
            totalSessions = totalSessions + 1,
            completedSessions = if (session.completed) completedSessions + 1 else completedSessions,
            minutesThisWeek = if (isThisWeek) minutesThisWeek + session.durationMinutes else minutesThisWeek,
            minutesThisMonth = if (isThisMonth) minutesThisMonth + session.durationMinutes else minutesThisMonth,
            sessionsThisWeek = if (isThisWeek) sessionsThisWeek + 1 else sessionsThisWeek,
            sessionsThisMonth = if (isThisMonth) sessionsThisMonth + 1 else sessionsThisMonth,
            lastPracticeAt = session.completedAt,
            lastPracticeTitle = session.contentTitle,
            averageDurationMinutes = (totalMinutes + session.durationMinutes) / (totalSessions + 1),
            updatedAt = now
        )
    }

    /**
     * Formate le temps total
     */
    @Exclude
    fun formatTotalTime(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0 -> "${minutes}min"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h${minutes.toString().padStart(2, '0')}"
        }
    }

    /**
     * Formate le temps du mois
     */
    @Exclude
    fun formatMonthTime(): String {
        val hours = minutesThisMonth / 60
        val minutes = minutesThisMonth % 60
        return when {
            hours == 0 -> "${minutes}min ce mois-ci"
            minutes == 0 -> "${hours}h ce mois-ci"
            else -> "${hours}h${minutes.toString().padStart(2, '0')} ce mois-ci"
        }
    }

    companion object {
        /**
         * Crée des stats vides pour un type de pratique
         */
        fun createEmpty(practiceType: String): PracticeStats {
            return PracticeStats(practiceType = practiceType)
        }

        /**
         * Vérifie si un timestamp est dans les X derniers jours
         */
        private fun isWithinDays(timestamp: Long, now: Long, days: Int): Boolean {
            val diffMillis = now - timestamp
            val diffDays = diffMillis / (24 * 60 * 60 * 1000)
            return diffDays < days
        }
    }
}
