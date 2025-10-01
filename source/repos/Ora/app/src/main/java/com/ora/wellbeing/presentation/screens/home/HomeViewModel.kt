package com.ora.wellbeing.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Injecter les use cases quand ils seront créés
    // private val getUserProfileUseCase: GetUserProfileUseCase,
    // private val getRecommendationsUseCase: GetRecommendationsUseCase,
    // private val getActivePrograms UzeCase: GetActiveProgramsUseCase,
    // private val getWeeklyStatsUseCase: GetWeeklyStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LoadHomeData -> loadHomeData()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                // TODO: Remplacer par de vraies données depuis les use cases
                _uiState.value = _uiState.value.copy(
                    isLoading = true
                )

                // Simulation de chargement de données
                val mockData = generateMockData()

                _uiState.value = mockData.copy(
                    isLoading = false
                )

                Timber.d("Home data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement des données"
                )
                Timber.e(e, "Error loading home data")
            }
        }
    }

    private fun generateMockData(): HomeUiState {
        return HomeUiState(
            userName = "Alex",
            streakDays = 7,
            dailyRecommendations = listOf(
                HomeUiState.ContentRecommendation(
                    id = "1",
                    title = "Méditation matinale",
                    category = "Méditation",
                    duration = "10 min",
                    thumbnailUrl = ""
                ),
                HomeUiState.ContentRecommendation(
                    id = "2",
                    title = "Yoga doux",
                    category = "Yoga",
                    duration = "15 min",
                    thumbnailUrl = ""
                ),
                HomeUiState.ContentRecommendation(
                    id = "3",
                    title = "Respiration anti-stress",
                    category = "Respiration",
                    duration = "5 min",
                    thumbnailUrl = ""
                )
            ),
            activePrograms = listOf(
                HomeUiState.ActiveProgram(
                    id = "prog1",
                    title = "Défi 21 jours - Méditation",
                    currentDay = 8,
                    totalDays = 21,
                    progressPercentage = 38
                ),
                HomeUiState.ActiveProgram(
                    id = "prog2",
                    title = "Yoga pour débutants",
                    currentDay = 3,
                    totalDays = 10,
                    progressPercentage = 30
                )
            ),
            totalMinutesThisWeek = 127,
            sessionsCompletedThisWeek = 9,
            favoriteCategory = "Méditation"
        )
    }
}

/**
 * État de l'interface utilisateur pour l'écran Home
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "",
    val streakDays: Int = 0,
    val dailyRecommendations: List<ContentRecommendation> = emptyList(),
    val activePrograms: List<ActiveProgram> = emptyList(),
    val totalMinutesThisWeek: Int = 0,
    val sessionsCompletedThisWeek: Int = 0,
    val favoriteCategory: String = ""
) {
    /**
     * Recommandation de contenu pour l'écran d'accueil
     */
    data class ContentRecommendation(
        val id: String,
        val title: String,
        val category: String,
        val duration: String,
        val thumbnailUrl: String,
        val description: String = ""
    )

    /**
     * Programme actif de l'utilisateur
     */
    data class ActiveProgram(
        val id: String,
        val title: String,
        val currentDay: Int,
        val totalDays: Int,
        val progressPercentage: Int,
        val nextSessionTitle: String = "",
        val nextSessionDuration: String = ""
    )
}

/**
 * Événements de l'interface utilisateur pour l'écran Home
 */
sealed interface HomeUiEvent {
    data object LoadHomeData : HomeUiEvent
}