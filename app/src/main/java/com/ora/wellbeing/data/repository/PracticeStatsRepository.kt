package com.ora.wellbeing.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.service.EmailNotificationService
import com.ora.wellbeing.domain.model.PracticeSession
import com.ora.wellbeing.domain.model.PracticeStats
import com.ora.wellbeing.domain.model.UserStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour gérer les statistiques de pratique utilisateur
 *
 * Collections Firestore:
 * - users/{uid}/sessions - Historique de toutes les sessions
 * - users/{uid}/practiceStats - Stats agrégées par type de pratique
 * - stats/{uid} - Stats globales utilisateur
 *
 * Email Triggers (Phase 6 - Issue #53):
 * - First session completion: Sends email when user completes their first practice session
 * - Streak milestones: Sends email when user reaches 7, 14, 30, 60, 90, 180, or 365 day streak
 */
@Singleton
class PracticeStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userStatsRepository: UserStatsRepository,
    private val emailNotificationService: EmailNotificationService
) {

    companion object {
        /**
         * Streak milestones that trigger celebration emails
         */
        private val STREAK_MILESTONES = listOf(7, 14, 30, 60, 90, 180, 365)
    }

    /**
     * CoroutineScope for fire-and-forget email operations.
     * Uses SupervisorJob to prevent email failures from affecting other operations.
     */
    private val emailScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun getCurrentUid(): String? = auth.currentUser?.uid

    /**
     * Enregistre une session de pratique complétée
     * Met à jour automatiquement:
     * - L'historique des sessions
     * - Les stats par type de pratique
     * - Les stats globales utilisateur
     * - Envoie un email de première session si applicable (Issue #53)
     */
    suspend fun recordSession(session: PracticeSession): Result<String> {
        return try {
            val uid = getCurrentUid() ?: return Result.failure(Exception("User not authenticated"))
            val sessionWithUid = session.copy(uid = uid)

            // Check if this is the user's first session BEFORE recording
            val sessionCountBefore = getSessionCount(uid)
            val isFirstSession = sessionCountBefore == 0L

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

            // 4. Send first completion email if this was the first session (Issue #53)
            if (isFirstSession) {
                sendFirstCompletionEmailAsync(uid, sessionToSave)
            }

            Result.success(sessionRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Error recording session")
            Result.failure(e)
        }
    }

    /**
     * Sends first completion email asynchronously.
     * Failures are logged but do not affect the session recording.
     */
    private fun sendFirstCompletionEmailAsync(uid: String, session: PracticeSession) {
        emailScope.launch {
            try {
                Timber.d("Sending first completion email for user $uid")
                val success = emailNotificationService.sendFirstCompletionEmail(
                    uid = uid,
                    contentType = session.practiceType,
                    contentTitle = session.contentTitle
                )
                if (success) {
                    Timber.i("First completion email sent successfully for user $uid")
                } else {
                    Timber.w("First completion email was not sent for user $uid (possibly disabled in preferences)")
                }
            } catch (e: Exception) {
                // Silent failure - don't crash the app for email issues
                Timber.e(e, "Failed to send first completion email for user $uid")
            }
        }
    }

    /**
     * Gets the total number of sessions for a user.
     * Used to detect first session for email trigger.
     *
     * @param uid Firebase user ID
     * @return Number of sessions, or 0 if error
     */
    private suspend fun getSessionCount(uid: String): Long {
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("sessions")
                .get()
                .await()
            snapshot.size().toLong()
        } catch (e: Exception) {
            Timber.e(e, "Error getting session count for user $uid")
            0L
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
     * FIX(build-debug-android): Convert session.completedAt (Long) to Date for incrementSession
     * Feature(Issue #53): Sends streak milestone email when applicable
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

            // Store the old streak for milestone comparison
            val oldStreakDays = currentStats.streakDays

            // FIX(build-debug-android): Convert Long timestamp to Date
            val completedAtDate = Date(session.completedAt)

            // Calculer les nouvelles stats
            val updatedStats = currentStats.incrementSession(
                durationMinutes = session.durationMinutes,
                timestamp = completedAtDate // FIX(build-debug-android): Now expects Date instead of Long
            )

            // Sauvegarder
            statsRef.set(updatedStats).await()

            Timber.d("Global stats updated: uid=$uid, sessions=${updatedStats.sessions}, streak=${updatedStats.streakDays}")

            // Check for streak milestone and send email if applicable (Issue #53)
            val newStreakDays = updatedStats.streakDays
            checkAndSendStreakMilestoneEmail(uid, oldStreakDays, newStreakDays)

        } catch (e: Exception) {
            Timber.e(e, "Error updating global stats")
        }
    }

    /**
     * Checks if the user has reached a streak milestone and sends celebration email.
     *
     * A milestone email is sent when:
     * - The new streak is in the STREAK_MILESTONES list (7, 14, 30, 60, 90, 180, 365)
     * - The old streak was less than the new streak (to prevent duplicate emails on same-day practice)
     *
     * @param uid Firebase user ID
     * @param oldStreakDays The streak count before the session
     * @param newStreakDays The streak count after the session
     */
    private fun checkAndSendStreakMilestoneEmail(uid: String, oldStreakDays: Int, newStreakDays: Int) {
        // Only send if we've crossed a milestone threshold
        // This prevents sending emails if the user had a streak, lost it, and is rebuilding
        if (newStreakDays in STREAK_MILESTONES && oldStreakDays < newStreakDays) {
            Timber.d("Streak milestone reached: $newStreakDays days for user $uid")
            sendStreakMilestoneEmailAsync(uid, newStreakDays)
        }
    }

    /**
     * Sends streak milestone email asynchronously.
     * Failures are logged but do not affect the stats update.
     */
    private fun sendStreakMilestoneEmailAsync(uid: String, streakDays: Int) {
        emailScope.launch {
            try {
                Timber.d("Sending streak milestone email for $streakDays days (user: $uid)")
                val success = emailNotificationService.sendStreakMilestoneEmail(
                    uid = uid,
                    streakDays = streakDays
                )
                if (success) {
                    Timber.i("Streak milestone email sent successfully for $streakDays days (user: $uid)")
                } else {
                    Timber.w("Streak milestone email was not sent for user $uid (possibly disabled in preferences)")
                }
            } catch (e: Exception) {
                // Silent failure - don't crash the app for email issues
                Timber.e(e, "Failed to send streak milestone email for user $uid")
            }
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
