package com.ora.wellbeing.feature.practice.player.specialized.massage

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
import javax.inject.Inject

@HiltViewModel
class MassagePlayerViewModel @Inject constructor(
    application: Application,
    private val practiceRepository: PracticeRepository,
    private val analytics: FirebaseAnalytics
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MassagePlayerState())
    val uiState: StateFlow<MassagePlayerState> = _uiState.asStateFlow()

    private var player: PracticePlayerEnhanced? = null
    private var sessionTimer: Job? = null
    private var zoneTimer: Job? = null

    init {
        val config = PlayerConfig(
            enableBackgroundAudio = true,
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
    }

    fun loadPractice(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            practiceRepository.getById(practiceId).fold(
                onSuccess = { practice ->
                    _uiState.update { it.copy(practice = practice) }
                    player?.prepare(practice.mediaUrl)

                    // Initialiser les zones avec la première zone active
                    val zones = BodyZone.defaultZones().mapIndexed { index, zone ->
                        if (index == 0) zone.copy(state = ZoneState.ACTIVE) else zone
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bodyZones = zones,
                            currentInstructionRes = zones.firstOrNull()?.instructionRes?.firstOrNull() ?: 0,
                            pressureLevel = zones.firstOrNull()?.pressureRecommended ?: PressureLevel.MEDIUM
                        )
                    }

                    analytics.logEvent("massage_player_opened") {
                        param("practice_id", practice.id)
                        param("discipline", practice.discipline.name)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Erreur de chargement")
                    }
                    Timber.e(error, "Error loading practice for massage player")
                }
            )
        }
    }

    fun onEvent(event: MassagePlayerEvent) {
        when (event) {
            is MassagePlayerEvent.TogglePlayPause -> togglePlayPause()
            is MassagePlayerEvent.SeekTo -> player?.seekTo(event.position)
            is MassagePlayerEvent.Retry -> retry()
            is MassagePlayerEvent.SelectZone -> selectZone(event.index)
            is MassagePlayerEvent.NextZone -> nextZone()
            is MassagePlayerEvent.PreviousZone -> previousZone()
            is MassagePlayerEvent.CompleteCurrentZone -> completeCurrentZone()
            is MassagePlayerEvent.RepeatCurrentZone -> repeatCurrentZone()
            is MassagePlayerEvent.SetPressureLevel -> setPressureLevel(event.level)
            is MassagePlayerEvent.ToggleBodyMap -> toggleBodyMap()
        }
    }

    private fun togglePlayPause() {
        if (_uiState.value.playerState.isPlaying) {
            player?.pause()
            stopSessionTimer()
            stopZoneTimer()
        } else {
            player?.play()
            startSession()
            startZoneTimer()
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

    private fun startZoneTimer() {
        zoneTimer?.cancel()
        val currentZone = _uiState.value.bodyZones.getOrNull(_uiState.value.currentZoneIndex) ?: return
        _uiState.update { it.copy(zoneTimer = currentZone.duration) }

        zoneTimer = viewModelScope.launch {
            var remaining = currentZone.duration
            while (remaining > 0 && _uiState.value.playerState.isPlaying) {
                delay(1000)
                remaining -= 1000
                _uiState.update { it.copy(zoneTimer = remaining) }

                // Mettre à jour l'instruction selon le temps écoulé
                updateInstruction(currentZone, currentZone.duration - remaining)
            }

            if (remaining <= 0) {
                // Zone terminée automatiquement
                onZoneCompleted()
            }
        }
    }

    private fun stopZoneTimer() {
        zoneTimer?.cancel()
    }

    private fun updateInstruction(zone: BodyZone, elapsed: Long) {
        val totalDuration = zone.duration
        val instructionIndex = ((elapsed.toFloat() / totalDuration) * zone.instructionRes.size).toInt()
            .coerceIn(0, zone.instructionRes.size - 1)

        val instructionRes = zone.instructionRes.getOrNull(instructionIndex) ?: 0
        _uiState.update { it.copy(currentInstructionRes = instructionRes) }
    }

    private fun onZoneCompleted() {
        val currentIndex = _uiState.value.currentZoneIndex
        val zones = _uiState.value.bodyZones.toMutableList()

        // Marquer la zone comme terminée
        if (currentIndex in zones.indices) {
            zones[currentIndex] = zones[currentIndex].copy(state = ZoneState.COMPLETED)
        }

        _uiState.update { it.copy(bodyZones = zones) }

        // Passer à la zone suivante si disponible
        if (currentIndex < zones.size - 1) {
            selectZone(currentIndex + 1)
        } else {
            // Toutes les zones sont terminées
            analytics.logEvent("massage_session_completed") {
                param("practice_id", _uiState.value.practice?.id ?: "unknown")
                param("zones_completed", zones.count { it.state == ZoneState.COMPLETED }.toLong())
            }
        }
    }

    private fun retry() {
        _uiState.value.practice?.id?.let { loadPractice(it) }
    }

    private fun selectZone(index: Int) {
        val zones = _uiState.value.bodyZones
        if (index !in zones.indices) return

        stopZoneTimer()

        val updatedZones = zones.mapIndexed { i, zone ->
            when {
                i == index -> zone.copy(state = ZoneState.ACTIVE)
                zone.state == ZoneState.ACTIVE -> zone.copy(state = ZoneState.PENDING)
                else -> zone
            }
        }

        val selectedZone = zones[index]
        _uiState.update {
            it.copy(
                bodyZones = updatedZones,
                currentZoneIndex = index,
                currentInstructionRes = selectedZone.instructionRes.firstOrNull() ?: 0,
                pressureLevel = selectedZone.pressureRecommended,
                zoneRepetitions = it.targetRepetitions
            )
        }

        if (_uiState.value.playerState.isPlaying) {
            startZoneTimer()
        }

        analytics.logEvent("massage_zone_selected") {
            param("zone", selectedZone.id)
        }
    }

    private fun nextZone() {
        val currentIndex = _uiState.value.currentZoneIndex
        val zones = _uiState.value.bodyZones
        if (currentIndex < zones.size - 1) {
            selectZone(currentIndex + 1)
        }
    }

    private fun previousZone() {
        val currentIndex = _uiState.value.currentZoneIndex
        if (currentIndex > 0) {
            selectZone(currentIndex - 1)
        }
    }

    private fun completeCurrentZone() {
        onZoneCompleted()
    }

    private fun repeatCurrentZone() {
        val currentIndex = _uiState.value.currentZoneIndex
        val remaining = _uiState.value.zoneRepetitions

        if (remaining > 0) {
            _uiState.update { it.copy(zoneRepetitions = remaining - 1) }
            startZoneTimer()
        } else {
            completeCurrentZone()
        }
    }

    private fun setPressureLevel(level: PressureLevel) {
        _uiState.update { it.copy(pressureLevel = level) }

        analytics.logEvent("massage_pressure_changed") {
            param("level", level.name)
        }
    }

    private fun toggleBodyMap() {
        _uiState.update { it.copy(showBodyMap = !it.showBodyMap) }
    }

    override fun onCleared() {
        super.onCleared()
        stopSessionTimer()
        stopZoneTimer()
        player?.release()
        player = null
    }
}
