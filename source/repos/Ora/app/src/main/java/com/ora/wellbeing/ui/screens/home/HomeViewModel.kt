package com.ora.wellbeing.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ora.wellbeing.domain.model.VideoContent
import com.ora.wellbeing.domain.model.ContentCategory
import com.ora.wellbeing.data.repository.OraRepository
import com.ora.wellbeing.data.worker.ReminderScheduler
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val featuredVideo: VideoContent? = null,
    val categories: List<ContentCategory> = ContentCategory.values().toList(),
    val error: String? = null
)

sealed class HomeUiEvent {
    object LoadContent : HomeUiEvent()
    data class CategoryClicked(val category: ContentCategory) : HomeUiEvent()
    object GetStartedClicked : HomeUiEvent()
    object FeaturedVideoClicked : HomeUiEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: OraRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadContent()
        scheduleEveningReminder()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.LoadContent -> loadContent()
            is HomeUiEvent.CategoryClicked -> {
                // Navigation sera gérée par l'UI
            }
            is HomeUiEvent.GetStartedClicked -> {
                // Navigation vers la première session
            }
            is HomeUiEvent.FeaturedVideoClicked -> {
                // Lancer la vidéo featured
            }
        }
    }

    private fun loadContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val featuredVideo = repository.getFeaturedVideo()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    featuredVideo = featuredVideo
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun scheduleEveningReminder() {
        viewModelScope.launch {
            try {
                // Programmer le rappel du soir à 20h00
                reminderScheduler.scheduleEveningReminder(hourOfDay = 20, minute = 0)
            } catch (e: Exception) {
                // Log l'erreur sans affecter l'UI
            }
        }
    }
}