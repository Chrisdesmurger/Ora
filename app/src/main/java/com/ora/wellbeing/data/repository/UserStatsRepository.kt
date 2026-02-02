package com.ora.wellbeing.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ora.wellbeing.data.model.UserStats
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository Firestore pour les statistiques utilisateurs
 * Collection: "stats"
 *
 * Retourne des domain models (com.ora.wellbeing.domain.model.UserStats)
 */
@Singleton
class UserStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION_USER_STATS = "stats"
    }

    /**
     * Écoute les changements des stats en temps réel
     * Retourne le domain model UserStats
     */
    fun getUserStatsFlow(uid: String): Flow<com.ora.wellbeing.domain.model.UserStats?> = callbackFlow {
        Timber.d("UserStatsRepository: Setting up listener for uid=$uid")

        val listener: ListenerRegistration = firestore
            .collection(COLLECTION_USER_STATS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "UserStatsRepository: Error listening to stats")
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val dataStats = snapshot.toObject(UserStats::class.java)
                        val domainStats = dataStats?.toDomainModel()
                        Timber.d("UserStatsRepository: Stats updated: ${domainStats?.sessions} sessions")
                        trySend(domainStats)
                    } catch (e: Exception) {
                        Timber.e(e, "UserStatsRepository: Error mapping stats")
                        trySend(null)
                    }
                } else {
                    Timber.d("UserStatsRepository: Stats don't exist yet")
                    trySend(null)
                }
            }

        awaitClose {
            Timber.d("UserStatsRepository: Removing listener for uid=$uid")
            listener.remove()
        }
    }

    /**
     * Récupère les stats une seule fois
     */
    suspend fun getUserStats(uid: String): Result<com.ora.wellbeing.domain.model.UserStats?> {
        return try {
            Timber.d("UserStatsRepository: Fetching stats for uid=$uid")
            val snapshot = firestore
                .collection(COLLECTION_USER_STATS)
                .document(uid)
                .get()
                .await()

            val dataStats = snapshot.toObject(UserStats::class.java)
            val domainStats = dataStats?.toDomainModel()
            Timber.d("UserStatsRepository: Stats fetched: ${domainStats?.sessions} sessions")
            Result.success(domainStats)
        } catch (e: Exception) {
            Timber.e(e, "UserStatsRepository: Error fetching stats")
            Result.failure(e)
        }
    }

    /**
     * Crée ou remplace les stats
     */
    suspend fun setUserStats(stats: com.ora.wellbeing.domain.model.UserStats): Result<Unit> {
        return try {
            Timber.d("UserStatsRepository: Setting stats for uid=${stats.uid}")
            val dataStats = UserStats.fromDomainModel(stats)
            firestore
                .collection(COLLECTION_USER_STATS)
                .document(stats.uid)
                .set(dataStats)
                .await()

            Timber.d("UserStatsRepository: Stats set successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserStatsRepository: Error setting stats")
            Result.failure(e)
        }
    }

    /**
     * Met à jour des champs spécifiques
     */
    suspend fun updateUserStats(uid: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            Timber.d("UserStatsRepository: Updating stats for uid=$uid")
            firestore
                .collection(COLLECTION_USER_STATS)
                .document(uid)
                .update(updates)
                .await()

            Timber.d("UserStatsRepository: Stats updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserStatsRepository: Error updating stats")
            Result.failure(e)
        }
    }

    /**
     * Incrémente le nombre de sessions après une pratique
     */
    suspend fun incrementSession(uid: String, durationMinutes: Int): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "sessions" to FieldValue.increment(1),
                "totalMinutes" to FieldValue.increment(durationMinutes.toLong()),
                "lastPracticeAt" to now,
                "updatedAt" to now
            )
            updateUserStats(uid, updates)
        } catch (e: Exception) {
            Timber.e(e, "UserStatsRepository: Error incrementing session")
            Result.failure(e)
        }
    }

    /**
     * Met à jour le streak
     */
    suspend fun updateStreak(uid: String, newStreak: Int): Result<Unit> {
        return updateUserStats(
            uid, mapOf(
                "streakDays" to newStreak,
                "updatedAt" to System.currentTimeMillis()
            )
        )
    }

    /**
     * Supprime les stats
     */
    suspend fun deleteUserStats(uid: String): Result<Unit> {
        return try {
            Timber.d("UserStatsRepository: Deleting stats for uid=$uid")
            firestore
                .collection(COLLECTION_USER_STATS)
                .document(uid)
                .delete()
                .await()

            Timber.d("UserStatsRepository: Stats deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserStatsRepository: Error deleting stats")
            Result.failure(e)
        }
    }
}
