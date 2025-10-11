package com.ora.wellbeing.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.domain.model.PracticeSession
import com.ora.wellbeing.domain.model.PracticeStats
import com.ora.wellbeing.domain.model.UserStats
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les statistiques de pratique utilisateur
 *
 * Collections Firestore:
 * - users/{uid}/sessions - Historique de toutes les sessions
 * - users/{uid}/practiceStats - Stats agrégées par type de pratique
 * - stats/{uid} - Stats globales utilisateur
 */
@Singleton
class PracticeStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userStatsRepository: UserStatsRepository
) {

    private fun getCurrentUid(): String? = auth.currentUser?.uid

    /**
     * Enregistre une session de pratique complétée
     * Met à jour automatiquement:
     * - L'historique des sessions
     * - Les stats par type de pratique
     * - Les stats globales utilisateur
     */
    suspend fun recordSession(session: PracticeSession): Result<String> {
        return try {
            val uid = getCurrentUid() ?: return Result.failure(Exception("User not authenticated"))
            val sessionWithUid = session.copy(uid = uid)

            // 1. Enregistrer la session dans l'historique
            val sessionRef = if (session.id.isBlank()) {
                firestore.collection("users").document(uid)
                    .collection("sessions")
                    .document()
            } else {
                firestore.collection("users").document(uid)
                    .collection("sessions")
                    .document(session.id)
            }

            val sessionToSave = sessionWithUid.copy(id = sessionRef.id)
            sessionRef.set(sessionToSave).await()

            Timber.d("Session recorded: ${sessionRef.id}, type=${session.practiceType}, duration=${session.durationMinutes}min")

            // 2. Mettre à jour les stats par type de pratique
            updatePracticeStats(uid, sessionToSave)

            // 3. Mettre à jour les stats globales
            updateGlobalStats(uid, sessionToSave)

            Result.success(sessionRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error recording session")
            Result.failure(e)
        }
    }

    /**
     * Met à jour les stats d'un type de pratique spécifique
     */
    private suspend fun updatePracticeStats(uid: String, session: PracticeSession) {
        try {
            val statsRef = firestore.collection("users").document(uid)
                .collection("practiceStats")
                .document(session.practiceType)

            // Lire les stats actuelles
            val snapshot = statsRef.get().await()
            val currentStats = if (snapshot.exists()) {
                snapshot.toObject(PracticeStats::class.java) ?: PracticeStats.createEmpty(session.practiceType)
            } else {
                PracticeStats.createEmpty(session.practiceType)
            }

            // Calculer les nouvelles stats
            val updatedStats = currentStats.addSession(session)

            // Sauvegarder
            statsRef.set(updatedStats).await()

            Timber.d("Practice stats updated: ${session.practiceType}, total=${updatedStats.totalMinutes}min")
        } catch (e: Exception) {
            Timber.e(e, "Error updating practice stats")
        }
    }

    /**
     * Met à jour les stats globales de l'utilisateur
     */
    private suspend fun updateGlobalStats(uid: String, session: PracticeSession) {
        try {
            val statsRef = firestore.collection("stats").document(uid)

            // Lire les stats actuelles
            val snapshot = statsRef.get().await()
            val currentStats = if (snapshot.exists()) {
                snapshot.toObject(UserStats::class.java) ?: UserStats.createDefault(uid)
            } else {
                UserStats.createDefault(uid)
            }

            // Calculer les nouvelles stats
            val updatedStats = currentStats.incrementSession(
                durationMinutes = session.durationMinutes,
                timestamp = session.completedAt
            )

            // Sauvegarder
            statsRef.set(updatedStats).await()

            Timber.d("Global stats updated: uid=$uid, sessions=${updatedStats.sessions}, streak=${updatedStats.streakDays}")
        } catch (e: Exception) {
            Timber.e(e, "Error updating global stats")
        }
    }

    /**
     * Récupère les stats par type de pratique (reactive)
     */
    fun observePracticeStats(uid: String): Flow<List<PracticeStats>> = callbackFlow {
        val listenerRegistration = firestore.collection("users").document(uid)
            .collection("practiceStats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error observing practice stats")
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val stats = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PracticeStats::class.java)
                    }
                    trySend(stats)
                    Timber.d("Practice stats updated: ${stats.size} types")
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Récupère l'historique des sessions (limité aux N dernières)
     */
    suspend fun getRecentSessions(uid: String, limit: Int = 20): Result<List<PracticeSession>> {
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("sessions")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PracticeSession::class.java)
            }

            Timber.d("Retrieved ${sessions.size} recent sessions")
            Result.success(sessions)
        } catch (e: Exception) {
            Timber.e(e, "Error getting recent sessions")
            Result.failure(e)
        }
    }

    /**
     * Récupère la dernière session pour afficher "Dernière activité"
     */
    suspend fun getLastSession(uid: String): Result<PracticeSession?> {
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("sessions")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val session = snapshot.documents.firstOrNull()?.toObject(PracticeSession::class.java)
            Result.success(session)
        } catch (e: Exception) {
            Timber.e(e, "Error getting last session")
            Result.failure(e)
        }
    }

    /**
     * Récupère les sessions d'un type spécifique
     */
    suspend fun getSessionsByType(
        uid: String,
        practiceType: String,
        limit: Int = 50
    ): Result<List<PracticeSession>> {
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("sessions")
                .whereEqualTo("practiceType", practiceType)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PracticeSession::class.java)
            }

            Result.success(sessions)
        } catch (e: Exception) {
            Timber.e(e, "Error getting sessions by type")
            Result.failure(e)
        }
    }
}
