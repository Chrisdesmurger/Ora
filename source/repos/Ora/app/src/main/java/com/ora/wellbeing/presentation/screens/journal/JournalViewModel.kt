package com.ora.wellbeing.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.model.GratitudeEntry
import com.ora.wellbeing.domain.repository.GratitudeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val gratitudeRepository: GratitudeRepository,
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
                error = "Vous devez être connecté pour voir vos gratitudes"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Combine today's entry, recent entries, and calculate stats
                combine(
                    gratitudeRepository.getTodayEntry(uid),
                    gratitudeRepository.getRecentEntries(uid, limit = 10)
                ) { todayEntry, recentEntries ->
                    Triple(todayEntry, recentEntries, uid)
                }.collect { (todayEntry, recentEntries, currentUid) ->
                    // Calculate streak and stats
                    val streak = gratitudeRepository.calculateStreak(currentUid)
                    val totalCount = gratitudeRepository.getTotalEntryCount(currentUid)
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
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement du journal: ${e.message}"
                )
                Timber.e(e, "Error observing gratitude data")
            }
        }
    }

    private fun saveGratitudes(gratitudes: List<String>, mood: String?, notes: String?) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("saveGratitudes: No authenticated user")
            _uiState.value = _uiState.value.copy(error = "Vous devez être connecté")
            return
        }

        viewModelScope.launch {
            try {
                val entry = GratitudeEntry.createForToday(uid, gratitudes, mood, notes)

                val result = gratitudeRepository.createEntry(entry)
                result.fold(
                    onSuccess = {
                        Timber.i("saveGratitudes: Entry saved successfully")
                        // UI state will update automatically via Flow
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Erreur lors de la sauvegarde: ${error.message}"
                        )
                        Timber.e(error, "Error saving gratitude entry")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la sauvegarde: ${e.message}"
                )
                Timber.e(e, "Error saving gratitude entry")
            }
        }
    }

    private fun deleteEntry(entryId: String) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("deleteEntry: No authenticated user")
            return
        }

        viewModelScope.launch {
            try {
                // Entry ID is the date (yyyy-MM-dd)
                val result = gratitudeRepository.deleteEntry(uid, entryId)
                result.fold(
                    onSuccess = {
                        Timber.i("deleteEntry: Entry $entryId deleted successfully")
                        // UI state will update automatically via Flow
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
     * Converts GratitudeEntry (data model) to JournalEntry (UI model)
     */
    private fun GratitudeEntry.toUiEntry(): JournalUiState.JournalEntry {
        return JournalUiState.JournalEntry(
            id = date, // Use date as ID
            date = date,
            formattedDate = getFormattedDate(),
            gratitudes = gratitudes,
            mood = mood ?: "",
            notes = notes ?: "",
            createdAt = createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            updatedAt = updatedAt?.toDate()?.time ?: System.currentTimeMillis()
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
    data class SaveGratitudes(val gratitudes: List<String>, val mood: String?, val notes: String?) : JournalUiEvent
}
