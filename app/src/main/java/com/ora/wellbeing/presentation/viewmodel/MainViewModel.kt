package com.ora.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.repository.SettingsRepository
import com.ora.wellbeing.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel principal de l'application
 * Gère l'état global et la navigation initiale
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkInitialState()
    }

    /**
     * Vérifie l'état initial de l'application
     */
    private fun checkInitialState() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isReady = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Efface les erreurs
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Force le rechargement
     */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        checkInitialState()
    }
}

/**
 * État UI principal de l'application
 */
data class MainUiState(
    val isLoading: Boolean = true,
    val isReady: Boolean = false,
    val error: String? = null
)