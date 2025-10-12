package com.ora.wellbeing.presentation.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.data.model.DailyJournalEntry
import com.ora.wellbeing.data.model.MoodType
import com.ora.wellbeing.domain.repository.DailyJournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DailyJournalViewModel @Inject constructor(
    private val dailyJournalRepository: DailyJournalRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyJournalUiState())
    val uiState: StateFlow<DailyJournalUiState> = _uiState.asStateFlow()

    init {
        loadTodayEntry()
    }

    fun onEvent(event: DailyJournalUiEvent) {
        when (event) {
            is DailyJournalUiEvent.LoadEntry -> loadEntry(event.date)
            is DailyJournalUiEvent.UpdateMood -> updateMood(event.mood)
            is DailyJournalUiEvent.UpdateShortNote -> updateShortNote(event.note)
            is DailyJournalUiEvent.UpdateDailyStory -> updateDailyStory(event.story)
            is DailyJournalUiEvent.UpdateGratitude -> updateGratitude(event.index, event.text)
            is DailyJournalUiEvent.UpdateAccomplishment -> updateAccomplishment(event.index, event.text)
            is DailyJournalUiEvent.AddAccomplishment -> addAccomplishment()
            is DailyJournalUiEvent.RemoveAccomplishment -> removeAccomplishment(event.index)
            is DailyJournalUiEvent.UpdateImprovement -> updateImprovement(event.index, event.text)
            is DailyJournalUiEvent.UpdateLearnings -> updateLearnings(event.learnings)
            is DailyJournalUiEvent.UpdateRemindMeTomorrow -> updateRemindMeTomorrow(event.remind)
            is DailyJournalUiEvent.SaveEntry -> saveEntry()
            is DailyJournalUiEvent.DeleteEntry -> deleteEntry(event.date)
        }
    }

    private fun loadTodayEntry() {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("loadTodayEntry: No authenticated user")
            _uiState.value = _uiState.value.copy(
                error = "Vous devez être connecté"
            )
            return
        }

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        loadEntry(today)
    }

    private fun loadEntry(date: String?) {
        val uid = auth.currentUser?.uid ?: return
        val targetDate = date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val result = dailyJournalRepository.getEntryByDate(uid, targetDate)
                result.fold(
                    onSuccess = { entry ->
                        if (entry != null) {
                            // Load existing entry
                            _uiState.value = DailyJournalUiState(
                                isLoading = false,
                                currentDate = targetDate,
                                mood = entry.mood,
                                shortNote = entry.shortNote,
                                dailyStory = entry.dailyStory,
                                gratitudes = entry.gratitudes.toMutableList().apply {
                                    while (size < 3) add("")
                                },
                                accomplishments = entry.accomplishments.toMutableList().apply {
                                    if (isEmpty()) add("")
                                },
                                improvements = entry.improvements.toMutableList().apply {
                                    while (size < 3) add("")
                                },
                                learnings = entry.learnings,
                                remindMeTomorrow = entry.remindMeTomorrow,
                                isExistingEntry = true
                            )
                            Timber.d("loadEntry: Loaded entry for $targetDate")
                        } else {
                            // New entry
                            _uiState.value = DailyJournalUiState(
                                isLoading = false,
                                currentDate = targetDate,
                                gratitudes = mutableListOf("", "", ""),
                                accomplishments = mutableListOf(""),
                                improvements = mutableListOf("", "", "")
                            )
                            Timber.d("loadEntry: Creating new entry for $targetDate")
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Erreur lors du chargement: ${error.message}"
                        )
                        Timber.e(error, "loadEntry: Error loading entry")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur: ${e.message}"
                )
                Timber.e(e, "loadEntry: Exception")
            }
        }
    }

    private fun updateMood(mood: String) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    private fun updateShortNote(note: String) {
        _uiState.value = _uiState.value.copy(shortNote = note.take(200))
    }

    private fun updateDailyStory(story: String) {
        _uiState.value = _uiState.value.copy(dailyStory = story.take(2000))
    }

    private fun updateGratitude(index: Int, text: String) {
        val gratitudes = _uiState.value.gratitudes.toMutableList()
        if (index in gratitudes.indices) {
            gratitudes[index] = text
            _uiState.value = _uiState.value.copy(gratitudes = gratitudes)
        }
    }

    private fun updateAccomplishment(index: Int, text: String) {
        val accomplishments = _uiState.value.accomplishments.toMutableList()
        if (index in accomplishments.indices) {
            accomplishments[index] = text
            _uiState.value = _uiState.value.copy(accomplishments = accomplishments)
        }
    }

    private fun addAccomplishment() {
        val accomplishments = _uiState.value.accomplishments.toMutableList()
        accomplishments.add("")
        _uiState.value = _uiState.value.copy(accomplishments = accomplishments)
    }

    private fun removeAccomplishment(index: Int) {
        val accomplishments = _uiState.value.accomplishments.toMutableList()
        if (index in accomplishments.indices && accomplishments.size > 1) {
            accomplishments.removeAt(index)
            _uiState.value = _uiState.value.copy(accomplishments = accomplishments)
        }
    }

    private fun updateImprovement(index: Int, text: String) {
        val improvements = _uiState.value.improvements.toMutableList()
        if (index in improvements.indices) {
            improvements[index] = text
            _uiState.value = _uiState.value.copy(improvements = improvements)
        }
    }

    private fun updateLearnings(learnings: String) {
        _uiState.value = _uiState.value.copy(learnings = learnings.take(1000))
    }

    private fun updateRemindMeTomorrow(remind: Boolean) {
        _uiState.value = _uiState.value.copy(remindMeTomorrow = remind)
    }

    private fun saveEntry() {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("saveEntry: No authenticated user")
            _uiState.value = _uiState.value.copy(error = "Vous devez être connecté")
            return
        }

        val state = _uiState.value

        // Validation
        if (state.mood.isEmpty()) {
            _uiState.value = state.copy(error = "Veuillez sélectionner une humeur")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true, error = null)

                // Filter out empty strings from lists
                val gratitudes = state.gratitudes.filter { it.isNotBlank() }
                val accomplishments = state.accomplishments.filter { it.isNotBlank() }
                val improvements = state.improvements.filter { it.isNotBlank() }

                val entry = DailyJournalEntry.createForDate(
                    uid = uid,
                    date = state.currentDate,
                    mood = state.mood,
                    shortNote = state.shortNote,
                    dailyStory = state.dailyStory,
                    gratitudes = gratitudes,
                    accomplishments = accomplishments,
                    improvements = improvements,
                    learnings = state.learnings,
                    remindMeTomorrow = state.remindMeTomorrow
                )

                val result = dailyJournalRepository.saveDailyEntry(entry)
                result.fold(
                    onSuccess = {
                        _uiState.value = state.copy(
                            isSaving = false,
                            saveSuccess = true,
                            isExistingEntry = true
                        )
                        Timber.i("saveEntry: Entry saved successfully for ${state.currentDate}")
                    },
                    onFailure = { error ->
                        _uiState.value = state.copy(
                            isSaving = false,
                            error = "Erreur lors de la sauvegarde: ${error.message}"
                        )
                        Timber.e(error, "saveEntry: Error saving entry")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = "Erreur: ${e.message}"
                )
                Timber.e(e, "saveEntry: Exception")
            }
        }
    }

    private fun deleteEntry(date: String) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val result = dailyJournalRepository.deleteEntry(uid, date)
                result.fold(
                    onSuccess = {
                        Timber.i("deleteEntry: Entry deleted for $date")
                        // Reset to new entry state
                        loadEntry(date)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = "Erreur lors de la suppression: ${error.message}"
                        )
                        Timber.e(error, "deleteEntry: Error")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "deleteEntry: Exception")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}

/**
 * UI State for Daily Journal Entry Screen
 */
data class DailyJournalUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val currentDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val mood: String = "",
    val shortNote: String = "",
    val dailyStory: String = "",
    val gratitudes: MutableList<String> = mutableListOf("", "", ""),
    val accomplishments: MutableList<String> = mutableListOf(""),
    val improvements: MutableList<String> = mutableListOf("", "", ""),
    val learnings: String = "",
    val remindMeTomorrow: Boolean = false,
    val isExistingEntry: Boolean = false
) {
    val isValid: Boolean
        get() = mood.isNotEmpty()

    val formattedDate: String
        get() = try {
            val localDate = LocalDate.parse(currentDate, DateTimeFormatter.ISO_LOCAL_DATE)
            localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.FRENCH))
        } catch (e: Exception) {
            currentDate
        }
}

/**
 * UI Events for Daily Journal Entry Screen
 */
sealed interface DailyJournalUiEvent {
    data class LoadEntry(val date: String?) : DailyJournalUiEvent
    data class UpdateMood(val mood: String) : DailyJournalUiEvent
    data class UpdateShortNote(val note: String) : DailyJournalUiEvent
    data class UpdateDailyStory(val story: String) : DailyJournalUiEvent
    data class UpdateGratitude(val index: Int, val text: String) : DailyJournalUiEvent
    data class UpdateAccomplishment(val index: Int, val text: String) : DailyJournalUiEvent
    data object AddAccomplishment : DailyJournalUiEvent
    data class RemoveAccomplishment(val index: Int) : DailyJournalUiEvent
    data class UpdateImprovement(val index: Int, val text: String) : DailyJournalUiEvent
    data class UpdateLearnings(val learnings: String) : DailyJournalUiEvent
    data class UpdateRemindMeTomorrow(val remind: Boolean) : DailyJournalUiEvent
    data object SaveEntry : DailyJournalUiEvent
    data class DeleteEntry(val date: String) : DailyJournalUiEvent
}
