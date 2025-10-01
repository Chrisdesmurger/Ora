package com.ora.wellbeing.presentation.screens.stats

import androidx.compose.ui.graphics.Color

/**
 * État UI pour l'écran de statistiques détaillées d'une pratique
 */
data class PracticeStatsUiState(
    val isLoading: Boolean = true,
    val practiceDetails: PracticeDetails? = null,
    val weeklyStats: List<WeeklyDataPoint> = emptyList(),
    val practiceBreakdown: List<PracticeTypeBreakdown> = emptyList(),
    val sessionHistory: List<SessionHistoryItem> = emptyList(),
    val error: String? = null
)

/**
 * Détails d'une pratique
 */
data class PracticeDetails(
    val id: String,
    val name: String,
    val totalTime: String,         // Ex: "6h 30m"
    val regularityDays: Int,       // Ex: 18
    val sessionsCount: Int,        // Ex: 9
    val monthlyTime: String,       // Ex: "3h 45 ce mois-ci"
    val growthPercentage: Int = 0  // Ex: 20 pour +20% par rapport au mois dernier
)

/**
 * Point de données pour le graphique hebdomadaire
 */
data class WeeklyDataPoint(
    val dayLabel: String,    // L, M, M, J, V, S, D
    val minutes: Int,        // Durée en minutes
    val dayOfWeek: Int       // 1 = Lundi, 7 = Dimanche
)

/**
 * Répartition des types de pratiques (pour pie chart)
 */
data class PracticeTypeBreakdown(
    val typeName: String,    // Ex: "Yoga doux", "Yoga danse", "Yoga power"
    val percentage: Int,     // Ex: 35 pour 35%
    val color: Color,        // Couleur dans le pie chart
    val durationMinutes: Int // Durée totale en minutes
)

/**
 * Item d'historique de séances
 */
data class SessionHistoryItem(
    val id: String,
    val date: String,        // Ex: "14 avr."
    val duration: String,    // Ex: "45 min"
    val sessionName: String  // Ex: "Yoga doux du matin"
)

/**
 * Événements UI pour l'écran de statistiques de pratique
 */
sealed class PracticeStatsUiEvent {
    data class LoadPracticeStats(val practiceId: String) : PracticeStatsUiEvent()
    object NavigateBack : PracticeStatsUiEvent()
    object StartNewSession : PracticeStatsUiEvent()
}