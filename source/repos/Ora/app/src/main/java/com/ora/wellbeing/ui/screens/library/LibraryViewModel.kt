package com.ora.wellbeing.ui.screens.library

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
import com.ora.wellbeing.data.player.VideoPlayerManager
import javax.inject.Inject

data class LibraryUiState(
    val isLoading: Boolean = false,
    val selectedFilter: ContentCategory? = null,
    val meditationVideos: List<VideoContent> = emptyList(),
    val yogaVideos: List<VideoContent> = emptyList(),
    val pilatesVideos: List<VideoContent> = emptyList(),
    val breathingVideos: List<VideoContent> = emptyList(),
    val error: String? = null
)

sealed class LibraryUiEvent {
    object LoadLibrary : LibraryUiEvent()
    data class FilterByCategory(val category: ContentCategory?) : LibraryUiEvent()
    data class VideoClicked(val video: VideoContent) : LibraryUiEvent()
    data class CategoryClicked(val category: ContentCategory) : LibraryUiEvent()
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: OraRepository,
    private val videoPlayerManager: VideoPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.LoadLibrary -> loadLibrary()
            is LibraryUiEvent.FilterByCategory -> filterByCategory(event.category)
            is LibraryUiEvent.VideoClicked -> playVideo(event.video)
            is LibraryUiEvent.CategoryClicked -> {
                // Navigation sera gérée par l'UI
            }
        }
    }

    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val meditationVideos = repository.getVideosByCategory(ContentCategory.MEDITATION)
                val yogaVideos = repository.getVideosByCategory(ContentCategory.YOGA)
                val pilatesVideos = repository.getVideosByCategory(ContentCategory.PILATES)
                val breathingVideos = repository.getVideosByCategory(ContentCategory.BREATHING)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    meditationVideos = meditationVideos,
                    yogaVideos = yogaVideos,
                    pilatesVideos = pilatesVideos,
                    breathingVideos = breathingVideos
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun filterByCategory(category: ContentCategory?) {
        _uiState.value = _uiState.value.copy(selectedFilter = category)
    }

    private fun playVideo(video: VideoContent) {
        viewModelScope.launch {
            try {
                videoPlayerManager.playVideo(video.videoUrl)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        videoPlayerManager.release()
    }
}