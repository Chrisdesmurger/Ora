package com.ora.wellbeing.data.sync

import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.model.UserStats
import com.ora.wellbeing.data.repository.AuthRepository
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.domain.repository.FirestoreUserStatsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX(user-dynamic): SyncManager pour orchestrer la synchronisation Firestore
 *
 * Responsabilités:
 * - S'abonner aux profils/stats au login
 * - Créer automatiquement profil/stats si absents
 * - Nettoyer les listeners au logout
 * - Exposer l'état de sync
 */
@Singleton
class SyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val userStatsRepository: FirestoreUserStatsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var profileSyncJob: Job? = null
    private var statsSyncJob: Job? = null

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _userStats = MutableStateFlow<UserStats?>(null)
    val userStats: StateFlow<UserStats?> = _userStats.asStateFlow()

    /**
     * Démarre la synchronisation pour un utilisateur
     * Doit être appelé après le login
     */
    fun startSync(uid: String) {
        Timber.d("SyncManager: Starting sync for uid=$uid")

        if (uid.isBlank()) {
            Timber.e("SyncManager: Cannot start sync with empty uid")
            _syncState.value = SyncState.Error("UID invalide")
            return
        }

        // Arrêter toute synchro précédente
        stopSync()

        _syncState.value = SyncState.Syncing

        // S'abonner au profil
        profileSyncJob = scope.launch {
            try {
                userProfileRepository.getUserProfile(uid).collect { profile ->
                    if (profile == null) {
                        // Profil n'existe pas encore, le créer
                        Timber.d("SyncManager: Profile doesn't exist, creating default profile")
                        createDefaultProfile(uid)
                    } else {
                        Timber.d("SyncManager: Profile received: ${profile.firstName}")
                        _userProfile.value = profile
                        updateSyncState()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "SyncManager: Error syncing profile")
                _syncState.value = SyncState.Error("Erreur profil: ${e.message}")
            }
        }

        // S'abonner aux stats
        statsSyncJob = scope.launch {
            try {
                userStatsRepository.getUserStats(uid).collect { stats ->
                    if (stats == null) {
                        // Stats n'existent pas encore, les créer
                        Timber.d("SyncManager: Stats don't exist, creating default stats")
                        createDefaultStats(uid)
                    } else {
                        Timber.d("SyncManager: Stats received: ${stats.sessions} sessions")
                        _userStats.value = stats
                        updateSyncState()
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "SyncManager: Error syncing stats")
                _syncState.value = SyncState.Error("Erreur stats: ${e.message}")
            }
        }
    }

    /**
     * Arrête toutes les synchronisations en cours
     * Doit être appelé au logout
     */
    fun stopSync() {
        Timber.d("SyncManager: Stopping sync")

        profileSyncJob?.cancel()
        statsSyncJob?.cancel()

        profileSyncJob = null
        statsSyncJob = null

        _userProfile.value = null
        _userStats.value = null
        _syncState.value = SyncState.Idle
    }

    /**
     * Crée un profil par défaut pour un nouvel utilisateur
     */
    private suspend fun createDefaultProfile(uid: String) {
        try {
            val firebaseUser = authRepository.getCurrentFirebaseUser()
            val defaultProfile = UserProfile(
                uid = uid,
                firstName = firebaseUser?.displayName?.split(" ")?.firstOrNull(),
                photoUrl = firebaseUser?.photoUrl?.toString(),
                planTier = "free",
                createdAt = System.currentTimeMillis(),
                locale = null
            )
            // Ensure hasCompletedOnboarding is set to false for new users
            defaultProfile.hasCompletedOnboarding = false

            val result = userProfileRepository.createUserProfile(defaultProfile)
            if (result.isSuccess) {
                Timber.d("SyncManager: Default profile created successfully")
            } else {
                Timber.e("SyncManager: Failed to create default profile: ${result.exceptionOrNull()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "SyncManager: Exception creating default profile")
        }
    }

    /**
     * Crée des stats par défaut pour un nouvel utilisateur
     */
    private suspend fun createDefaultStats(uid: String) {
        try {
            val defaultStats = UserStats.createDefault(uid)

            val result = userStatsRepository.createUserStats(defaultStats)
            if (result.isSuccess) {
                Timber.d("SyncManager: Default stats created successfully")
            } else {
                Timber.e("SyncManager: Failed to create default stats: ${result.exceptionOrNull()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "SyncManager: Exception creating default stats")
        }
    }

    /**
     * Met à jour l'état de synchronisation
     */
    private fun updateSyncState() {
        if (_userProfile.value != null && _userStats.value != null) {
            _syncState.value = SyncState.Synced
        }
    }
}

/**
 * État de la synchronisation
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Synced : SyncState()
    data class Error(val message: String) : SyncState()
}
