package com.ora.wellbeing.domain.usecase

import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.model.UserStats
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

// FIX(user-dynamic): Use case pour observer les données utilisateur complètes
// Combine profil + stats en un seul Flow réactif

/**
 * Data class combinant profil et stats utilisateur
 */
data class UserData(
    val profile: UserProfile?,
    val stats: UserStats?
)

/**
 * Use case pour observer les données utilisateur en temps réel
 * Combine les Flow de profil et stats pour l'UI
 *
 * @param uid Firebase Auth UID
 * @return Flow<UserData> émettant les changements de profil ET stats
 */
class GetUserDataUseCase @Inject constructor(
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val userStatsRepository: FirestoreUserStatsRepository
) {
    operator fun invoke(uid: String): Flow<UserData> {
        require(uid.isNotBlank()) { "UID ne peut pas être vide" }

        Timber.d("GetUserDataUseCase: Observing user data for $uid")

        // FIX(user-dynamic): Combiner les deux Flow pour l'UI
        return combine(
            userProfileRepository.getUserProfile(uid),
            userStatsRepository.getUserStats(uid)
        ) { profile, stats ->
            UserData(profile = profile, stats = stats)
        }
    }
}
