package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ora.wellbeing.domain.model.UserStats
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

// FIX(build-debug-android): Import Date for timestamp handling
// FIX(user-dynamic): Implémentation Firestore du repository UserStats
// Offline-first avec cache persistant, gestion business logic des streaks

@Singleton
class FirestoreUserStatsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreUserStatsRepository {

    companion object {
        private const val COLLECTION_STATS = "stats"
    }

    // FIX(user-dynamic): Observer les stats en temps réel avec snapshotListener
    override fun getUserStats(uid: String): Flow<UserStats?> = callbackFlow {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("getUserStats: Listening to stats $uid")

        val listenerRegistration = firestore.collection(COLLECTION_STATS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        // FIX(user-dynamic): Gestion erreurs selon contrat
                        Timber.e(error, "getUserStats: Erreur snapshot listener")
                        when (error.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                Timber.e("getUserStats: Permission denied - token expiré?")
                            }
                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                Timber.w("getUserStats: Network unavailable - utilise cache")
                            }
                            else -> {
                                Timber.e("getUserStats: Erreur inattendue ${error.code}")
                            }
                        }
                        // Ne pas fermer le flow, laisser le listener actif
                    }
                    snapshot != null -> {
                        val stats = if (snapshot.exists()) {
                            snapshot.toObject(UserStats::class.java)
                        } else {
                            // FIX(user-dynamic): Document not found → retourner null
                            Timber.w("getUserStats: Stats $uid n'existent pas")
                            null
                        }
                        trySend(stats)
                    }
                }
            }

        awaitClose {
            Timber.d("getUserStats: Removing listener for $uid")
            listenerRegistration.remove()
        }
    }

    // FIX(user-dynamic): Créer nouvelles stats
    override suspend fun createUserStats(stats: UserStats): Result<Unit> = try {
        require(stats.uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("createUserStats: Creating stats ${stats.uid}")

        firestore.collection(COLLECTION_STATS)
            .document(stats.uid)
            .set(stats)
            .await()

        Timber.i("createUserStats: Stats ${stats.uid} créées avec succès")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "createUserStats: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "createUserStats: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Incrémenter stats après séance (business logic complète)
    // FIX(build-debug-android): Changed timestamp parameter from Long to Date
    override suspend fun incrementSession(
        uid: String,
        durationMinutes: Int,
        timestamp: Date
    ): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }
        require(durationMinutes > 0) { "Duration doit être > 0" }

        Timber.d("incrementSession: uid=$uid, duration=$durationMinutes, timestamp=$timestamp")

        // FIX(user-dynamic): Transaction pour garantir la cohérence des calculs
        firestore.runTransaction { transaction ->
            val docRef = firestore.collection(COLLECTION_STATS).document(uid)
            val snapshot = transaction.get(docRef)

            if (!snapshot.exists()) {
                // FIX(user-dynamic): Créer stats si n'existent pas
                Timber.w("incrementSession: Stats n'existent pas, création...")
                val newStats = UserStats.createDefault(uid)
                    .incrementSession(durationMinutes, timestamp)
                transaction.set(docRef, newStats)
            } else {
                // FIX(user-dynamic): Mettre à jour stats existantes avec business logic
                val currentStats = snapshot.toObject(UserStats::class.java)
                    ?: throw IllegalStateException("Cannot parse stats for $uid")

                val updatedStats = currentStats.incrementSession(durationMinutes, timestamp)

                Timber.d(
                    "incrementSession: " +
                            "totalMinutes: ${currentStats.totalMinutes} -> ${updatedStats.totalMinutes}, " +
                            "sessions: ${currentStats.sessions} -> ${updatedStats.sessions}, " +
                            "streakDays: ${currentStats.streakDays} -> ${updatedStats.streakDays}"
                )

                transaction.set(docRef, updatedStats)
            }
        }.await()

        Timber.i("incrementSession: Stats $uid mises à jour avec succès")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "incrementSession: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "incrementSession: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Reset streak (admin ou user demande)
    // IMPORTANT: Field names must match Firestore schema (camelCase)
    // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
    override suspend fun resetStreak(uid: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("resetStreak: uid=$uid")

        val updates = mapOf(
            "streakDays" to 0,
            "updatedAt" to Date() // FIX(build-debug-android): Changed from System.currentTimeMillis() to Date()
        )

        firestore.collection(COLLECTION_STATS)
            .document(uid)
            .update(updates)
            .await()

        Timber.i("resetStreak: Streak reset pour $uid")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "resetStreak: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "resetStreak: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Suppression stats (GDPR)
    override suspend fun deleteUserStats(uid: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("deleteUserStats: Deleting stats $uid")

        firestore.collection(COLLECTION_STATS)
            .document(uid)
            .delete()
            .await()

        Timber.i("deleteUserStats: Stats $uid supprimées")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "deleteUserStats: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "deleteUserStats: Erreur inattendue")
        Result.failure(e)
    }
}
