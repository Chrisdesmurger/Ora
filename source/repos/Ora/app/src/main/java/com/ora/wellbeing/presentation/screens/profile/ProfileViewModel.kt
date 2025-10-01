package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
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
class ProfileViewModel @Inject constructor(
    // TODO: Injecter les use cases quand ils seront créés
    // private val getUserProfileUseCase: GetUserProfileUseCase,
    // private val getPracticeTimesUseCase: GetPracticeTimesUseCase,
    // private val getUserGoalsUseCase: GetUserGoalsUseCase,
    // private val updateGoalUseCase: UpdateGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfileData -> loadProfileData()
            is ProfileUiEvent.NavigateToEditProfile -> {
                Timber.d("Navigate to edit profile")
                // Navigation sera gérée dans le Screen
            }
            is ProfileUiEvent.NavigateToPracticeStats -> {
                Timber.d("Navigate to practice stats: ${event.practiceId}")
                // Navigation sera gérée dans le Screen
            }
            is ProfileUiEvent.ToggleGoal -> toggleGoal(event.goalId)
            is ProfileUiEvent.NavigateToGratitudes -> {
                Timber.d("Navigate to gratitudes")
                // Navigation sera gérée dans le Screen
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Remplacer par de vraies données depuis les use cases
                val mockData = generateMockProfileData()

                _uiState.value = mockData.copy(isLoading = false)

                Timber.d("Profile data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du profil"
                )
                Timber.e(e, "Error loading profile data")
            }
        }
    }

    private fun toggleGoal(goalId: String) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val updatedGoals = currentState.goals.map { goal ->
                    if (goal.id == goalId) {
                        goal.copy(isCompleted = !goal.isCompleted)
                    } else {
                        goal
                    }
                }

                _uiState.value = currentState.copy(goals = updatedGoals)

                Timber.d("Goal toggled: goalId=$goalId")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour de l'objectif"
                )
                Timber.e(e, "Error toggling goal")
            }
        }
    }

    /**
     * Génère des données mockées correspondant au mockup image9.png
     */
    private fun generateMockProfileData(): ProfileUiState {
        // Couleurs selon le mockup
        val orangeColor = Color(0xFFF4845F)
        val orangeLightColor = Color(0xFFFDB5A0)
        val greenColor = Color(0xFF7BA089)
        val greenLightColor = Color(0xFFB4D4C3)

        return ProfileUiState(
            userProfile = UserProfile(
                name = "Clara",
                motto = "Je prends soin de moi chaque jour",
                photoUrl = null // Sera remplacé par une vraie URL plus tard
            ),
            practiceTimes = listOf(
                PracticeTime(
                    id = "yoga",
                    name = "Yoga",
                    time = "3h45 ce mois-ci",
                    color = orangeColor,
                    icon = Icons.Default.SelfImprovement
                ),
                PracticeTime(
                    id = "pilates",
                    name = "Pilates",
                    time = "2h15 ce mois-ci",
                    color = orangeLightColor,
                    icon = Icons.Default.FitnessCenter
                ),
                PracticeTime(
                    id = "meditation",
                    name = "Méditation",
                    time = "4h30 ce mois-ci",
                    color = greenColor,
                    icon = Icons.Default.Spa
                ),
                PracticeTime(
                    id = "breathing",
                    name = "Respiration",
                    time = "1h20 ce mois-ci",
                    color = greenLightColor,
                    icon = Icons.Default.Air
                )
            ),
            streak = 5,
            totalTime = "24h10",
            lastActivity = "Yoga doux - 25 min",
            hasGratitudeToday = true,
            goals = listOf(
                Goal(
                    id = "goal_1",
                    text = "Lire plus",
                    isCompleted = true
                ),
                Goal(
                    id = "goal_2",
                    text = "Arrêter l'alcool",
                    isCompleted = true
                ),
                Goal(
                    id = "goal_3",
                    text = "10 min de réseaux sociaux max",
                    isCompleted = false
                )
            )
        )
    }
}