package com.ora.wellbeing.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.model.DailyJournalEntry
import com.ora.wellbeing.domain.repository.DailyJournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val dailyJournalRepository: DailyJournalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        observeGratitudeData()
    }

    fun onEvent(event: JournalUiEvent) {
        when (event) {
            is JournalUiEvent.LoadJournalData -> observeGratitudeData()
            is JournalUiEvent.DeleteEntry -> deleteEntry(event.entryId)
            is JournalUiEvent.SaveGratitudes -> saveGratitudes(event.gratitudes, event.mood, event.notes)
        }
    }

    private fun observeGratitudeData() {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("observeGratitudeData: No authenticated user")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Vous devez être connecté pour voir votre journal"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get today's date
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Get today's entry
                val todayResult = dailyJournalRepository.getEntryByDate(uid, today)
                val todayEntry = todayResult.getOrNull()

                // Get recent entries (last 10)
                val recentResult = dailyJournalRepository.getRecentEntries(uid, limit = 10)
                val recentEntries = recentResult.getOrNull() ?: emptyList()

                // Calculate streak (consecutive days with entries)
                val currentMonth = YearMonth.now()
                val monthEntriesResult = dailyJournalRepository.observeEntriesForMonth(
                    uid,
                    currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                )

                // Simple streak calculation: count how many of the last N days have entries
                val streak = calculateSimpleStreak(recentEntries)
                val totalCount = recentEntries.size
                val thisMonthCount = recentEntries.count { entry ->
                    val entryDate = LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE)
                    entryDate.month == LocalDate.now().month && entryDate.year == LocalDate.now().year
                }

                _uiState.value = JournalUiState(
                    isLoading = false,
                    error = null,
                    todayEntry = todayEntry?.toUiEntry(),
                    recentEntries = recentEntries.map { it.toUiEntry() },
                    totalEntries = totalCount,
                    gratitudeStreak = streak,
                    thisMonthEntries = thisMonthCount
                )

                Timber.d("observeGratitudeData: Updated UI state (total=$totalCount, streak=$streak)")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du journal: ${e.message}"
                )
                Timber.e(e, "Error observing journal data")
            }
        }
    }

    private fun calculateSimpleStreak(entries: List<DailyJournalEntry>): Int {
        if (entries.isEmpty()) return 0

        val sortedDates = entries.map {
            LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE)
        }.sortedDescending()

        var streak = 0
        var expectedDate = LocalDate.now()

        for (date in sortedDates) {
            if (date == expectedDate || date == expectedDate.minusDays(1)) {
                streak++
                expectedDate = date.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }

    private fun saveGratitudes(gratitudes: List<String>, mood: String?, notes: String?) {
        // This method is deprecated - use DailyJournalEntryScreen instead
        Timber.w("saveGratitudes: Deprecated method called, use DailyJournalEntryScreen instead")
    }

    private fun deleteEntry(entryId: String) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("deleteEntry: No authenticated user")
            return
        }

        viewModelScope.launch {
            try {
                // Entry ID is the date (yyyy-MM-dd)
                val result = dailyJournalRepository.deleteEntry(uid, entryId)
                result.fold(
                    onSuccess = {
                        Timber.i("deleteEntry: Entry $entryId deleted successfully")
                        // Reload data after deletion
                        observeGratitudeData()
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Erreur lors de la suppression: ${error.message}"
                        )
                        Timber.e(error, "Error deleting entry")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression: ${e.message}"
                )
                Timber.e(e, "Error deleting journal entry")
            }
        }
    }

    /**
     * Converts DailyJournalEntry (data model) to JournalEntry (UI model)
     */
    private fun DailyJournalEntry.toUiEntry(): JournalUiState.JournalEntry {
        val moodEmoji = when (mood) {
            "happy" -> "😊"
            "neutral" -> "😐"
            "sad" -> "😕"
            "frustrated" -> "😠"
            else -> ""
        }

        return JournalUiState.JournalEntry(
            id = date, // Use date as ID
            date = date,
            formattedDate = getFormattedDate(),
            gratitudes = gratitudes,
            mood = moodEmoji,
            notes = shortNote,
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis()
        )
    }

    private fun DailyJournalEntry.getFormattedDate(): String {
        return try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.FRENCH))
        } catch (e: Exception) {
            date
        }
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
    data class SaveGratitudes(val gratitudes: List<String>, val mood: String?, val notes: String?) : JournalUiEvent
}
