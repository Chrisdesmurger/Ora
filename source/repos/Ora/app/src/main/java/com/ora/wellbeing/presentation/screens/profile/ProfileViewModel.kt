package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.model.PlanTier
import com.ora.wellbeing.data.repository.PracticeStatsRepository
import com.ora.wellbeing.data.repository.UserProfileRepository
import com.ora.wellbeing.data.repository.UserStatsRepository
import com.ora.wellbeing.data.sync.SyncManager
import com.ora.wellbeing.domain.model.PracticeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// FIX(user-dynamic): ProfileViewModel mis à jour pour utiliser les données Firestore
// FIX(stats): Ajout de PracticeStatsRepository pour stats détaillées par type
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val userProfileRepository: UserProfileRepository,
    private val practiceStatsRepository: PracticeStatsRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // FIX(user-dynamic): S'abonner aux changements de profil et stats via SyncManager
        observeUserData()
        // FIX(stats): Observer les stats détaillées par pratique
        observePracticeStats()
        // FIX(stats): Observer la dernière activité
        observeLastActivity()
    }

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
            is ProfileUiEvent.UpdateMotto -> updateMotto(event.motto)
            is ProfileUiEvent.UpdatePhotoUrl -> updatePhotoUrl(event.photoUrl)
        }
    }

    // FIX(user-dynamic): Observer les données utilisateur depuis le SyncManager
    private fun observeUserData() {
        viewModelScope.launch {
            combine(
                syncManager.userProfile,
                syncManager.userStats,
                syncManager.syncState
            ) { profile, stats, syncState ->
                Triple(profile, stats, syncState)
            }.collect { (profile, stats, syncState) ->
                // FIX(user-dynamic): Transformer les données Firestore en UiState
                _uiState.value = _uiState.value.copy(
                    isLoading = syncState is com.ora.wellbeing.data.sync.SyncState.Syncing,
                    error = if (syncState is com.ora.wellbeing.data.sync.SyncState.Error) {
                        syncState.message
                    } else null,
                    userProfile = profile?.let {
                        com.ora.wellbeing.presentation.screens.profile.UserProfile(
                            name = it.displayName(),
                            firstName = it.firstName ?: "",
                            motto = it.motto ?: "Je prends soin de moi chaque jour",
                            photoUrl = it.photoUrl,
                            isPremium = it.isPremium,
                            planTier = when(it.planTier) {
                                "free" -> "Gratuit"
                                "premium" -> "Premium"
                                "lifetime" -> "Lifetime"
                                else -> "Gratuit"
                            }
                        )
                    },
                    streak = stats?.streakDays ?: 0,
                    totalTime = stats?.formatTotalTime() ?: "0min",
                    hasGratitudeToday = false, // TODO: Implement gratitude tracking
                    goals = buildGoalsFromStats(stats)
                )

                Timber.d("ProfileViewModel: UI State updated - ${profile?.firstName}, ${stats?.sessions} sessions")
            }
        }
    }

    // FIX(stats): Observer les stats détaillées par type de pratique
    private fun observePracticeStats() {
        viewModelScope.launch {
            syncManager.userProfile.collect { profile ->
                profile?.let { userProfile ->
                    practiceStatsRepository.observePracticeStats(userProfile.uid)
                        .collect { practiceStatsList ->
                            _uiState.value = _uiState.value.copy(
                                practiceTimes = buildPracticeTimesFromStats(practiceStatsList)
                            )
                            Timber.d("Practice stats updated: ${practiceStatsList.size} types")
                        }
                }
            }
        }
    }

    // FIX(stats): Observer la dernière activité
    private fun observeLastActivity() {
        viewModelScope.launch {
            syncManager.userProfile.collect { profile ->
                profile?.let { userProfile ->
                    val lastSessionResult = practiceStatsRepository.getLastSession(userProfile.uid)
                    lastSessionResult.onSuccess { session ->
                        val activityText = session?.let {
                            "${it.contentTitle} - ${it.durationMinutes} min"
                        } ?: "Aucune activité récente"

                        _uiState.value = _uiState.value.copy(
                            lastActivity = activityText
                        )
                        Timber.d("Last activity updated: $activityText")
                    }
                }
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                Timber.d("Profile data loading triggered (managed by SyncManager)")
                // FIX(user-dynamic): Les données sont maintenant gérées automatiquement par le SyncManager
                // Pas besoin de fetch manuel, juste observer les flows
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du profil"
                )
                Timber.e(e, "Error loading profile data")
            }
        }
    }

    // TODO: Implement goals tracking system
    private fun toggleGoal(goalId: String) {
        // Stub: Goals system not yet implemented
        Timber.d("toggleGoal called but not implemented: $goalId")
    }

    // FIX(user-dynamic): Mettre à jour le motto
    private fun updateMotto(motto: String) {
        viewModelScope.launch {
            val profile = syncManager.userProfile.value ?: return@launch
            val result = userProfileRepository.updateMotto(profile.uid, motto)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour du motto"
                )
            }
        }
    }

    // FIX(user-dynamic): Mettre à jour la photo de profil
    private fun updatePhotoUrl(photoUrl: String?) {
        viewModelScope.launch {
            val profile = syncManager.userProfile.value ?: return@launch
            val result = userProfileRepository.updatePhotoUrl(profile.uid, photoUrl)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la mise à jour de la photo"
                )
            }
        }
    }

    // FIX(stats): Construire les PracticeTime depuis les vraies stats Firestore
    private fun buildPracticeTimesFromStats(practiceStatsList: List<PracticeStats>): List<PracticeTime> {
        val orangeColor = Color(0xFFF4845F)
        val orangeLightColor = Color(0xFFFDB5A0)
        val greenColor = Color(0xFF7BA089)
        val greenLightColor = Color(0xFFB4D4C3)

        val statsMap = practiceStatsList.associateBy { it.practiceType }

        return listOf(
            PracticeTime(
                id = "yoga",
                name = "Yoga",
                time = statsMap["yoga"]?.formatMonthTime() ?: "0min ce mois-ci",
                color = orangeColor,
                icon = Icons.Default.SelfImprovement
            ),
            PracticeTime(
                id = "pilates",
                name = "Pilates",
                time = statsMap["pilates"]?.formatMonthTime() ?: "0min ce mois-ci",
                color = orangeLightColor,
                icon = Icons.Default.FitnessCenter
            ),
            PracticeTime(
                id = "meditation",
                name = "Méditation",
                time = statsMap["meditation"]?.formatMonthTime() ?: "0min ce mois-ci",
                color = greenColor,
                icon = Icons.Default.Spa
            ),
            PracticeTime(
                id = "breathing",
                name = "Respiration",
                time = statsMap["breathing"]?.formatMonthTime() ?: "0min ce mois-ci",
                color = greenLightColor,
                icon = Icons.Default.Air
            )
        )
    }

    // TODO: Implement goals tracking system
    private fun buildGoalsFromStats(stats: com.ora.wellbeing.domain.model.UserStats?): List<Goal> {
        // Stub: Goals system not yet implemented
        return emptyList()
    }

    // FIX(user-dynamic): Helper pour mapper les IDs de goals aux textes
    // TODO: À remplacer par une vraie collection Firestore "goals"
    private fun getGoalTextById(goalId: String): String {
        return when (goalId) {
            "goal_read" -> "Lire plus"
            "goal_alcohol" -> "Arrêter l'alcool"
            "goal_social" -> "10 min de réseaux sociaux max"
            "goal_exercise" -> "30 min d'exercice par jour"
            "goal_meditation" -> "Méditer chaque matin"
            else -> goalId
        }
    }
}
