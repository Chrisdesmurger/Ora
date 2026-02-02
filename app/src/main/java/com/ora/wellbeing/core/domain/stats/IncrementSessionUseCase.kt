package com.ora.wellbeing.core.domain.stats

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case pour incrémenter les statistiques de session
 * Met à jour les stats utilisateur après une session complétée
 */
@Singleton
class IncrementSessionUseCase @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    /**
     * Incrémente les stats après une session complétée
     * @param durationMin Durée de la session en minutes
     * @param discipline Type de pratique
     */
    suspend operator fun invoke(durationMin: Int, discipline: String = ""): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Utilisateur non authentifié"))

            val statsRef = firestore.collection("stats").document(uid)

            // Incrémenter totalMinutes et sessionsCompleted
            val updates = hashMapOf<String, Any>(
                "totalMinutes" to FieldValue.increment(durationMin.toLong()),
                "sessionsCompleted" to FieldValue.increment(1)
            )

            // Si discipline spécifiée, incrémenter aussi le compteur spécifique
            if (discipline.isNotEmpty()) {
                val disciplineField = "${discipline.lowercase()}Minutes"
                updates[disciplineField] = FieldValue.increment(durationMin.toLong())
            }

            statsRef.update(updates).await()

            Timber.d("Stats incrémentées: +$durationMin min, discipline=$discipline")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'incrémentation des stats")
            Result.failure(e)
        }
    }
}
