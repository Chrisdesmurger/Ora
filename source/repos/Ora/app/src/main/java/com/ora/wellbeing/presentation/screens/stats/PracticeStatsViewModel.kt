package com.ora.wellbeing.presentation.screens.stats

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeStatsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
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

    private fun loadPracticeStats(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Simuler un chargement réseau
                delay(500)

                // Données mock basées sur le practiceId
                val mockData = getMockPracticeStats(practiceId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        practiceDetails = mockData.first,
                        weeklyStats = mockData.second,
                        practiceBreakdown = mockData.third,
                        sessionHistory = mockData.fourth
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des statistiques"
                    )
                }
            }
        }
    }

    /**
     * Génère des données mock pour différentes pratiques
     */
    private fun getMockPracticeStats(practiceId: String): Quartet<
            PracticeDetails,
            List<WeeklyDataPoint>,
            List<PracticeTypeBreakdown>,
            List<SessionHistoryItem>
            > {
        return when (practiceId.lowercase()) {
            "yoga" -> {
                val details = PracticeDetails(
                    id = "yoga",
                    name = "Yoga",
                    totalTime = "6h 30m",
                    regularityDays = 18,
                    sessionsCount = 9,
                    monthlyTime = "3h 45 ce mois-ci",
                    growthPercentage = 20
                )

                val weeklyData = listOf(
                    WeeklyDataPoint("L", 25, 1),
                    WeeklyDataPoint("M", 30, 2),
                    WeeklyDataPoint("M", 40, 3),
                    WeeklyDataPoint("J", 42, 4),
                    WeeklyDataPoint("V", 60, 5),
                    WeeklyDataPoint("D", 35, 7)
                )

                val breakdown = listOf(
                    PracticeTypeBreakdown("Yoga doux", 35, Color(0xFFF4845F), 140),
                    PracticeTypeBreakdown("Yoga danse", 50, Color(0xFFFDB5A0), 200),
                    PracticeTypeBreakdown("Yoga power", 15, Color(0xFF7BA089), 60)
                )

                val history = listOf(
                    SessionHistoryItem("1", "14 avr.", "45 min", "Yoga doux du matin"),
                    SessionHistoryItem("2", "9 avr.", "20 min", "Yoga express"),
                    SessionHistoryItem("3", "2 avr.", "40 min", "Yoga flow"),
                )

                Quartet(details, weeklyData, breakdown, history)
            }

            "pilates" -> {
                val details = PracticeDetails(
                    id = "pilates",
                    name = "Pilates",
                    totalTime = "4h 15m",
                    regularityDays = 12,
                    sessionsCount = 6,
                    monthlyTime = "2h 15 ce mois-ci",
                    growthPercentage = 15
                )

                val weeklyData = listOf(
                    WeeklyDataPoint("L", 20, 1),
                    WeeklyDataPoint("M", 35, 2),
                    WeeklyDataPoint("J", 30, 4),
                    WeeklyDataPoint("V", 45, 5),
                    WeeklyDataPoint("D", 25, 7)
                )

                val breakdown = listOf(
                    PracticeTypeBreakdown("Pilates classique", 60, Color(0xFFFDB5A0), 152),
                    PracticeTypeBreakdown("Pilates reformer", 40, Color(0xFFB4D4C3), 103)
                )

                val history = listOf(
                    SessionHistoryItem("1", "12 avr.", "30 min", "Pilates core"),
                    SessionHistoryItem("2", "8 avr.", "25 min", "Pilates débutant"),
                )

                Quartet(details, weeklyData, breakdown, history)
            }

            "meditation" -> {
                val details = PracticeDetails(
                    id = "meditation",
                    name = "Méditation",
                    totalTime = "8h 20m",
                    regularityDays = 25,
                    sessionsCount = 30,
                    monthlyTime = "5h 10 ce mois-ci",
                    growthPercentage = 35
                )

                val weeklyData = listOf(
                    WeeklyDataPoint("L", 15, 1),
                    WeeklyDataPoint("M", 20, 2),
                    WeeklyDataPoint("M", 15, 3),
                    WeeklyDataPoint("J", 25, 4),
                    WeeklyDataPoint("V", 20, 5),
                    WeeklyDataPoint("S", 10, 6),
                    WeeklyDataPoint("D", 30, 7)
                )

                val breakdown = listOf(
                    PracticeTypeBreakdown("Méditation guidée", 45, Color(0xFF7BA089), 225),
                    PracticeTypeBreakdown("Pleine conscience", 35, Color(0xFFB4D4C3), 175),
                    PracticeTypeBreakdown("Méditation sommeil", 20, Color(0xFFF4845F), 100)
                )

                val history = listOf(
                    SessionHistoryItem("1", "15 avr.", "10 min", "Méditation du matin"),
                    SessionHistoryItem("2", "14 avr.", "15 min", "Scan corporel"),
                    SessionHistoryItem("3", "13 avr.", "20 min", "Méditation du soir"),
                )

                Quartet(details, weeklyData, breakdown, history)
            }

            "respiration", "breathing" -> {
                val details = PracticeDetails(
                    id = "respiration",
                    name = "Respiration",
                    totalTime = "2h 45m",
                    regularityDays = 15,
                    sessionsCount = 20,
                    monthlyTime = "1h 30 ce mois-ci",
                    growthPercentage = 10
                )

                val weeklyData = listOf(
                    WeeklyDataPoint("L", 10, 1),
                    WeeklyDataPoint("M", 8, 2),
                    WeeklyDataPoint("M", 12, 3),
                    WeeklyDataPoint("J", 15, 4),
                    WeeklyDataPoint("V", 10, 5),
                    WeeklyDataPoint("S", 5, 6),
                    WeeklyDataPoint("D", 8, 7)
                )

                val breakdown = listOf(
                    PracticeTypeBreakdown("Respiration carrée", 40, Color(0xFF7BA089), 66),
                    PracticeTypeBreakdown("Cohérence cardiaque", 35, Color(0xFFFDB5A0), 58),
                    PracticeTypeBreakdown("Respiration anti-stress", 25, Color(0xFFB4D4C3), 41)
                )

                val history = listOf(
                    SessionHistoryItem("1", "14 avr.", "5 min", "Respiration calme"),
                    SessionHistoryItem("2", "10 avr.", "8 min", "Cohérence cardiaque"),
                )

                Quartet(details, weeklyData, breakdown, history)
            }

            else -> {
                // Pratique par défaut
                val details = PracticeDetails(
                    id = practiceId,
                    name = practiceId.replaceFirstChar { it.uppercase() },
                    totalTime = "2h 00m",
                    regularityDays = 8,
                    sessionsCount = 5,
                    monthlyTime = "1h 30 ce mois-ci",
                    growthPercentage = 5
                )

                val weeklyData = listOf(
                    WeeklyDataPoint("L", 15, 1),
                    WeeklyDataPoint("M", 20, 2),
                    WeeklyDataPoint("V", 25, 5)
                )

                val breakdown = listOf(
                    PracticeTypeBreakdown("Type A", 60, Color(0xFFF4845F), 72),
                    PracticeTypeBreakdown("Type B", 40, Color(0xFF7BA089), 48)
                )

                val history = listOf(
                    SessionHistoryItem("1", "10 avr.", "30 min", "Séance 1"),
                )

                Quartet(details, weeklyData, breakdown, history)
            }
        }
    }
}

/**
 * Helper class pour retourner 4 valeurs
 */
data class Quartet<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)