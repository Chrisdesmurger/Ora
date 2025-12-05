package com.ora.wellbeing.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ora.wellbeing.data.model.PlanTier
import com.ora.wellbeing.data.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX(user-dynamic): Repository Firestore pour les profils utilisateurs
 * Collection: "user_profiles"
 */
@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        // FIX(user-dynamic): Nom de collection doit correspondre aux règles Firestore
        private const val COLLECTION_USER_PROFILES = "users"
    }

    /**
     * Écoute les changements du profil utilisateur en temps réel
     */
    fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        Timber.d("UserProfileRepository: Setting up listener for uid=$uid")

        val listener: ListenerRegistration = firestore
            .collection(COLLECTION_USER_PROFILES)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "UserProfileRepository: Error listening to profile")
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val profile = snapshot.toObject(UserProfile::class.java)
                    Timber.d("UserProfileRepository: Profile updated: ${profile?.firstName}")
                    trySend(profile)
                } else {
                    Timber.d("UserProfileRepository: Profile doesn't exist yet")
                    trySend(null)
                }
            }

        awaitClose {
            Timber.d("UserProfileRepository: Removing listener for uid=$uid")
            listener.remove()
        }
    }

    /**
     * Récupère le profil une seule fois
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            Timber.d("UserProfileRepository: Fetching profile for uid=$uid")
            val snapshot = firestore
                .collection(COLLECTION_USER_PROFILES)
                .document(uid)
                .get()
                .await()

            val profile = snapshot.toObject(UserProfile::class.java)
            Timber.d("UserProfileRepository: Profile fetched: ${profile?.firstName}")
            Result.success(profile)
        } catch (e: Exception) {
            Timber.e(e, "UserProfileRepository: Error fetching profile")
            Result.failure(e)
        }
    }

    /**
     * Crée ou met à jour le profil complet
     */
    suspend fun setUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            Timber.d("UserProfileRepository: Setting profile for uid=${profile.uid}")
            firestore
                .collection(COLLECTION_USER_PROFILES)
                .document(profile.uid)
                .set(profile)
                .await()

            Timber.d("UserProfileRepository: Profile set successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserProfileRepository: Error setting profile")
            Result.failure(e)
        }
    }

    /**
     * Met à jour des champs spécifiques du profil
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            Timber.d("UserProfileRepository: Updating profile for uid=$uid with $updates")
            firestore
                .collection(COLLECTION_USER_PROFILES)
                .document(uid)
                .update(updates)
                .await()

            Timber.d("UserProfileRepository: Profile updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserProfileRepository: Error updating profile")
            Result.failure(e)
        }
    }

    /**
     * Met à jour le prénom
     */
    suspend fun updateFirstName(uid: String, firstName: String): Result<Unit> {
        return updateUserProfile(uid, mapOf("first_name" to firstName))
    }

    /**
     * Met à jour le nom
     */
    suspend fun updateLastName(uid: String, lastName: String): Result<Unit> {
        return updateUserProfile(uid, mapOf("last_name" to lastName))
    }

    /**
     * Met à jour la photo de profil
     */
    suspend fun updatePhotoUrl(uid: String, photoUrl: String?): Result<Unit> {
        return updateUserProfile(uid, mapOf("photo_url" to photoUrl))
    }

    /**
     * Met à jour le motto
     */
    suspend fun updateMotto(uid: String, motto: String): Result<Unit> {
        return updateUserProfile(uid, mapOf("motto" to motto))
    }

    /**
     * Met à jour la date de naissance
     */
    suspend fun updateBirthDate(uid: String, birthDate: String): Result<Unit> {
        return updateUserProfile(uid, mapOf("birth_date" to birthDate))
    }

    /**
     * Met à jour le genre
     */
    suspend fun updateGender(uid: String, gender: String): Result<Unit> {
        return updateUserProfile(uid, mapOf("gender" to gender))
    }

    /**
     * Met à jour les informations du profil groupé (onboarding)
     * Utilisé par l'écran profile_group de l'onboarding
     */
    suspend fun updateProfileGroup(
        uid: String,
        firstName: String?,
        birthDate: String?,
        gender: String?
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any?>()

            firstName?.let { updates["first_name"] = it }
            birthDate?.let { updates["birth_date"] = it }
            gender?.let { updates["gender"] = it }

            if (updates.isEmpty()) {
                Timber.w("UserProfileRepository: No profile group fields to update")
                return Result.success(Unit)
            }

            Timber.d("UserProfileRepository: Updating profile group for uid=$uid")
            updateUserProfile(uid, updates)
        } catch (e: Exception) {
            Timber.e(e, "UserProfileRepository: Error updating profile group")
            Result.failure(e)
        }
    }

    /**
     * Met à jour le plan tier (Free, Premium, Lifetime)
     */
    suspend fun updatePlanTier(uid: String, planTier: PlanTier): Result<Unit> {
        return updateUserProfile(uid, mapOf("plan_tier" to planTier.name))
    }

    /**
     * Supprime le profil
     */
    suspend fun deleteUserProfile(uid: String): Result<Unit> {
        return try {
            Timber.d("UserProfileRepository: Deleting profile for uid=$uid")
            firestore
                .collection(COLLECTION_USER_PROFILES)
                .document(uid)
                .delete()
                .await()

            Timber.d("UserProfileRepository: Profile deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "UserProfileRepository: Error deleting profile")
            Result.failure(e)
        }
    }
}
