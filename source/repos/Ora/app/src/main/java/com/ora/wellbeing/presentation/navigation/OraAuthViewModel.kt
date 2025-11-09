package com.ora.wellbeing.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.repository.AuthRepository
import com.ora.wellbeing.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FIX(user-dynamic): ViewModel pour gérer l'état d'authentification et le SyncManager
 * Expose un Flow simple qui indique si l'utilisateur est connecté
 */
@HiltViewModel
class OraAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager // FIX(user-dynamic): Injection du SyncManager
) : ViewModel() {

    /**
     * Expose the user profile from SyncManager for onboarding check
     */
    val userProfile = syncManager.userProfile

    /**
     * Flow qui émet true si l'utilisateur est authentifié, false sinon
     * FIX(user-dynamic): Démarre/arrête le SyncManager selon l'état d'authentification
     */
    val isAuthenticated: StateFlow<Boolean> = authRepository.currentUserFlow()
        .map { user ->
            val authenticated = user != null
            Timber.d("OraAuthViewModel: User authentication state: $authenticated, uid=${user?.id}")

            // FIX(user-dynamic): Gérer le SyncManager selon l'état d'authentification
            if (authenticated && user != null) {
                Timber.d("OraAuthViewModel: User authenticated, starting sync with uid=${user.id}")
                startSync(user.id)
            } else {
                Timber.d("OraAuthViewModel: User not authenticated, stopping sync")
                stopSync()
            }

            authenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.isUserSignedIn()
        )

    init {
        // FIX(user-dynamic): Démarrer le sync si déjà authentifié au lancement
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentFirebaseUser()
            if (currentUser != null) {
                Timber.d("OraAuthViewModel: User already authenticated, starting sync")
                startSync(currentUser.uid)
            }
        }
    }

    /**
     * FIX(user-dynamic): Démarre la synchronisation Firestore pour un utilisateur
     */
    private fun startSync(uid: String) {
        viewModelScope.launch {
            try {
                Timber.d("OraAuthViewModel: Starting SyncManager for uid=$uid")
                syncManager.startSync(uid)
            } catch (e: Exception) {
                Timber.e(e, "OraAuthViewModel: Error starting SyncManager")
            }
        }
    }

    /**
     * FIX(user-dynamic): Arrête la synchronisation au logout
     */
    private fun stopSync() {
        viewModelScope.launch {
            try {
                Timber.d("OraAuthViewModel: Stopping SyncManager")
                syncManager.stopSync()
            } catch (e: Exception) {
                Timber.e(e, "OraAuthViewModel: Error stopping SyncManager")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // FIX(user-dynamic): Cleanup du SyncManager
        stopSync()
    }
}
