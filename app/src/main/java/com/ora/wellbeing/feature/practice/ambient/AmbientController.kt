package com.ora.wellbeing.feature.practice.ambient

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.min

/**
 * État du contrôleur d'ambiance
 */
data class AmbientState(
    val isPlaying: Boolean = false,
    val currentTrackId: String? = null,
    val volume: Float = 0.3f,
    val crossfadeEnabled: Boolean = true,
    val crossfadeDurationMs: Long = 500L
)

/**
 * Contrôleur pour les pistes d'ambiance
 * Utilise un deuxième ExoPlayer en loop pour mixer avec le média principal
 */
class AmbientController(
    context: Context,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(AmbientState())
    val state: StateFlow<AmbientState> = _state.asStateFlow()

    private var ambientPlayer: ExoPlayer? = null
    private var fadeJob: Job? = null

    init {
        ambientPlayer = ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE // Loop
            volume = 0.3f

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            // Ne devrait jamais arriver car on est en loop
                            Timber.w("Ambient player ended unexpectedly")
                        }
                        Player.STATE_READY -> {
                            Timber.d("Ambient track ready")
                        }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Timber.e(error, "Erreur ambient player")
                    // Ne pas bloquer si la piste d'ambiance échoue
                    stop()
                }
            })
        }
    }

    /**
     * Démarre une piste d'ambiance
     */
    fun start(trackUrl: String, trackId: String, volume: Float = 0.3f) {
        try {
            val mediaItem = MediaItem.fromUri(trackUrl)
            ambientPlayer?.setMediaItem(mediaItem)
            ambientPlayer?.prepare()

            if (_state.value.crossfadeEnabled) {
                // Crossfade in
                fadeIn(volume)
            } else {
                ambientPlayer?.volume = volume
                ambientPlayer?.play()
            }

            _state.value = _state.value.copy(
                isPlaying = true,
                currentTrackId = trackId,
                volume = volume
            )

            Timber.d("Ambient track started: $trackId, volume=$volume")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors du démarrage de la piste d'ambiance")
        }
    }

    /**
     * Arrête la piste d'ambiance
     */
    fun stop() {
        if (_state.value.crossfadeEnabled) {
            fadeOut()
        } else {
            ambientPlayer?.stop()
            _state.value = _state.value.copy(
                isPlaying = false,
                currentTrackId = null
            )
        }
    }

    /**
     * Change le volume
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        ambientPlayer?.volume = clampedVolume
        _state.value = _state.value.copy(volume = clampedVolume)
        Timber.d("Ambient volume set to: $clampedVolume")
    }

    /**
     * Active/désactive le crossfade
     */
    fun setCrossfadeEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(crossfadeEnabled = enabled)
    }

    /**
     * Fade in (crossfade d'entrée)
     */
    private fun fadeIn(targetVolume: Float) {
        fadeJob?.cancel()
        fadeJob = scope.launch(Dispatchers.Main) {
            val steps = 20
            val stepDuration = _state.value.crossfadeDurationMs / steps
            val volumeStep = targetVolume / steps

            ambientPlayer?.volume = 0f
            ambientPlayer?.play()

            for (i in 1..steps) {
                delay(stepDuration)
                val newVolume = min(volumeStep * i, targetVolume)
                ambientPlayer?.volume = newVolume
            }

            ambientPlayer?.volume = targetVolume
            Timber.d("Fade in completed")
        }
    }

    /**
     * Fade out (crossfade de sortie)
     */
    private fun fadeOut() {
        fadeJob?.cancel()
        fadeJob = scope.launch(Dispatchers.Main) {
            val currentVolume = ambientPlayer?.volume ?: 0f
            val steps = 20
            val stepDuration = _state.value.crossfadeDurationMs / steps
            val volumeStep = currentVolume / steps

            for (i in 1..steps) {
                delay(stepDuration)
                val newVolume = (currentVolume - volumeStep * i).coerceAtLeast(0f)
                ambientPlayer?.volume = newVolume
            }

            ambientPlayer?.stop()
            _state.value = _state.value.copy(
                isPlaying = false,
                currentTrackId = null
            )
            Timber.d("Fade out completed")
        }
    }

    /**
     * Libère les ressources
     */
    fun release() {
        fadeJob?.cancel()
        ambientPlayer?.release()
        ambientPlayer = null
    }
}
