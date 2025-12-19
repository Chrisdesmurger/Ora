package com.ora.wellbeing.feature.practice.player.specialized.yoga

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.ora.wellbeing.core.data.practice.PracticeRepository
import com.ora.wellbeing.feature.practice.player.PlayerConfig
import com.ora.wellbeing.feature.practice.player.PracticePlayerEnhanced
import com.ora.wellbeing.feature.practice.ui.Chapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class YogaPlayerViewModel @Inject constructor(
    application: Application,
    private val practiceRepository: PracticeRepository,
    private val analytics: FirebaseAnalytics
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(YogaPlayerState())
    val uiState: StateFlow<YogaPlayerState> = _uiState.asStateFlow()

    private var player: PracticePlayerEnhanced? = null
    private var sessionTimer: kotlinx.coroutines.Job? = null

    init {
        val config = PlayerConfig(
            enableBackgroundAudio = false, // Vidéo yoga doit rester visible
            enablePictureInPicture = true,
            enableRetry = true,
            maxRetryCount = 3
        )
        player = PracticePlayerEnhanced(application.applicationContext, config)

        viewModelScope.launch {
            player?.state?.collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
                updateCurrentChapter(playerState.currentPosition)
                updateNextPosePreview(playerState.currentPosition)
            }
        }
    }

    fun loadPractice(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            practiceRepository.getById(practiceId).fold(
                onSuccess = { practice ->
                    _uiState.update { it.copy(practice = practice) }
                    player?.prepare(practice.mediaUrl)

                    // Générer les chapitres basés sur la durée
                    val chapters = generateChaptersForPractice(practice.durationMin)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapters = chapters
                        )
                    }

                    analytics.logEvent("yoga_player_opened") {
                        param("practice_id", practice.id)
                        param("discipline", practice.discipline.name)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: getApplication<Application>().getString(com.ora.wellbeing.R.string.yoga_error_loading))
                    }
                    Timber.e(error, "Error loading practice for yoga player")
                }
            )
        }
    }

    fun onEvent(event: YogaPlayerEvent) {
        when (event) {
            is YogaPlayerEvent.TogglePlayPause -> togglePlayPause()
            is YogaPlayerEvent.SeekForward -> player?.seekForward()
            is YogaPlayerEvent.SeekBackward -> player?.seekBackward()
            is YogaPlayerEvent.SeekTo -> player?.seekTo(event.position)
            is YogaPlayerEvent.ToggleFullscreen -> toggleFullscreen()
            is YogaPlayerEvent.Retry -> retry()
            is YogaPlayerEvent.ToggleMirrorMode -> toggleMirrorMode()
            is YogaPlayerEvent.SwitchSide -> switchSide()
            is YogaPlayerEvent.GoToChapter -> goToChapter(event.index)
            is YogaPlayerEvent.NextChapter -> nextChapter()
            is YogaPlayerEvent.PreviousChapter -> previousChapter()
        }
    }

    private fun togglePlayPause() {
        if (_uiState.value.playerState.isPlaying) {
            player?.pause()
            stopSessionTimer()
        } else {
            player?.play()
            startSession()
        }
    }

    private fun startSession() {
        if (_uiState.value.sessionStartTime == 0L) {
            _uiState.update { it.copy(sessionStartTime = System.currentTimeMillis()) }
            startSessionTimer()
        }
    }

    private fun startSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = viewModelScope.launch {
            while (_uiState.value.playerState.isPlaying) {
                kotlinx.coroutines.delay(1000)
                val startTime = _uiState.value.sessionStartTime
                if (startTime > 0L) {
                    _uiState.update {
                        it.copy(sessionDuration = System.currentTimeMillis() - startTime)
                    }
                }
            }
        }
    }

    private fun stopSessionTimer() {
        sessionTimer?.cancel()
    }

    private fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    private fun retry() {
        _uiState.value.practice?.id?.let { loadPractice(it) }
    }

    private fun toggleMirrorMode() {
        val newMirrorMode = !_uiState.value.isMirrorMode
        _uiState.update { it.copy(isMirrorMode = newMirrorMode) }

        analytics.logEvent("yoga_mirror_mode_toggled") {
            param("enabled", newMirrorMode.toString())
        }
    }

    private fun switchSide() {
        val newSide = when (_uiState.value.currentSide) {
            YogaSide.NONE -> YogaSide.LEFT
            YogaSide.LEFT -> YogaSide.RIGHT
            YogaSide.RIGHT -> YogaSide.LEFT
        }
        _uiState.update { it.copy(currentSide = newSide) }

        analytics.logEvent("yoga_side_switched") {
            param("side", newSide.name)
        }
    }

    private fun goToChapter(index: Int) {
        val chapters = _uiState.value.chapters
        if (index in chapters.indices) {
            player?.seekTo(chapters[index].startTime)
            _uiState.update { it.copy(currentChapterIndex = index) }

            analytics.logEvent("yoga_chapter_selected") {
                param("chapter_index", index.toLong())
                param("chapter_title", chapters[index].title)
            }
        }
    }

    private fun nextChapter() {
        val currentIndex = _uiState.value.currentChapterIndex
        val chapters = _uiState.value.chapters
        if (currentIndex < chapters.size - 1) {
            goToChapter(currentIndex + 1)
        }
    }

    private fun previousChapter() {
        val currentIndex = _uiState.value.currentChapterIndex
        if (currentIndex > 0) {
            goToChapter(currentIndex - 1)
        }
    }

    private fun updateCurrentChapter(currentPosition: Long) {
        val chapters = _uiState.value.chapters
        val currentIndex = chapters.indexOfLast { it.startTime <= currentPosition }
        if (currentIndex >= 0 && currentIndex != _uiState.value.currentChapterIndex) {
            _uiState.update { it.copy(currentChapterIndex = currentIndex) }
        }
    }

    private fun updateNextPosePreview(currentPosition: Long) {
        val chapters = _uiState.value.chapters
        val currentIndex = _uiState.value.currentChapterIndex

        if (currentIndex < chapters.size - 1) {
            val nextChapter = chapters[currentIndex + 1]
            val timeUntil = nextChapter.startTime - currentPosition

            if (timeUntil > 0 && timeUntil <= 30000) { // Afficher 30s avant
                _uiState.update {
                    it.copy(
                        nextPosePreview = PosePreview(
                            name = nextChapter.title,
                            timeUntil = timeUntil
                        )
                    )
                }
            } else {
                _uiState.update { it.copy(nextPosePreview = null) }
            }
        }
    }

    private fun generateChaptersForPractice(durationMin: Int): List<Chapter> {
        // Générer des chapitres par défaut basés sur la durée
        val durationMs = durationMin * 60 * 1000L
        val context = getApplication<Application>()
        val chapterNames = listOf(
            context.getString(com.ora.wellbeing.R.string.yoga_chapter_warmup),
            context.getString(com.ora.wellbeing.R.string.yoga_chapter_sun_salutation),
            context.getString(com.ora.wellbeing.R.string.yoga_chapter_standing_poses),
            context.getString(com.ora.wellbeing.R.string.yoga_chapter_floor_poses),
            context.getString(com.ora.wellbeing.R.string.yoga_chapter_relaxation)
        )

        val chapterDuration = durationMs / chapterNames.size

        return chapterNames.mapIndexed { index, name ->
            Chapter(
                title = name,
                startTime = index * chapterDuration
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        player?.release()
        player = null
    }
}
