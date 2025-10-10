package com.ora.wellbeing.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Firestore data model pour les statistiques utilisateur
 * Collection: "stats"
 *
 * IMPORTANT: Utilise camelCase pour les noms de champs (pas de @PropertyName)
 * car les repositories utilisent ces noms directement dans les requêtes
 *
 * Structure Firestore:
 * stats/{uid}
 *   - uid: String
 *   - totalMinutes: Number (Long)
 *   - sessions: Number (Long)
 *   - streakDays: Number (Long)
 *   - lastPracticeAt: Number (Long, epoch ms, nullable)
 *   - updatedAt: Number (Long, epoch ms)
 */
@IgnoreExtraProperties
class UserStats() {
    @DocumentId
    var uid: String = ""

    var totalMinutes: Long = 0
    var sessions: Long = 0
    var streakDays: Long = 0
    var lastPracticeAt: Long? = null
    var updatedAt: Long = System.currentTimeMillis()

    /**
     * Convertit vers le domain model
     */
    @Exclude
    fun toDomainModel(): com.ora.wellbeing.domain.model.UserStats {
        return com.ora.wellbeing.domain.model.UserStats(
            uid = uid,
            totalMinutes = totalMinutes.toInt(),
            sessions = sessions.toInt(),
            streakDays = streakDays.toInt(),
            lastPracticeAt = lastPracticeAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Crée depuis le domain model
         */
        fun fromDomainModel(domain: com.ora.wellbeing.domain.model.UserStats): UserStats {
            return UserStats().apply {
                uid = domain.uid
                totalMinutes = domain.totalMinutes.toLong()
                sessions = domain.sessions.toLong()
                streakDays = domain.streakDays.toLong()
                lastPracticeAt = domain.lastPracticeAt
                updatedAt = domain.updatedAt
            }
        }
    }
}
