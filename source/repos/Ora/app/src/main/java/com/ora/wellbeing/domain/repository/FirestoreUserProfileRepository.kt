package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

// FIX(user-dynamic): Repository interface pour les profils utilisateur Firestore
// Clean Architecture: Domain layer définit le contrat, Data layer l'implémente

/**
 * Repository pour les profils utilisateur stockés dans Firestore
 * Collection: users/{uid}
 *
 * Privacy by design: Toutes les opérations vérifient que request.auth.uid == uid
 * Offline-first: Flow retourne cache si network error
 */
interface FirestoreUserProfileRepository {

    /**
     * Observe le profil utilisateur en temps réel
     * Retourne Flow<UserProfile?> qui émet:
     * - null si le profil n'existe pas
     * - UserProfile si le profil existe
     * - Données cache si offline
     *
     * @param uid Firebase Auth UID
     * @return Flow réactif du profil utilisateur
     */
    fun getUserProfile(uid: String): Flow<UserProfile?>

    /**
     * Crée un nouveau profil utilisateur
     * Appelé une seule fois lors du premier login Firebase Auth
     *
     * @param profile Profil à créer (uid doit correspondre à auth.uid)
     * @return Result.success si créé, Result.failure si erreur
     */
    suspend fun createUserProfile(profile: UserProfile): Result<Unit>

    /**
     * Met à jour le profil utilisateur complet
     *
     * @param profile Profil mis à jour
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>

    /**
     * Met à jour uniquement la locale de l'utilisateur
     *
     * @param uid Firebase Auth UID
     * @param locale Nouvelle locale ("fr" | "en" | null)
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun updateLocale(uid: String, locale: String?): Result<Unit>

    /**
     * Met à jour le plan tier de l'utilisateur (après achat In-App)
     *
     * @param uid Firebase Auth UID
     * @param tier Nouveau tier ("free" | "premium")
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun updatePlanTier(uid: String, tier: String): Result<Unit>

    /**
     * Met à jour la photo de profil
     *
     * @param uid Firebase Auth UID
     * @param url URL de la photo (Firebase Storage ou provider)
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun updatePhotoUrl(uid: String, url: String): Result<Unit>

    /**
     * Met à jour les informations du profil groupé (onboarding)
     * Utilisé par l'écran profile_group de l'onboarding
     *
     * @param uid Firebase Auth UID
     * @param firstName Prénom de l'utilisateur
     * @param birthDate Date de naissance (format DD/MM/YYYY)
     * @param gender Genre ("male" | "female" | "non_binary" | "prefer_not_to_say")
     * @return Result.success si mis à jour, Result.failure si erreur
     */
    suspend fun updateProfileGroup(
        uid: String,
        firstName: String?,
        birthDate: String?,
        gender: String?
    ): Result<Unit>

    /**
     * Supprime le profil utilisateur (GDPR)
     * Appelé lors de la suppression du compte
     *
     * @param uid Firebase Auth UID
     * @return Result.success si supprimé, Result.failure si erreur
     */
    suspend fun deleteUserProfile(uid: String): Result<Unit>
}
