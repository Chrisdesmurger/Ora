package com.ora.wellbeing.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.concurrent.TimeUnit

// FIX(build-debug-android): Use Date instead of Long for Firestore Timestamp compatibility
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
 * @property lastPracticeAt Timestamp dernière pratique (Firestore Timestamp → Date)
 * @property updatedAt Dernier update des stats (Firestore Timestamp → Date)
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

    // FIX(build-debug-android): Changed from Long? to Date? to handle Firestore Timestamp
    var lastPracticeAt: Date? = null

    // FIX(build-debug-android): Changed from Long to Date? to handle Firestore Timestamp
    @ServerTimestamp
    var updatedAt: Date? = null

    // No-arg constructor required by Firestore
    constructor()

    // Constructor for easy creation
    constructor(
        uid: String,
        totalMinutes: Int = 0,
        sessions: Int = 0,
        streakDays: Int = 0,
        lastPracticeAt: Date? = null, // FIX(build-debug-android): Changed from Long? to Date?
        updatedAt: Date? = null // FIX(build-debug-android): Changed from Long to Date?
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
                updatedAt = Date() // FIX(build-debug-android): Changed from System.currentTimeMillis() to Date()
            )
        }

        /**
         * Calcule si deux timestamps sont des jours consécutifs
         * FIX(build-debug-android): Updated to work with Date instead of Long
         */
        fun areConsecutiveDays(previousDate: Date, currentDate: Date): Boolean {
            val previousDay = TimeUnit.MILLISECONDS.toDays(previousDate.time)
            val currentDay = TimeUnit.MILLISECONDS.toDays(currentDate.time)
            return currentDay - previousDay == 1L
        }

        /**
         * Calcule si deux timestamps sont le même jour
         * FIX(build-debug-android): Updated to work with Date instead of Long
         */
        fun isSameDay(date1: Date, date2: Date): Boolean {
            val day1 = TimeUnit.MILLISECONDS.toDays(date1.time)
            val day2 = TimeUnit.MILLISECONDS.toDays(date2.time)
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
        lastPracticeAt: Date? = this.lastPracticeAt, // FIX(build-debug-android): Changed from Long? to Date?
        updatedAt: Date? = this.updatedAt // FIX(build-debug-android): Changed from Long to Date?
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
     * FIX(build-debug-android): Updated to work with Date instead of Long
     */
    @Exclude
    fun incrementSession(durationMinutes: Int, timestamp: Date = Date()): UserStats {
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
     * FIX(build-debug-android): Updated to work with Date instead of Long
     */
    @Exclude
    private fun calculateNewStreak(currentDate: Date): Int {
        if (lastPracticeAt == null) {
            return 1 // Premier jour
        }

        return when {
            isSameDay(lastPracticeAt!!, currentDate) -> {
                // Même jour, on garde le streak actuel
                streakDays
            }
            areConsecutiveDays(lastPracticeAt!!, currentDate) -> {
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
            updatedAt = Date() // FIX(build-debug-android): Changed from System.currentTimeMillis() to Date()
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
     * FIX(build-debug-android): Updated to work with Date instead of Long
     */
    @Exclude
    fun hasPracticedToday(): Boolean {
        if (lastPracticeAt == null) return false
        return isSameDay(lastPracticeAt!!, Date())
    }
}
