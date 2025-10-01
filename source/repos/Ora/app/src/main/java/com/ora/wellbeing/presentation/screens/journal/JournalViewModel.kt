package com.ora.wellbeing.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    // TODO: Injecter les use cases quand ils seront créés
    // private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    // private val addJournalEntryUseCase: AddJournalEntryUseCase,
    // private val updateJournalEntryUseCase: UpdateJournalEntryUseCase,
    // private val getGratitudeStatsUseCase: GetGratitudeStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun onEvent(event: JournalUiEvent) {
        when (event) {
            is JournalUiEvent.LoadJournalData -> loadJournalData()
            is JournalUiEvent.DeleteEntry -> deleteEntry(event.entryId)
        }
    }

    private fun loadJournalData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Remplacer par de vraies données depuis les use cases
                val mockData = generateMockJournalData()

                _uiState.value = mockData.copy(isLoading = false)

                Timber.d("Journal data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du journal"
                )
                Timber.e(e, "Error loading journal data")
            }
        }
    }

    private fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            try {
                // TODO: Implémenter la suppression d'entrée
                Timber.d("Entry deleted: $entryId")

                // Recharger les données après suppression
                loadJournalData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression de l'entrée"
                )
                Timber.e(e, "Error deleting journal entry")
            }
        }
    }

    private fun generateMockJournalData(): JournalUiState {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)

        // Créer des entrées d'exemple
        val recentEntries = mutableListOf<JournalUiState.JournalEntry>()

        // Entrée d'hier
        val yesterday = today.minusDays(1)
        recentEntries.add(
            JournalUiState.JournalEntry(
                id = "entry_${yesterday}",
                date = yesterday.toString(),
                formattedDate = yesterday.format(formatter),
                gratitudes = listOf(
                    "Une belle journée ensoleillée",
                    "Un café délicieux ce matin",
                    "Un appel de ma famille"
                ),
                mood = "😊 Joyeux",
                notes = "Une très belle journée dans l'ensemble !"
            )
        )

        // Entrée d'avant-hier
        val dayBefore = today.minusDays(2)
        recentEntries.add(
            JournalUiState.JournalEntry(
                id = "entry_${dayBefore}",
                date = dayBefore.toString(),
                formattedDate = dayBefore.format(formatter),
                gratitudes = listOf(
                    "Terminé un projet important",
                    "Dîner avec des amis",
                    "Une bonne nuit de sommeil"
                ),
                mood = "🙏 Reconnaissant",
                notes = "Productif et social, parfait !"
            )
        )

        // Entrée il y a 3 jours
        val threeDaysAgo = today.minusDays(3)
        recentEntries.add(
            JournalUiState.JournalEntry(
                id = "entry_${threeDaysAgo}",
                date = threeDaysAgo.toString(),
                formattedDate = threeDaysAgo.format(formatter),
                gratitudes = listOf(
                    "Une séance de méditation apaisante",
                    "Un bon livre découvert",
                    "La santé de mes proches"
                ),
                mood = "😌 Paisible",
                notes = "Moment de calme et de réflexion."
            )
        )

        // Déterminer s'il y a une entrée aujourd'hui (simulons que non pour encourager l'utilisateur)
        val todayEntry: JournalUiState.JournalEntry? = null

        return JournalUiState(
            todayEntry = todayEntry,
            recentEntries = recentEntries,
            totalEntries = 15,
            gratitudeStreak = 3, // 3 jours consécutifs
            thisMonthEntries = 8
        )
    }
}

/**
 * État de l'interface utilisateur pour l'écran Journal
 */
data class JournalUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val todayEntry: JournalEntry? = null,
    val recentEntries: List<JournalEntry> = emptyList(),
    val totalEntries: Int = 0,
    val gratitudeStreak: Int = 0,
    val thisMonthEntries: Int = 0
) {
    /**
     * Entrée de journal de gratitudes
     */
    data class JournalEntry(
        val id: String,
        val date: String, // Format ISO (yyyy-MM-dd)
        val formattedDate: String, // Format d'affichage
        val gratitudes: List<String>,
        val mood: String = "",
        val notes: String = "",
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis()
    )
}

/**
 * Événements de l'interface utilisateur pour l'écran Journal
 */
sealed interface JournalUiEvent {
    data object LoadJournalData : JournalUiEvent
    data class DeleteEntry(val entryId: String) : JournalUiEvent
}