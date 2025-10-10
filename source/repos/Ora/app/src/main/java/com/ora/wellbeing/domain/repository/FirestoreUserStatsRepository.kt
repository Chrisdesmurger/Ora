package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

// FIX(user-dynamic): Repository interface pour les statistiques utilisateur Firestore
// Clean Architecture: Domain layer définit le contrat, Data layer l'implémente

/**
 * Repository pour les statistiques utilisateur stockées dans Firestore
 * Collection: stats/{uid}
 *
 * Privacy by design: Toutes les opérations vérifient que request.auth.uid == uid
 * Offline-first: Flow retourne cache si network error
 */
interface FirestoreUserStatsRepository {

    /**
     * Observe les statistiques utilisateur en temps réel
     * Retourne Flow<UserStats?> qui émet:
     * - null si les stats n'existent pas
     * - UserStats si les stats existent
     * - Données cache si offline
     *
     * @param uid Firebase Auth UID
     * @return Flow réactif des statistiques
     */
    fun getUserStats(uid: String): Flow<UserStats?>

    /**
     * Crée de nouvelles statistiques pour un utilisateur
     * Appelé une seule fois lors du premier login (avec profil)
     *
     * @param stats Statistiques à créer (uid doit correspondre à auth.uid)
     * @return Result.success si créé, Result.failure si erreur
     */
    suspend fun createUserStats(stats: UserStats): Result<Unit>

    /**
     * Incrémente les stats après une séance terminée
     * Logique métier:
     * - totalMinutes += durationMinutes
     * - sessions += 1
     * - streakDays = calculateStreak(lastPracticeAt, timestamp)
     * - lastPracticeAt = timestamp
     * - updatedAt = timestamp
     *
     * @param uid Firebase Auth UID
     * @param durationMinutes Durée de la séance en minutes
     * @param timestamp Timestamp de fin de séance (epoch ms)
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun incrementSession(
        uid: String,
        durationMinutes: Int,
        timestamp: Long = System.currentTimeMillis()
    ): Result<Unit>

    /**
     * Réinitialise le streak (rare, admin ou user demande)
     *
     * @param uid Firebase Auth UID
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun resetStreak(uid: String): Result<Unit>

    /**
     * Supprime les statistiques utilisateur (GDPR)
     * Appelé lors de la suppression du compte
     *
     * @param uid Firebase Auth UID
     * @return Result.success si supprimé, Result.failure si erreur
     */
    suspend fun deleteUserStats(uid: String): Result<Unit>
}
