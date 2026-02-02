package com.ora.wellbeing.feature.practice.player.specialized.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.ora.wellbeing.core.data.practice.PracticeRepository
import com.ora.wellbeing.feature.practice.player.PlayerConfig
import com.ora.wellbeing.feature.practice.player.PracticePlayerEnhanced
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MeditationPlayerViewModel @Inject constructor(
    application: Application,
    private val practiceRepository: PracticeRepository,
    private val analytics: FirebaseAnalytics
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MeditationPlayerState())
    val uiState: StateFlow<MeditationPlayerState> = _uiState.asStateFlow()

    private var player: PracticePlayerEnhanced? = null
    private var sessionTimer: Job? = null
    private var breathingTimer: Job? = null
    private var sleepTimer: Job? = null

    init {
        val config = PlayerConfig(
            enableBackgroundAudio = true, // Audio continue en arrière-plan
            enablePictureInPicture = false,
            enableRetry = true,
            maxRetryCount = 3
        )
        player = PracticePlayerEnhanced(application.applicationContext, config)

        viewModelScope.launch {
            player?.state?.collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
            }
        }

        // Vérifier si on doit activer le mode nuit (après 20h)
        checkNightMode()
    }

    fun loadPractice(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            practiceRepository.getById(practiceId).fold(
                onSuccess = { practice ->
                    _uiState.update { it.copy(practice = practice) }
                    player?.prepare(practice.mediaUrl)
                    _uiState.update { it.copy(isLoading = false) }

                    analytics.logEvent("meditation_player_opened") {
                        param("practice_id", practice.id)
                        param("discipline", practice.discipline.name)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Erreur de chargement")
                    }
                    Timber.e(error, "Error loading practice for meditation player")
                }
            )
        }
    }

    fun onEvent(event: MeditationPlayerEvent) {
        when (event) {
            is MeditationPlayerEvent.TogglePlayPause -> togglePlayPause()
            is MeditationPlayerEvent.SeekTo -> player?.seekTo(event.position)
            is MeditationPlayerEvent.Retry -> retry()
            is MeditationPlayerEvent.SetAmbientSound -> setAmbientSound(event.sound)
            is MeditationPlayerEvent.SetAmbientVolume -> setAmbientVolume(event.volume)
            is MeditationPlayerEvent.SetSleepTimer -> setSleepTimer(event.option)
            is MeditationPlayerEvent.CancelSleepTimer -> cancelSleepTimer()
            is MeditationPlayerEvent.ToggleNightMode -> toggleNightMode()
            is MeditationPlayerEvent.StartBreathingExercise -> startBreathingExercise()
            is MeditationPlayerEvent.StopBreathingExercise -> stopBreathingExercise()
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
                delay(1000)
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

    private fun retry() {
        _uiState.value.practice?.id?.let { loadPractice(it) }
    }

    private fun setAmbientSound(sound: AmbientSound?) {
        _uiState.update { it.copy(activeAmbientSound = sound) }

        analytics.logEvent("meditation_ambient_sound_changed") {
            param("sound", sound?.id ?: "none")
        }
    }

    private fun setAmbientVolume(volume: Float) {
        _uiState.update { it.copy(ambientVolume = volume.coerceIn(0f, 1f)) }
    }

    private fun setSleepTimer(option: SleepTimerOption) {
        sleepTimer?.cancel()

        if (option == SleepTimerOption.INFINITE) {
            _uiState.update {
                it.copy(
                    sleepTimerEnabled = false,
                    sleepTimerDuration = 0L,
                    sleepTimerRemaining = 0L
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                sleepTimerEnabled = true,
                sleepTimerDuration = option.durationMs,
                sleepTimerRemaining = option.durationMs
            )
        }

        sleepTimer = viewModelScope.launch {
            var remaining = option.durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _uiState.update { it.copy(sleepTimerRemaining = remaining) }
            }

            // Timer terminé - pause et fade out
            player?.pause()
            _uiState.update {
                it.copy(
                    sleepTimerEnabled = false,
                    sleepTimerRemaining = 0L
                )
            }

            analytics.logEvent("meditation_sleep_timer_completed") {
                param("duration_min", (option.durationMs / 60000).toLong())
            }
        }

        analytics.logEvent("meditation_sleep_timer_set") {
            param("duration_min", (option.durationMs / 60000).toLong())
        }
    }

    private fun cancelSleepTimer() {
        sleepTimer?.cancel()
        _uiState.update {
            it.copy(
                sleepTimerEnabled = false,
                sleepTimerDuration = 0L,
                sleepTimerRemaining = 0L
            )
        }
    }

    private fun toggleNightMode() {
        val newNightMode = !_uiState.value.isNightMode
        _uiState.update { it.copy(isNightMode = newNightMode) }

        analytics.logEvent("meditation_night_mode_toggled") {
            param("enabled", newNightMode.toString())
        }
    }

    private fun checkNightMode() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour >= 20 || hour < 6) {
            _uiState.update { it.copy(isNightMode = true) }
        }
    }

    private fun startBreathingExercise() {
        breathingTimer?.cancel()
        _uiState.update {
            it.copy(
                breathingPhase = BreathingPhase.INHALE,
                breathingCycleProgress = 0f
            )
        }

        breathingTimer = viewModelScope.launch {
            while (true) {
                val currentPhase = _uiState.value.breathingPhase
                val phaseDuration = currentPhase.durationMs

                // Animation du progrès
                val steps = 60 // 60 étapes pour une animation fluide
                val stepDuration = phaseDuration / steps

                for (i in 0 until steps) {
                    delay(stepDuration)
                    _uiState.update {
                        it.copy(breathingCycleProgress = (i + 1).toFloat() / steps)
                    }
                }

                // Passer à la phase suivante
                _uiState.update {
                    it.copy(
                        breathingPhase = currentPhase.next(),
                        breathingCycleProgress = 0f
                    )
                }
            }
        }
    }

    private fun stopBreathingExercise() {
        breathingTimer?.cancel()
        _uiState.update {
            it.copy(
                breathingPhase = BreathingPhase.IDLE,
                breathingCycleProgress = 0f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        breathingTimer?.cancel()
        sleepTimer?.cancel()
        player?.release()
        player = null
    }
}
