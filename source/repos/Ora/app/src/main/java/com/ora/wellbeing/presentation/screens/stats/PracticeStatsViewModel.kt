package com.ora.wellbeing.presentation.screens.stats

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.repository.PracticeStatsRepository
import com.ora.wellbeing.data.sync.SyncManager
import com.ora.wellbeing.domain.model.PracticeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// FIX(stats): Connect to real Firestore data instead of mock data
@HiltViewModel
class PracticeStatsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val practiceStatsRepository: PracticeStatsRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeStatsUiState())
    val uiState: StateFlow<PracticeStatsUiState> = _uiState.asStateFlow()

    init {
        val practiceId = savedStateHandle.get<String>("practiceId")
        practiceId?.let {
            onEvent(PracticeStatsUiEvent.LoadPracticeStats(it))
        }
    }

    fun onEvent(event: PracticeStatsUiEvent) {
        when (event) {
            is PracticeStatsUiEvent.LoadPracticeStats -> {
                loadPracticeStats(event.practiceId)
            }
            is PracticeStatsUiEvent.NavigateBack -> {
                // Géré par la navigation
            }
            is PracticeStatsUiEvent.StartNewSession -> {
                // TODO: Naviguer vers l'écran de session
            }
        }
    }

    // FIX(stats): Load real data from Firestore instead of mock data
    private fun loadPracticeStats(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val profile = syncManager.userProfile.value
                if (profile == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Utilisateur non connecté"
                        )
                    }
                    return@launch
                }

                // Observer les stats pour ce type de pratique en temps réel
                practiceStatsRepository.observePracticeStats(profile.uid)
                    .collect { allStats ->
                        // Filtrer pour obtenir les stats du type demandé
                        val practiceStats = allStats.find { it.practiceType == practiceId }

                        if (practiceStats != null) {
                            // Charger les sessions récentes pour l'historique et le graphique
                            val sessionsResult = practiceStatsRepository.getSessionsByType(
                                uid = profile.uid,
                                practiceType = practiceId,
                                limit = 50
                            )

                            sessionsResult.onSuccess { sessions ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        practiceDetails = buildPracticeDetails(practiceId, practiceStats),
                                        weeklyStats = buildWeeklyStats(sessions),
                                        practiceBreakdown = emptyList(), // TODO: Implement breakdown by content type
                                        sessionHistory = buildSessionHistory(sessions)
                                    )
                                }
                            }.onFailure { error ->
                                Timber.e(error, "Error loading sessions")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        practiceDetails = buildPracticeDetails(practiceId, practiceStats),
                                        weeklyStats = emptyList(),
                                        sessionHistory = emptyList()
                                    )
                                }
                            }
                        } else {
                            // Aucune donnée pour ce type de pratique - afficher 0
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    practiceDetails = buildEmptyPracticeDetails(practiceId),
                                    weeklyStats = emptyList(),
                                    practiceBreakdown = emptyList(),
                                    sessionHistory = emptyList()
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading practice stats")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des statistiques"
                    )
                }
            }
        }
    }

    // FIX(stats): Build PracticeDetails from real Firestore data
    private fun buildPracticeDetails(practiceId: String, stats: PracticeStats): PracticeDetails {
        val practiceName = when (practiceId) {
            "yoga" -> "Yoga"
            "pilates" -> "Pilates"
            "meditation" -> "Méditation"
            "breathing" -> "Respiration"
            else -> practiceId.replaceFirstChar { it.uppercase() }
        }

        return PracticeDetails(
            id = practiceId,
            name = practiceName,
            totalTime = formatMinutesToTime(stats.totalMinutes),
            regularityDays = 0, // TODO: Calculate from session dates
            sessionsCount = stats.totalSessions,
            monthlyTime = "${stats.minutesThisMonth} min ce mois-ci",
            growthPercentage = 0 // TODO: Calculate growth percentage
        )
    }

    // FIX(stats): Build empty PracticeDetails when no data exists
    private fun buildEmptyPracticeDetails(practiceId: String): PracticeDetails {
        val practiceName = when (practiceId) {
            "yoga" -> "Yoga"
            "pilates" -> "Pilates"
            "meditation" -> "Méditation"
            "breathing" -> "Respiration"
            else -> practiceId.replaceFirstChar { it.uppercase() }
        }

        return PracticeDetails(
            id = practiceId,
            name = practiceName,
            totalTime = "0 min",
            regularityDays = 0,
            sessionsCount = 0,
            monthlyTime = "0 min ce mois-ci",
            growthPercentage = 0
        )
    }

    // FIX(stats): Build weekly stats from sessions
    private fun buildWeeklyStats(sessions: List<com.ora.wellbeing.domain.model.PracticeSession>): List<WeeklyDataPoint> {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Obtenir le début de la semaine (lundi)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis

        // Grouper les sessions par jour de la semaine
        val sessionsByDay = sessions
            .filter { it.completedAt >= weekStart }
            .groupBy { session ->
                val sessionCalendar = Calendar.getInstance().apply {
                    timeInMillis = session.completedAt
                }
                sessionCalendar.get(Calendar.DAY_OF_WEEK)
            }

        // Créer les points de données pour chaque jour de la semaine
        val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")
        val weekDays = listOf(
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
        )

        return weekDays.mapIndexed { index, dayOfWeek ->
            val minutesForDay = sessionsByDay[dayOfWeek]?.sumOf { it.durationMinutes } ?: 0
            WeeklyDataPoint(
                dayLabel = dayLabels[index],
                minutes = minutesForDay,
                dayOfWeek = dayOfWeek
            )
        }.filter { it.minutes > 0 } // Ne montrer que les jours avec des sessions
    }

    // FIX(stats): Build session history from sessions
    private fun buildSessionHistory(sessions: List<com.ora.wellbeing.domain.model.PracticeSession>): List<SessionHistoryItem> {
        val dateFormat = SimpleDateFormat("d MMM", Locale.FRENCH)

        return sessions
            .sortedByDescending { it.completedAt }
            .take(10) // Limiter aux 10 dernières sessions
            .map { session ->
                SessionHistoryItem(
                    id = session.id,
                    date = dateFormat.format(Date(session.completedAt)),
                    duration = "${session.durationMinutes} min",
                    sessionName = session.contentTitle
                )
            }
    }

    // Helper: Format minutes to "Xh Ym" format
    private fun formatMinutesToTime(minutes: Int): String {
        if (minutes == 0) return "0 min"

        val hours = minutes / 60
        val mins = minutes % 60

        return when {
            hours == 0 -> "${mins} min"
            mins == 0 -> "${hours}h"
            else -> "${hours}h ${mins}m"
        }
    }

    // FIX(stats): Removed getMockPracticeStats() - now using real Firestore data
}