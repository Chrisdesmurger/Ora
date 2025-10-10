package com.ora.wellbeing.core.domain.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modèle de notes personnelles pour une pratique
 */
data class PracticeNotes(
    val practiceId: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Repository pour les notes personnelles des pratiques
 * Stockage: Firestore users/{uid}/notes/{practiceId}
 */
@Singleton
class UserNotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Récupère les notes pour une pratique
     */
    suspend fun getNotes(practiceId: String): Result<String> {
        return try {
            val uid = getCurrentUserId()
                ?: return Result.failure(Exception("Utilisateur non authentifié"))

            val doc = firestore
                .collection("users")
                .document(uid)
                .collection("notes")
                .document(practiceId)
                .get()
                .await()

            val notes = doc.toObject(PracticeNotes::class.java)?.notes ?: ""
            Result.success(notes)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération des notes pour $practiceId")
            Result.failure(e)
        }
    }

    /**
     * Enregistre les notes pour une pratique
     */
    suspend fun saveNotes(practiceId: String, notes: String): Result<Unit> {
        return try {
            val uid = getCurrentUserId()
                ?: return Result.failure(Exception("Utilisateur non authentifié"))

            val practiceNotes = PracticeNotes(
                practiceId = practiceId,
                notes = notes,
                updatedAt = System.currentTimeMillis()
            )

            firestore
                .collection("users")
                .document(uid)
                .collection("notes")
                .document(practiceId)
                .set(practiceNotes)
                .await()

            Timber.d("Notes enregistrées pour $practiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'enregistrement des notes pour $practiceId")
            Result.failure(e)
        }
    }

    /**
     * Supprime les notes pour une pratique
     */
    suspend fun deleteNotes(practiceId: String): Result<Unit> {
        return try {
            val uid = getCurrentUserId()
                ?: return Result.failure(Exception("Utilisateur non authentifié"))

            firestore
                .collection("users")
                .document(uid)
                .collection("notes")
                .document(practiceId)
                .delete()
                .await()

            Timber.d("Notes supprimées pour $practiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la suppression des notes pour $practiceId")
            Result.failure(e)
        }
    }
}
