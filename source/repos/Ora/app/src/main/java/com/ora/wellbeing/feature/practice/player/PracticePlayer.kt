package com.ora.wellbeing.feature.practice.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * État du player
 */
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val buffering: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false
)

/**
 * Wrapper autour de Media3 ExoPlayer pour la lecture de pratiques
 */
class PracticePlayer(
    context: Context
) {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var player: ExoPlayer? = null
    private var positionUpdateRunnable: Runnable? = null
    private var handler: android.os.Handler? = null

    init {
        player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _state.value = _state.value.copy(buffering = true)
                        }
                        Player.STATE_READY -> {
                            _state.value = _state.value.copy(
                                buffering = false,
                                duration = duration
                            )
                        }
                        Player.STATE_ENDED -> {
                            _state.value = _state.value.copy(
                                isPlaying = false,
                                completed = true
                            )
                            stopPositionUpdates()
                        }
                        Player.STATE_IDLE -> {
                            // Nothing to do
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.value = _state.value.copy(isPlaying = isPlaying)
                    if (isPlaying) {
                        startPositionUpdates()
                    } else {
                        stopPositionUpdates()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    Timber.e(error, "Erreur de lecture")
                    _state.value = _state.value.copy(
                        error = "Erreur de lecture: ${error.message}",
                        isPlaying = false
                    )
                }
            })
        }

        handler = android.os.Handler(android.os.Looper.getMainLooper())
    }

    /**
     * Prépare le média pour la lecture
     */
    fun prepare(url: String) {
        try {
            val mediaItem = MediaItem.fromUri(url)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            Timber.d("Média préparé: $url")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la préparation du média")
            _state.value = _state.value.copy(error = "Erreur: ${e.message}")
        }
    }

    /**
     * Lance la lecture
     */
    fun play() {
        player?.play()
    }

    /**
     * Met en pause
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Avance de X secondes
     */
    fun seekForward(seconds: Long = 15) {
        player?.let {
            val newPosition = (it.currentPosition + (seconds * 1000)).coerceAtMost(it.duration)
            it.seekTo(newPosition)
            _state.value = _state.value.copy(currentPosition = newPosition)
        }
    }

    /**
     * Recule de X secondes
     */
    fun seekBackward(seconds: Long = 15) {
        player?.let {
            val newPosition = (it.currentPosition - (seconds * 1000)).coerceAtLeast(0)
            it.seekTo(newPosition)
            _state.value = _state.value.copy(currentPosition = newPosition)
        }
    }

    /**
     * Se déplace à une position spécifique
     */
    fun seekTo(position: Long) {
        player?.seekTo(position)
        _state.value = _state.value.copy(currentPosition = position)
    }

    /**
     * Obtient le player ExoPlayer (pour PlayerView)
     */
    fun getExoPlayer(): ExoPlayer? = player

    /**
     * Libère les ressources
     */
    fun release() {
        stopPositionUpdates()
        player?.release()
        player = null
        handler = null
    }

    /**
     * Démarre les mises à jour de position
     */
    private fun startPositionUpdates() {
        positionUpdateRunnable = object : Runnable {
            override fun run() {
                player?.let {
                    _state.value = _state.value.copy(
                        currentPosition = it.currentPosition,
                        duration = it.duration
                    )
                }
                handler?.postDelayed(this, 500) // Update every 500ms
            }
        }
        handler?.post(positionUpdateRunnable!!)
    }

    /**
     * Arrête les mises à jour de position
     */
    private fun stopPositionUpdates() {
        positionUpdateRunnable?.let { handler?.removeCallbacks(it) }
    }
}
