package com.ora.wellbeing.feature.practice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.ora.wellbeing.core.data.practice.PracticeRepository
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.core.domain.stats.IncrementSessionUseCase
import com.ora.wellbeing.feature.practice.player.PlaybackSpeed
import com.ora.wellbeing.feature.practice.player.PlayerConfig
import com.ora.wellbeing.feature.practice.player.PlayerState
import com.ora.wellbeing.feature.practice.player.PracticePlayerEnhanced
import com.ora.wellbeing.feature.practice.player.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI State pour le player
 */
data class PlayerUiState(
    val isLoading: Boolean = false,
    val practice: Practice? = null,
    val error: String? = null,
    val playerState: PlayerState = PlayerState(),
    val isFullscreen: Boolean = false,
    val isMinimized: Boolean = false,
    val sessionStartTime: Long = 0L,
    val sessionDuration: Long = 0L,
    val relatedPractices: List<Practice> = emptyList()
)

/**
 * UI Events pour le player
 */
sealed class PlayerUiEvent {
    object TogglePlayPause : PlayerUiEvent()
    object SeekForward : PlayerUiEvent()
    object SeekBackward : PlayerUiEvent()
    data class SeekTo(val position: Long) : PlayerUiEvent()
    data class SetPlaybackSpeed(val speed: PlaybackSpeed) : PlayerUiEvent()
    data class SetRepeatMode(val mode: RepeatMode) : PlayerUiEvent()
    object ToggleFullscreen : PlayerUiEvent()
    object Minimize : PlayerUiEvent()
    object Expand : PlayerUiEvent()
    object Close : PlayerUiEvent()
    object Retry : PlayerUiEvent()
    object EnterPipMode : PlayerUiEvent()
    object ExitPipMode : PlayerUiEvent()
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val practiceRepository: PracticeRepository,
    private val incrementSessionUseCase: IncrementSessionUseCase,
    private val analytics: FirebaseAnalytics
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var player: PracticePlayerEnhanced? = null
    private var sessionTimer: kotlinx.coroutines.Job? = null

    init {
        // Initialize enhanced player with config
        val config = PlayerConfig(
            enableBackgroundAudio = true,
            enablePictureInPicture = true,
            enableRetry = true,
            maxRetryCount = 3
        )

        player = PracticePlayerEnhanced(application.applicationContext, config)

        // Observe player state
        viewModelScope.launch {
            player?.state?.collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }

                // If session completed
                if (playerState.completed && _uiState.value.sessionStartTime > 0L) {
                    onSessionCompleted()
                }

                // Update session duration timer
                if (playerState.isPlaying && _uiState.value.sessionStartTime > 0L) {
                    updateSessionDuration()
                }
            }
        }
    }

    /**
     * Charge la pratique
     */
    fun loadPractice(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            practiceRepository.getById(practiceId).fold(
                onSuccess = { practice ->
                    _uiState.update { it.copy(practice = practice) }

                    // Prepare player
                    player?.prepare(practice.mediaUrl)

                    // Load related practices
                    loadRelatedPractices(practice)

                    _uiState.update { it.copy(isLoading = false) }

                    // Log analytics
                    analytics.logEvent("player_opened") {
                        param("practice_id", practice.id)
                        param("media_type", practice.mediaType.name)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erreur de chargement"
                        )
                    }
                    Timber.e(error, "Error loading practice")
                }
            )
        }
    }

    /**
     * Charge les pratiques similaires
     */
    private suspend fun loadRelatedPractices(practice: Practice) {
        practiceRepository.getSimilar(practice.id).fold(
            onSuccess = { related ->
                _uiState.update { it.copy(relatedPractices = related) }
            },
            onFailure = { error ->
                Timber.e(error, "Error loading related practices")
            }
        )
    }

    /**
     * Gère les événements UI
     */
    fun onEvent(event: PlayerUiEvent) {
        when (event) {
            is PlayerUiEvent.TogglePlayPause -> togglePlayPause()
            is PlayerUiEvent.SeekForward -> player?.seekForward()
            is PlayerUiEvent.SeekBackward -> player?.seekBackward()
            is PlayerUiEvent.SeekTo -> player?.seekTo(event.position)
            is PlayerUiEvent.SetPlaybackSpeed -> setPlaybackSpeed(event.speed)
            is PlayerUiEvent.SetRepeatMode -> setRepeatMode(event.mode)
            is PlayerUiEvent.ToggleFullscreen -> toggleFullscreen()
            is PlayerUiEvent.Minimize -> minimize()
            is PlayerUiEvent.Expand -> expand()
            is PlayerUiEvent.Close -> close()
            is PlayerUiEvent.Retry -> retry()
            is PlayerUiEvent.EnterPipMode -> enterPipMode()
            is PlayerUiEvent.ExitPipMode -> exitPipMode()
        }
    }

    /**
     * Toggle play/pause
     */
    private fun togglePlayPause() {
        if (_uiState.value.playerState.isPlaying) {
            player?.pause()
            stopSessionTimer()
        } else {
            player?.play()
            startSession()
        }
    }

    /**
     * Démarre la session
     */
    private fun startSession() {
        val practice = _uiState.value.practice ?: return

        if (_uiState.value.sessionStartTime == 0L) {
            _uiState.update { it.copy(sessionStartTime = System.currentTimeMillis()) }

            // Log analytics
            analytics.logEvent("session_started") {
                param("practice_id", practice.id)
                param("media_type", practice.mediaType.name)
                param("discipline", practice.discipline.name)
            }

            startSessionTimer()
        }
    }

    /**
     * Démarre le timer de session
     */
    private fun startSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = viewModelScope.launch {
            while (_uiState.value.playerState.isPlaying) {
                kotlinx.coroutines.delay(1000)
                updateSessionDuration()
            }
        }
    }

    /**
     * Arrête le timer de session
     */
    private fun stopSessionTimer() {
        sessionTimer?.cancel()
    }

    /**
     * Met à jour la durée de session
     */
    private fun updateSessionDuration() {
        val startTime = _uiState.value.sessionStartTime
        if (startTime > 0L) {
            val duration = System.currentTimeMillis() - startTime
            _uiState.update { it.copy(sessionDuration = duration) }
        }
    }

    /**
     * Change la vitesse de lecture
     */
    private fun setPlaybackSpeed(speed: PlaybackSpeed) {
        player?.setPlaybackSpeed(speed)

        analytics.logEvent("playback_speed_changed") {
            param("speed", speed.label)
        }
    }

    /**
     * Change le mode de répétition
     */
    private fun setRepeatMode(mode: RepeatMode) {
        player?.setRepeatMode(mode)

        analytics.logEvent("repeat_mode_changed") {
            param("mode", mode.name)
        }
    }

    /**
     * Toggle plein écran
     */
    private fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    /**
     * Minimise le player
     */
    private fun minimize() {
        _uiState.update {
            it.copy(
                isMinimized = true,
                isFullscreen = false
            )
        }

        analytics.logEvent("player_minimized") {
            param("practice_id", _uiState.value.practice?.id ?: "unknown")
        }
    }

    /**
     * Développe le player
     */
    private fun expand() {
        _uiState.update { it.copy(isMinimized = false) }

        analytics.logEvent("player_expanded") {
            param("practice_id", _uiState.value.practice?.id ?: "unknown")
        }
    }

    /**
     * Ferme le player
     */
    private fun close() {
        player?.pause()
        stopSessionTimer()

        // Save current position for resume later
        val position = player?.getCurrentPosition() ?: 0L
        val practice = _uiState.value.practice

        if (practice != null && position > 0L) {
            viewModelScope.launch {
                // TODO: Save resume position to repository
                Timber.d("Saving resume position: $position for practice ${practice.id}")
            }
        }

        _uiState.update {
            it.copy(
                isMinimized = false,
                sessionStartTime = 0L,
                sessionDuration = 0L
            )
        }

        analytics.logEvent("player_closed") {
            param("practice_id", practice?.id ?: "unknown")
            param("position", position)
        }
    }

    /**
     * Réessaye le chargement
     */
    private fun retry() {
        val practiceId = _uiState.value.practice?.id
        if (practiceId != null) {
            loadPractice(practiceId)
        }
    }

    /**
     * Entre en mode Picture-in-Picture
     */
    private fun enterPipMode() {
        if (player?.enterPipMode() == true) {
            analytics.logEvent("pip_mode_entered") {
                param("practice_id", _uiState.value.practice?.id ?: "unknown")
            }
        }
    }

    /**
     * Sort du mode Picture-in-Picture
     */
    private fun exitPipMode() {
        player?.exitPipMode()
        analytics.logEvent("pip_mode_exited") {
            param("practice_id", _uiState.value.practice?.id ?: "unknown")
        }
    }

    /**
     * Session terminée
     */
    private fun onSessionCompleted() {
        val practice = _uiState.value.practice ?: return
        val startTime = _uiState.value.sessionStartTime

        if (startTime == 0L) return

        val actualDuration = (System.currentTimeMillis() - startTime) / 1000 / 60 // in minutes

        viewModelScope.launch {
            // Increment stats
            incrementSessionUseCase(
                durationMin = actualDuration.toInt().coerceAtLeast(practice.durationMin),
                discipline = practice.discipline.name
            ).fold(
                onSuccess = {
                    Timber.d("Stats updated after session completion")
                },
                onFailure = { error ->
                    Timber.e(error, "Error updating stats")
                }
            )

            // Log analytics
            analytics.logEvent("session_completed") {
                param("practice_id", practice.id)
                param("planned_duration", practice.durationMin.toLong())
                param("actual_duration", actualDuration)
                param("completion_rate", (actualDuration / practice.durationMin.toFloat() * 100).toLong())
            }

            // Reset session
            _uiState.update {
                it.copy(
                    sessionStartTime = 0L,
                    sessionDuration = 0L
                )
            }
        }

        stopSessionTimer()
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        player?.release()
        player = null
        Timber.d("PlayerViewModel cleared")
    }
}
