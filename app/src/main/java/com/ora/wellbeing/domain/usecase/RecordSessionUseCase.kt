package com.ora.wellbeing.domain.usecase

import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

// FIX(user-dynamic): Use case pour enregistrer une séance terminée
// Met à jour les stats avec calcul automatique du streak

/**
 * Use case pour enregistrer une séance de pratique terminée
 * Appelé lorsque l'utilisateur termine une vidéo/audio de méditation/yoga
 *
 * Business logic:
 * - Incrémente totalMinutes
 * - Incrémente sessions
 * - Calcule et met à jour streakDays (selon lastPracticeAt)
 * - Met à jour lastPracticeAt et updatedAt
 *
 * @param uid Firebase Auth UID
 * @param durationMinutes Durée de la séance en minutes
 * @return Result.success si mis à jour, Result.failure sinon
 */
class RecordSessionUseCase @Inject constructor(
    private val userStatsRepository: FirestoreUserStatsRepository
) {
    suspend operator fun invoke(
        uid: String,
        durationMinutes: Int
    ): Result<Unit> {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }
        require(durationMinutes > 0) { "Duration doit être > 0" }

        Timber.d("RecordSessionUseCase: Recording session for $uid, duration=$durationMinutes min")

        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
        val timestamp = Date()
        val result = userStatsRepository.incrementSession(uid, durationMinutes, timestamp)

        if (result.isSuccess) {
            Timber.i("RecordSessionUseCase: Session enregistrée avec succès pour $uid")
        } else {
            Timber.e("RecordSessionUseCase: Échec enregistrement session pour $uid")
        }

        return result
    }
}
