package com.ora.wellbeing.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.concurrent.TimeUnit

// FIX(user-dynamic): Domain model pour les statistiques utilisateur
// Correspond au schéma Firestore stats/{uid} défini dans user_data_contract.yaml
// IMPORTANT: Uses regular class (not data class) for Firestore compatibility
// IMPORTANT: Field names match Firestore camelCase schema (no @PropertyName needed)

/**
 * Statistiques d'activité et progression utilisateur
 * Source de vérité: Firestore collection "stats"
 *
 * @property uid Firebase Auth UID (référence au profil)
 * @property totalMinutes Total minutes de pratique (min: 0, max: 525600)
 * @property sessions Nombre de séances complétées (min: 0)
 * @property streakDays Jours consécutifs de pratique (min: 0, max: 3650)
 * @property lastPracticeAt Timestamp dernière pratique (epoch ms, nullable)
 * @property updatedAt Dernier update des stats (epoch ms)
 */
@IgnoreExtraProperties
class UserStats {
    // uid is stored as both documentId AND field in Firestore
    // Don't use @DocumentId to avoid conflict - get it from the field instead
    var uid: String = ""

    // IMPORTANT: Field names match Firestore schema exactly (camelCase)
    // No @PropertyName needed when names match
    var totalMinutes: Int = 0
    var sessions: Int = 0
    var streakDays: Int = 0
    var lastPracticeAt: Long? = null
    var updatedAt: Long = 0L

    // No-arg constructor required by Firestore
    constructor()

    // Constructor for easy creation
    constructor(
        uid: String,
        totalMinutes: Int = 0,
        sessions: Int = 0,
        streakDays: Int = 0,
        lastPracticeAt: Long? = null,
        updatedAt: Long = System.currentTimeMillis()
    ) : this() {
        this.uid = uid
        this.totalMinutes = totalMinutes
        this.sessions = sessions
        this.streakDays = streakDays
        this.lastPracticeAt = lastPracticeAt
        this.updatedAt = updatedAt
    }

    companion object {
        /**
         * Crée des stats par défaut pour un nouvel utilisateur
         */
        fun createDefault(uid: String): UserStats {
            return UserStats(
                uid = uid,
                totalMinutes = 0,
                sessions = 0,
                streakDays = 0,
                lastPracticeAt = null,
                updatedAt = System.currentTimeMillis()
            )
        }

        /**
         * Calcule si deux timestamps sont des jours consécutifs
         */
        fun areConsecutiveDays(previousTimestamp: Long, currentTimestamp: Long): Boolean {
            val previousDay = TimeUnit.MILLISECONDS.toDays(previousTimestamp)
            val currentDay = TimeUnit.MILLISECONDS.toDays(currentTimestamp)
            return currentDay - previousDay == 1L
        }

        /**
         * Calcule si deux timestamps sont le même jour
         */
        fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
            val day1 = TimeUnit.MILLISECONDS.toDays(timestamp1)
            val day2 = TimeUnit.MILLISECONDS.toDays(timestamp2)
            return day1 == day2
        }
    }

    /**
     * Copy function for immutability pattern
     */
    @Exclude
    fun copy(
        uid: String = this.uid,
        totalMinutes: Int = this.totalMinutes,
        sessions: Int = this.sessions,
        streakDays: Int = this.streakDays,
        lastPracticeAt: Long? = this.lastPracticeAt,
        updatedAt: Long = this.updatedAt
    ): UserStats {
        return UserStats(
            uid = uid,
            totalMinutes = totalMinutes,
            sessions = sessions,
            streakDays = streakDays,
            lastPracticeAt = lastPracticeAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Incrémente les stats après une séance
     */
    @Exclude
    fun incrementSession(durationMinutes: Int, timestamp: Long = System.currentTimeMillis()): UserStats {
        val newStreak = calculateNewStreak(timestamp)

        return copy(
            totalMinutes = totalMinutes + durationMinutes,
            sessions = sessions + 1,
            streakDays = newStreak,
            lastPracticeAt = timestamp,
            updatedAt = timestamp
        )
    }

    /**
     * Calcule le nouveau streak basé sur le dernier practice
     */
    @Exclude
    private fun calculateNewStreak(currentTimestamp: Long): Int {
        if (lastPracticeAt == null) {
            return 1 // Premier jour
        }

        return when {
            isSameDay(lastPracticeAt!!, currentTimestamp) -> {
                // Même jour, on garde le streak actuel
                streakDays
            }
            areConsecutiveDays(lastPracticeAt!!, currentTimestamp) -> {
                // Jour consécutif, on incrémente
                streakDays + 1
            }
            else -> {
                // Streak cassé, on recommence
                1
            }
        }
    }

    /**
     * Réinitialise le streak (si nécessaire pour tests ou admin)
     */
    @Exclude
    fun resetStreak(): UserStats {
        return copy(
            streakDays = 0,
            updatedAt = System.currentTimeMillis()
        )
    }

    // FIX(user-dynamic): Méthodes helper pour l'UI
    /**
     * Formate les minutes totales en format lisible (ex: "2h 30min")
     */
    @Exclude
    fun formatTotalTime(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours == 0 -> "${minutes}min"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}min"
        }
    }

    /**
     * Vérifie si l'utilisateur a pratiqué aujourd'hui
     */
    @Exclude
    fun hasPracticedToday(): Boolean {
        if (lastPracticeAt == null) return false
        return isSameDay(lastPracticeAt!!, System.currentTimeMillis())
    }
}
