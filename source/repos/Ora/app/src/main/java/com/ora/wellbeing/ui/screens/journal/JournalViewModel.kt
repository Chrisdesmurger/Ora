package com.ora.wellbeing.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ora.wellbeing.domain.model.Gratitude
import com.ora.wellbeing.domain.model.HabitTracker
import com.ora.wellbeing.data.repository.OraRepository
import java.time.LocalDate
import javax.inject.Inject

data class JournalUiState(
    val isLoading: Boolean = false,
    val todayGratitudes: List<Gratitude> = emptyList(),
    val todayFeeling: String = "",
    val habitTrackers: List<HabitTracker> = emptyList(),
    val currentMonth: LocalDate = LocalDate.now(),
    val error: String? = null
)

sealed class JournalUiEvent {
    object LoadJournalData : JournalUiEvent()
    data class UpdateFeeling(val feeling: String) : JournalUiEvent()
    data class AddGratitude(val gratitude: String) : JournalUiEvent()
    data class ToggleHabit(val habitId: String, val date: LocalDate) : JournalUiEvent()
    object NavigateToDetail : JournalUiEvent()
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: OraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        loadJournalData()
    }

    fun onEvent(event: JournalUiEvent) {
        when (event) {
            is JournalUiEvent.LoadJournalData -> loadJournalData()
            is JournalUiEvent.UpdateFeeling -> updateFeeling(event.feeling)
            is JournalUiEvent.AddGratitude -> addGratitude(event.gratitude)
            is JournalUiEvent.ToggleHabit -> toggleHabit(event.habitId, event.date)
            is JournalUiEvent.NavigateToDetail -> {
                // Navigation sera gérée par l'UI
            }
        }
    }

    private fun loadJournalData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val todayGratitudes = repository.getTodayGratitudes()
                val habitTrackers = repository.getHabitTrackers()
                val todayEntry = repository.getTodayJournalEntry()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    todayGratitudes = todayGratitudes,
                    habitTrackers = habitTrackers,
                    todayFeeling = todayEntry?.story ?: "Je me sens plutôt bien aujourd'hui !"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun updateFeeling(feeling: String) {
        _uiState.value = _uiState.value.copy(todayFeeling = feeling)
    }

    private fun addGratitude(gratitude: String) {
        viewModelScope.launch {
            try {
                val newGratitude = Gratitude(
                    id = System.currentTimeMillis().toString(),
                    text = gratitude,
                    color = "#E8F5E8",
                    date = LocalDate.now()
                )
                repository.saveGratitude(newGratitude)
                loadJournalData() // Recharger les données
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun toggleHabit(habitId: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                repository.toggleHabit(habitId, date)
                loadJournalData() // Recharger les données
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}