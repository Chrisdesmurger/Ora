package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

// FIX(build-debug-android): Import Date for timestamp handling
// FIX(user-dynamic): Implémentation Firestore du repository UserProfile
// Offline-first avec cache persistant, gestion complète des erreurs

@Singleton
class FirestoreUserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreUserProfileRepository {

    companion object {
        private const val COLLECTION_USERS = "users"
    }

    // FIX(user-dynamic): Observer le profil en temps réel avec snapshotListener
    override fun getUserProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("getUserProfile: Listening to profile $uid")

        val listenerRegistration = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        // FIX(user-dynamic): Gestion erreurs selon contrat
                        Timber.e(error, "getUserProfile: Erreur snapshot listener")
                        when (error.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                Timber.e("getUserProfile: Permission denied - token expiré?")
                                // UI devrait logout user
                            }
                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                Timber.w("getUserProfile: Network unavailable - utilise cache")
                                // UI affiche indicateur offline, listener continuera avec cache
                            }
                            else -> {
                                Timber.e("getUserProfile: Erreur inattendue ${error.code}")
                            }
                        }
                        // Ne pas fermer le flow, laisser le listener actif pour récupérer quand network revient
                    }
                    snapshot != null -> {
                        val profile = if (snapshot.exists()) {
                            snapshot.toObject(UserProfile::class.java)
                        } else {
                            // FIX(user-dynamic): Document not found → retourner null
                            Timber.w("getUserProfile: Profile $uid n'existe pas")
                            null
                        }
                        trySend(profile)
                    }
                }
            }

        awaitClose {
            Timber.d("getUserProfile: Removing listener for $uid")
            listenerRegistration.remove()
        }
    }

    // FIX(user-dynamic): Créer nouveau profil
    override suspend fun createUserProfile(profile: UserProfile): Result<Unit> = try {
        require(profile.uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("createUserProfile: Creating profile ${profile.uid}")

        firestore.collection(COLLECTION_USERS)
            .document(profile.uid)
            .set(profile)
            .await()

        Timber.i("createUserProfile: Profile ${profile.uid} créé avec succès")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "createUserProfile: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "createUserProfile: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Mettre à jour profil complet
    override suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = try {
        require(profile.uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("updateUserProfile: Updating profile ${profile.uid}")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val updatedProfile = profile.copy(updatedAt = Date())

        firestore.collection(COLLECTION_USERS)
            .document(profile.uid)
            .set(updatedProfile)
            .await()

        Timber.i("updateUserProfile: Profile ${profile.uid} mis à jour")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updateUserProfile: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updateUserProfile: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Mise à jour partielle - locale
    // IMPORTANT: Field names must match Firestore schema (snake_case)
    override suspend fun updateLocale(uid: String, locale: String?): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("updateLocale: uid=$uid, locale=$locale")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val updates = mapOf(
            "locale" to locale,
            "updated_at" to Date()
        )

        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .update(updates)
            .await()

        Timber.i("updateLocale: Locale mise à jour pour $uid")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updateLocale: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updateLocale: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Mise à jour partielle - plan tier
    // IMPORTANT: Field names must match Firestore schema (snake_case)
    override suspend fun updatePlanTier(uid: String, tier: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }
        require(tier in listOf("free", "premium")) { "Tier doit être 'free' ou 'premium'" }

        Timber.d("updatePlanTier: uid=$uid, tier=$tier")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val updates = mapOf(
            "plan_tier" to tier,
            "updated_at" to Date()
        )

        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .update(updates)
            .await()

        Timber.i("updatePlanTier: PlanTier=$tier pour $uid")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updatePlanTier: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updatePlanTier: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Mise à jour partielle - photo URL
    // IMPORTANT: Field names must match Firestore schema (snake_case)
    override suspend fun updatePhotoUrl(uid: String, url: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("updatePhotoUrl: uid=$uid, url=$url")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val updates = mapOf(
            "photo_url" to url,
            "updated_at" to Date()
        )

        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .update(updates)
            .await()

        Timber.i("updatePhotoUrl: Photo mise à jour pour $uid")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updatePhotoUrl: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updatePhotoUrl: Erreur inattendue")
        Result.failure(e)
    }

    // FIX(user-dynamic): Mise à jour partielle - motto
    // IMPORTANT: Field names must match Firestore schema (snake_case)
    suspend fun updateMotto(uid: String, motto: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("updateMotto: uid=$uid, motto=$motto")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val updates = mapOf(
            "motto" to motto,
            "updated_at" to Date()
        )

        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .update(updates)
            .await()

        Timber.i("updateMotto: Motto mis à jour pour $uid")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updateMotto: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updateMotto: Erreur inattendue")
        Result.failure(e)
    }

    // Mise à jour partielle - profile group (onboarding)
    // IMPORTANT: Field names must match Firestore schema (snake_case)
    override suspend fun updateProfileGroup(
        uid: String,
        firstName: String?,
        birthDate: String?,
        gender: String?
    ): Result<Unit> {
        return try {
            require(uid.isNotBlank()) { "UID ne peut pas être vide" }

            Timber.d("updateProfileGroup: uid=$uid, firstName=$firstName, birthDate=$birthDate, gender=$gender")

            // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
            val updates = mutableMapOf<String, Any?>(
                "updated_at" to Date()
            )

            firstName?.let { updates["first_name"] = it }
            birthDate?.let { updates["birth_date"] = it }
            gender?.let { updates["gender"] = it }

            if (updates.size == 1) { // Only updated_at
                Timber.w("updateProfileGroup: No profile group fields to update")
                return Result.success(Unit)
            }

            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .await()

            Timber.i("updateProfileGroup: Profile group data updated for $uid")
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "updateProfileGroup: Erreur Firestore ${e.code}")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "updateProfileGroup: Erreur inattendue")
            Result.failure(e)
        }
    }

    // FIX(user-dynamic): Suppression profil (GDPR)
    override suspend fun deleteUserProfile(uid: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("deleteUserProfile: Deleting profile $uid")

        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .delete()
            .await()

        Timber.i("deleteUserProfile: Profile $uid supprimé")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "deleteUserProfile: Erreur Firestore ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "deleteUserProfile: Erreur inattendue")
        Result.failure(e)
    }
}
