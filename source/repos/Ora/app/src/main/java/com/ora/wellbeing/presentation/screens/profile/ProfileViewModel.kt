package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.repository.PracticeStatsRepository
import com.ora.wellbeing.data.repository.UserProfileRepository
import com.ora.wellbeing.data.repository.UserStatsRepository
import com.ora.wellbeing.data.sync.SyncManager
import com.ora.wellbeing.domain.model.PracticeStats
import com.ora.wellbeing.domain.repository.UserProgramRepository
import com.ora.wellbeing.data.model.UserProfile as FirestoreUserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ProfileViewModel - Redesigned for mockup (Issue #64)
 * Provides dynamic data for monthly stats, challenges, favorites
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val userProfileRepository: UserProfileRepository,
    private val practiceStatsRepository: PracticeStatsRepository,
    private val userStatsRepository: UserStatsRepository,
    private val userProgramRepository: UserProgramRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var practiceStatsJob: Job? = null
    private var lastActivityJob: Job? = null
    private var programsJob: Job? = null

    init {
        observeAllData()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.LoadProfileData -> loadProfileData()
            is ProfileUiEvent.NavigateToEditProfile -> {
                Timber.d("Navigate to edit profile")
            }
            is ProfileUiEvent.NavigateToPracticeStats -> {
                Timber.d("Navigate to practice stats: ${event.practiceId}")
            }
            is ProfileUiEvent.ToggleGoal -> toggleGoal(event.goalId)
            is ProfileUiEvent.NavigateToGratitudes -> {
                Timber.d("Navigate to gratitudes")
            }
            is ProfileUiEvent.UpdateMotto -> updateMotto(event.motto)
            is ProfileUiEvent.UpdatePhotoUrl -> updatePhotoUrl(event.photoUrl)
        }
    }

    /**
     * Observe all data sources and combine into UI state
     */
    private fun observeAllData() {
        viewModelScope.launch {
            try {
                combine(
                    syncManager.userProfile,
                    syncManager.userStats,
                    syncManager.syncState
                ) { profile, stats, syncState ->
                    Triple(profile, stats, syncState)
                }.collect { (profile, stats, syncState) ->
                    // Cast profile to FirestoreUserProfile for access to extension methods
                    val firestoreProfile = profile as? FirestoreUserProfile

                    // Update UI state with basic user data
                    _uiState.value = _uiState.value.copy(
                        isLoading = syncState is com.ora.wellbeing.data.sync.SyncState.Syncing,
                        error = if (syncState is com.ora.wellbeing.data.sync.SyncState.Error) {
                            syncState.message
                        } else null,
                        userProfile = firestoreProfile?.let { fp ->
                            UserProfile(
                                name = fp.getDisplayName(),
                                firstName = fp.firstName ?: "",
                                motto = fp.motto ?: "Je prends soin de moi chaque jour",
                                photoUrl = fp.photoUrl,
                                isPremium = fp.isPremium(),
                                planTier = when(fp.planTier) {
                                    "FREE" -> "Gratuit"
                                    "PREMIUM" -> "Premium"
                                    "LIFETIME" -> "Lifetime"
                                    else -> "Gratuit"
                                }
                            )
                        },
                        streak = stats?.streakDays ?: 0,
                        totalTime = stats?.formatTotalTime() ?: "0min",
                        completedWorkouts = stats?.sessions ?: 0,
                        currentMonthName = getCurrentMonthName(),
                        hasGratitudeToday = false // TODO: Implement gratitude tracking
                    )

                    // Calculate monthly completion percentage
                    calculateMonthlyCompletion(stats?.sessions ?: 0)

                    // Load detailed data if we have a profile
                    firestoreProfile?.let { fp ->
                        loadPracticeStatsForUser(fp.uid)
                        loadProgramDataForUser(fp.uid)
                    }

                    Timber.d("ProfileViewModel: UI State updated - ${firestoreProfile?.firstName}, ${stats?.sessions} sessions")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in observeAllData")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du profil"
                )
            }
        }
    }

    /**
     * Load practice stats for detailed time tracking
     */
    private fun loadPracticeStatsForUser(uid: String) {
        practiceStatsJob?.cancel()
        lastActivityJob?.cancel()

        practiceStatsJob = viewModelScope.launch {
            try {
                practiceStatsRepository.observePracticeStats(uid)
                    .collect { practiceStatsList ->
                        _uiState.value = _uiState.value.copy(
                            practiceTimes = buildPracticeTimesFromStats(practiceStatsList)
                        )
                        Timber.d("Practice stats updated: ${practiceStatsList.size} types")
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading practice stats")
            }
        }

        lastActivityJob = viewModelScope.launch {
            try {
                val lastSessionResult = practiceStatsRepository.getLastSession(uid)
                lastSessionResult.onSuccess { session ->
                    val activityText = session?.let {
                        "${it.contentTitle} - ${it.durationMinutes} min"
                    } ?: "Aucune activité récente"

                    _uiState.value = _uiState.value.copy(
                        lastActivity = activityText
                    )
                    Timber.d("Last activity updated: $activityText")
                }.onFailure { error ->
                    Timber.e(error, "Error getting last session")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading last activity")
            }
        }
    }

    /**
     * Load user programs data for challenges tracking
     */
    private fun loadProgramDataForUser(uid: String) {
        programsJob?.cancel()

        programsJob = viewModelScope.launch {
            try {
                // Observe active programs
                userProgramRepository.getActivePrograms(uid).collect { activePrograms ->
                    val challengesInProgress = activePrograms.size

                    // Get the first active challenge for display
                    val firstActiveChallenge = activePrograms.firstOrNull()?.let { program ->
                        ActiveChallenge(
                            id = program.programId,
                            name = program.programTitle ?: "Challenge",
                            progressPercent = program.calculateProgress(),
                            currentDay = program.currentDay,
                            totalDays = program.totalDays
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        challengesInProgress = challengesInProgress,
                        activeChallenge = firstActiveChallenge
                    )

                    Timber.d("Programs updated: $challengesInProgress active")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading programs")
            }
        }

        // Load completed programs count
        viewModelScope.launch {
            try {
                val completedCount = userProgramRepository.getCompletedProgramCount(uid)
                _uiState.value = _uiState.value.copy(
                    completedChallenges = completedCount
                )
                Timber.d("Completed programs: $completedCount")
            } catch (e: Exception) {
                Timber.e(e, "Error loading completed programs count")
            }
        }

        // TODO: Load favorites count (requires favorites implementation)
        // For now, use placeholder values
        _uiState.value = _uiState.value.copy(
            favoriteWorkoutsCount = 0,
            favoriteChallengesCount = 0
        )
    }

    /**
     * Calculate monthly completion percentage
     * Based on sessions completed vs target (e.g., 20 sessions/month = 100%)
     */
    private fun calculateMonthlyCompletion(totalSessions: Int) {
        // TODO: Implement proper monthly tracking
        // For now, calculate based on current month and a target of 20 sessions
        val targetSessionsPerMonth = 20
        val currentMonthCompletion = ((totalSessions % targetSessionsPerMonth) * 100 / targetSessionsPerMonth).coerceIn(0, 100)

        // Generate previous months (placeholder data)
        val previousMonths = listOf(
            MonthlyCompletion(getPreviousMonthName(1), 0),
            MonthlyCompletion(getPreviousMonthName(2), 0)
        )

        _uiState.value = _uiState.value.copy(
            currentMonthCompletionPercent = currentMonthCompletion,
            previousMonthStats = previousMonths
        )
    }

    /**
     * Get current month name in French
     */
    private fun getCurrentMonthName(): String {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.FRENCH)
        return monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    /**
     * Get previous month name (monthsBack = 1 for last month, 2 for two months ago)
     */
    private fun getPreviousMonthName(monthsBack: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsBack)
        val monthFormat = SimpleDateFormat("MMMM", Locale.FRENCH)
        return monthFormat.format(calendar.time).replaceFirstChar { it.uppercase() }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                Timber.d("Profile data loading triggered (managed by SyncManager)")
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
        Timber.d("toggleGoal called but not implemented: $goalId")
    }

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

    /**
     * Build practice times from Firestore stats
     */
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
}
