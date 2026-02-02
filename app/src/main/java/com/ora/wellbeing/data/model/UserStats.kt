package com.ora.wellbeing.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// FIX(build-debug-android): Migrated from Long to Date for Firestore Timestamp compatibility
// This is the Firestore data model (data layer) - should match domain model signature

/**
 * Firestore data model pour les statistiques utilisateur
 * Collection: "stats"
 *
 * IMPORTANT: Field names use camelCase to match Firestore schema
 *
 * Structure Firestore:
 * stats/{uid}
 *   - uid: String
 *   - totalMinutes: Number (Long)
 *   - sessions: Number (Long)
 *   - streakDays: Number (Long)
 *   - lastPracticeAt: Timestamp (Firestore Timestamp → Date)
 *   - updatedAt: Timestamp (Firestore Timestamp → Date)
 */
@IgnoreExtraProperties
class UserStats() {
    @DocumentId
    var uid: String = ""

    var totalMinutes: Long = 0
    var sessions: Long = 0
    var streakDays: Long = 0

    // FIX(build-debug-android): Changed from Long? to Date? to handle Firestore Timestamp
    var lastPracticeAt: Date? = null

    // FIX(build-debug-android): Changed from Long to Date? to handle Firestore Timestamp
    @ServerTimestamp
    var updatedAt: Date? = null

    /**
     * Convertit vers le domain model
     * FIX(build-debug-android): Updated to handle Date instead of Long
     */
    @Exclude
    fun toDomainModel(): com.ora.wellbeing.domain.model.UserStats {
        return com.ora.wellbeing.domain.model.UserStats(
            uid = uid,
            totalMinutes = totalMinutes.toInt(),
            sessions = sessions.toInt(),
            streakDays = streakDays.toInt(),
            lastPracticeAt = lastPracticeAt, // FIX(build-debug-android): Now Date? → Date?
            updatedAt = updatedAt // FIX(build-debug-android): Now Date? → Date?
        )
    }

    companion object {
        /**
         * Crée depuis le domain model
         * FIX(build-debug-android): Updated to handle Date instead of Long
         */
        fun fromDomainModel(domain: com.ora.wellbeing.domain.model.UserStats): UserStats {
            return UserStats().apply {
                uid = domain.uid
                totalMinutes = domain.totalMinutes.toLong()
                sessions = domain.sessions.toLong()
                streakDays = domain.streakDays.toLong()
                lastPracticeAt = domain.lastPracticeAt // FIX(build-debug-android): Now Date? → Date?
                updatedAt = domain.updatedAt // FIX(build-debug-android): Now Date? → Date?
            }
        }
    }
}
