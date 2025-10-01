package com.ora.wellbeing.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

/**
 * FIX(auth): ViewModel pour gérer l'état d'authentification au niveau de la navigation
 * Expose un Flow simple qui indique si l'utilisateur est connecté
 */
@HiltViewModel
class OraAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    /**
     * Flow qui émet true si l'utilisateur est authentifié, false sinon
     */
    val isAuthenticated: StateFlow<Boolean> = authRepository.currentUserFlow()
        .map { user ->
            val authenticated = user != null
            Timber.d("OraAuthViewModel: User authentication state: $authenticated")
            authenticated
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authRepository.isUserSignedIn()
        )
}
