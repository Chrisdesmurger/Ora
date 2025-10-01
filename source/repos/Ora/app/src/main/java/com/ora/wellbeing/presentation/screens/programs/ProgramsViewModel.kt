package com.ora.wellbeing.presentation.screens.programs

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
class ProgramsViewModel @Inject constructor(
    // TODO: Injecter les use cases quand ils seront créés
    // private val getProgramsUseCase: GetProgramsUseCase,
    // private val getActiveProgramsUseCase: GetActiveProgramsUseCase,
    // private val joinProgramUseCase: JoinProgramUseCase,
    // private val getRecommendedProgramsUseCase: GetRecommendedProgramsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramsUiState())
    val uiState: StateFlow<ProgramsUiState> = _uiState.asStateFlow()

    fun onEvent(event: ProgramsUiEvent) {
        when (event) {
            is ProgramsUiEvent.LoadProgramsData -> loadProgramsData()
            is ProgramsUiEvent.JoinProgram -> joinProgram(event.programId)
            is ProgramsUiEvent.LeaveProgram -> leaveProgram(event.programId)
        }
    }

    private fun loadProgramsData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Remplacer par de vraies données depuis les use cases
                val mockData = generateMockProgramsData()

                _uiState.value = mockData.copy(isLoading = false)

                Timber.d("Programs data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement des programmes"
                )
                Timber.e(e, "Error loading programs data")
            }
        }
    }

    private fun joinProgram(programId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implémenter l'inscription à un programme
                Timber.d("Joined program: $programId")

                // Recharger les données après inscription
                loadProgramsData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'inscription au programme"
                )
                Timber.e(e, "Error joining program")
            }
        }
    }

    private fun leaveProgram(programId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implémenter l'abandon d'un programme
                Timber.d("Left program: $programId")

                // Recharger les données après abandon
                loadProgramsData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de l'abandon du programme"
                )
                Timber.e(e, "Error leaving program")
            }
        }
    }

    private fun generateMockProgramsData(): ProgramsUiState {
        // Programmes actifs
        val activePrograms = listOf(
            ProgramsUiState.ActiveProgram(
                id = "active_1",
                title = "21 jours de méditation",
                description = "Développez une routine de méditation quotidienne",
                currentDay = 8,
                totalDays = 21,
                progressPercentage = 38,
                nextSessionTitle = "Méditation de pleine conscience",
                nextSessionDuration = "10 min"
            ),
            ProgramsUiState.ActiveProgram(
                id = "active_2",
                title = "Yoga pour débutants",
                description = "Apprenez les bases du yoga en douceur",
                currentDay = 3,
                totalDays = 10,
                progressPercentage = 30,
                nextSessionTitle = "Salutation au soleil",
                nextSessionDuration = "15 min"
            )
        )

        // Programmes recommandés
        val recommendedPrograms = listOf(
            ProgramsUiState.Program(
                id = "rec_1",
                title = "Gestion du stress",
                description = "Techniques avancées pour gérer le stress quotidien et retrouver la sérénité",
                category = "Bien-être",
                duration = 14,
                level = "Intermédiaire",
                participantCount = 1247,
                rating = 4.8f,
                thumbnailUrl = ""
            ),
            ProgramsUiState.Program(
                id = "rec_2",
                title = "Sommeil réparateur",
                description = "Améliorez la qualité de votre sommeil avec des techniques de relaxation",
                category = "Sommeil",
                duration = 7,
                level = "Débutant",
                participantCount = 892,
                rating = 4.6f,
                thumbnailUrl = ""
            )
        )

        // Défis populaires
        val popularChallenges = listOf(
            ProgramsUiState.Program(
                id = "challenge_1",
                title = "30 jours de gratitude",
                description = "Cultivez la gratitude quotidiennement",
                category = "Défis",
                duration = 30,
                level = "Tous niveaux",
                participantCount = 3456,
                rating = 4.9f,
                thumbnailUrl = ""
            ),
            ProgramsUiState.Program(
                id = "challenge_2",
                title = "7 jours de réveil en douceur",
                description = "Commencez chaque journée en douceur",
                category = "Défis",
                duration = 7,
                level = "Débutant",
                participantCount = 2103,
                rating = 4.7f,
                thumbnailUrl = ""
            ),
            ProgramsUiState.Program(
                id = "challenge_3",
                title = "14 jours sans stress",
                description = "Éliminez le stress de votre vie",
                category = "Défis",
                duration = 14,
                level = "Intermédiaire",
                participantCount = 1876,
                rating = 4.8f,
                thumbnailUrl = ""
            )
        )

        // Tous les programmes par catégorie
        val allPrograms = listOf(
            // Méditation
            ProgramsUiState.Program(
                id = "med_1",
                title = "Méditation avancée",
                description = "Techniques avancées de méditation",
                category = "Méditation",
                duration = 28,
                level = "Avancé",
                participantCount = 567,
                rating = 4.9f,
                thumbnailUrl = ""
            ),
            ProgramsUiState.Program(
                id = "med_2",
                title = "Méditation en mouvement",
                description = "Méditation à travers le mouvement",
                category = "Méditation",
                duration = 14,
                level = "Intermédiaire",
                participantCount = 432,
                rating = 4.5f,
                thumbnailUrl = ""
            ),

            // Yoga
            ProgramsUiState.Program(
                id = "yoga_1",
                title = "Yoga avancé",
                description = "Postures et séquences avancées",
                category = "Yoga",
                duration = 21,
                level = "Avancé",
                participantCount = 789,
                rating = 4.7f,
                thumbnailUrl = ""
            ),
            ProgramsUiState.Program(
                id = "yoga_2",
                title = "Yoga restaurateur",
                description = "Yoga doux pour la récupération",
                category = "Yoga",
                duration = 10,
                level = "Tous niveaux",
                participantCount = 634,
                rating = 4.6f,
                thumbnailUrl = ""
            ),

            // Bien-être
            ProgramsUiState.Program(
                id = "wellness_1",
                title = "Équilibre vie-travail",
                description = "Trouvez l'équilibre parfait",
                category = "Bien-être",
                duration = 21,
                level = "Intermédiaire",
                participantCount = 923,
                rating = 4.4f,
                thumbnailUrl = ""
            )
        )

        val programsByCategory = allPrograms.groupBy { it.category }

        return ProgramsUiState(
            activePrograms = activePrograms,
            recommendedPrograms = recommendedPrograms,
            popularChallenges = popularChallenges,
            programsByCategory = programsByCategory
        )
    }
}

/**
 * État de l'interface utilisateur pour l'écran Programs
 */
data class ProgramsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrograms: List<ActiveProgram> = emptyList(),
    val recommendedPrograms: List<Program> = emptyList(),
    val popularChallenges: List<Program> = emptyList(),
    val programsByCategory: Map<String, List<Program>> = emptyMap()
) {
    /**
     * Programme actif de l'utilisateur
     */
    data class ActiveProgram(
        val id: String,
        val title: String,
        val description: String,
        val currentDay: Int,
        val totalDays: Int,
        val progressPercentage: Int,
        val nextSessionTitle: String,
        val nextSessionDuration: String,
        val category: String = "",
        val thumbnailUrl: String = ""
    )

    /**
     * Programme disponible
     */
    data class Program(
        val id: String,
        val title: String,
        val description: String,
        val category: String,
        val duration: Int, // en jours
        val level: String,
        val participantCount: Int,
        val rating: Float,
        val thumbnailUrl: String,
        val instructor: String = "",
        val price: String = "Gratuit",
        val isEnrolled: Boolean = false,
        val estimatedTimePerDay: String = "10-15 min"
    )
}

/**
 * Événements de l'interface utilisateur pour l'écran Programs
 */
sealed interface ProgramsUiEvent {
    data object LoadProgramsData : ProgramsUiEvent
    data class JoinProgram(val programId: String) : ProgramsUiEvent
    data class LeaveProgram(val programId: String) : ProgramsUiEvent
}