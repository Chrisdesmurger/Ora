package com.ora.wellbeing.domain.usecase

import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.model.UserStats
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import timber.log.Timber
import javax.inject.Inject

// FIX(user-dynamic): Use case pour créer un profil utilisateur complet
// Orchestration: Crée à la fois le profil ET les stats lors du premier login

/**
 * Use case pour créer un profil utilisateur complet (profil + stats)
 * Appelé une seule fois lors du premier login Firebase Auth
 *
 * Business logic:
 * 1. Crée le document users/{uid} avec valeurs par défaut
 * 2. Crée le document stats/{uid} avec valeurs par défaut (0/0/0)
 * 3. Les deux opérations doivent réussir
 *
 * @return Result.success si les deux créations réussissent, Result.failure sinon
 */
class CreateUserProfileUseCase @Inject constructor(
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val userStatsRepository: FirestoreUserStatsRepository
) {
    suspend operator fun invoke(
        uid: String,
        firstName: String? = null,
        photoUrl: String? = null
    ): Result<Unit> {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("CreateUserProfileUseCase: Creating profile and stats for $uid")

        // FIX(user-dynamic): Créer profil par défaut
        val profile = UserProfile.createDefault(
            uid = uid,
            firstName = firstName
        ).copy(photoUrl = photoUrl)

        // FIX(user-dynamic): Créer stats par défaut
        val stats = UserStats.createDefault(uid)

        // FIX(user-dynamic): Créer les deux documents
        val profileResult = userProfileRepository.createUserProfile(profile)
        if (profileResult.isFailure) {
            Timber.e("CreateUserProfileUseCase: Échec création profil")
            return Result.failure(
                profileResult.exceptionOrNull() ?: Exception("Échec création profil")
            )
        }

        val statsResult = userStatsRepository.createUserStats(stats)
        if (statsResult.isFailure) {
            Timber.e("CreateUserProfileUseCase: Échec création stats")
            return Result.failure(
                statsResult.exceptionOrNull() ?: Exception("Échec création stats")
            )
        }

        Timber.i("CreateUserProfileUseCase: Profil et stats créés avec succès pour $uid")
        return Result.success(Unit)
    }
}
