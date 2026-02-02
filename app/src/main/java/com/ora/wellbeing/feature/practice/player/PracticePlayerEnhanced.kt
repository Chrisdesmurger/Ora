package com.ora.wellbeing.feature.practice.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File

/**
 * Version améliorée du PracticePlayer avec toutes les fonctionnalités avancées
 */
class PracticePlayerEnhanced(
    private val context: Context,
    private val config: PlayerConfig = PlayerConfig()
) {

    companion object {
        @Volatile
        private var sharedCache: SimpleCache? = null
        private val cacheLock = Any()

        /**
         * Obtient ou crée une instance singleton du cache.
         * Évite l'erreur "Another SimpleCache instance uses the folder"
         */
        private fun getSharedCache(context: Context, cacheSize: Long): SimpleCache {
            return sharedCache ?: synchronized(cacheLock) {
                sharedCache ?: run {
                    val cacheDir = File(context.cacheDir, "media")
                    val databaseProvider = StandaloneDatabaseProvider(context)
                    SimpleCache(
                        cacheDir,
                        LeastRecentlyUsedCacheEvictor(cacheSize),
                        databaseProvider
                    ).also { sharedCache = it }
                }
            }
        }
    }

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var player: ExoPlayer? = null
    private var positionUpdateRunnable: Runnable? = null
    private var handler: android.os.Handler? = null

    private val audioManager: AudioManager? = context.getSystemService()
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false

    private val connectivityManager: ConnectivityManager? = context.getSystemService()
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var retryCount = 0
    private val maxRetries = config.maxRetryCount

    init {
        setupPlayer()
        setupAudioFocus()
        setupNetworkMonitoring()
    }

    /**
     * Configure le player avec cache et options avancées
     */
    private fun setupPlayer() {
        try {
            // Utiliser le cache singleton pour éviter les conflits
            val cache = getSharedCache(context, config.cacheSize)

            // Build player
            player = ExoPlayer.Builder(context)
                .setLoadControl(DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000,  // Min buffer
                        50000,  // Max buffer
                        2500,   // Buffer for playback
                        5000    // Buffer for playback after rebuffer
                    )
                    .build()
                )
                .build()
                .apply {
                    // Set initial playback parameters
                    setPlaybackSpeed(config.defaultPlaybackSpeed.value)
                    repeatMode = when (config.defaultRepeatMode) {
                        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    }

                    // Add listener
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            handlePlaybackStateChange(playbackState)
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            handleIsPlayingChanged(isPlaying)
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            handlePlayerError(error)
                        }
                    })
                }

            handler = android.os.Handler(android.os.Looper.getMainLooper())

            Timber.d("PracticePlayerEnhanced initialized")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing player")
            _state.value = _state.value.copy(
                error = "Erreur d'initialisation: ${e.message}"
            )
        }
    }

    /**
     * Configure la gestion du focus audio
     */
    private fun setupAudioFocus() {
        if (!config.enableBackgroundAudio) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { focusChange ->
                    handleAudioFocusChange(focusChange)
                }
                .build()
        }
    }

    /**
     * Configure la surveillance de la connectivité réseau
     */
    private fun setupNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _state.value = _state.value.copy(isNetworkAvailable = true)
                // Retry if we were in error state due to network
                if (retryCount < maxRetries && _state.value.error != null) {
                    retryPlayback()
                }
            }

            override fun onLost(network: Network) {
                _state.value = _state.value.copy(isNetworkAvailable = false)
            }
        }

        connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    /**
     * Gère les changements de focus audio
     */
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                _state.value = _state.value.copy(hasAudioFocus = true)
                if (_state.value.isPlaying) {
                    player?.volume = 1f
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                _state.value = _state.value.copy(hasAudioFocus = false)
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player?.volume = 0.3f
            }
        }
    }

    /**
     * Gère les changements d'état de lecture
     */
    private fun handlePlaybackStateChange(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                _state.value = _state.value.copy(buffering = true)
            }
            Player.STATE_READY -> {
                _state.value = _state.value.copy(
                    buffering = false,
                    duration = player?.duration ?: 0L,
                    error = null
                )
                retryCount = 0 // Reset retry count on success
            }
            Player.STATE_ENDED -> {
                _state.value = _state.value.copy(
                    isPlaying = false,
                    completed = true
                )
                stopPositionUpdates()
                abandonAudioFocus()
            }
            Player.STATE_IDLE -> {
                // Nothing to do
            }
        }
    }

    /**
     * Gère les changements de lecture
     */
    private fun handleIsPlayingChanged(isPlaying: Boolean) {
        _state.value = _state.value.copy(isPlaying = isPlaying)
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    /**
     * Gère les erreurs du player
     */
    private fun handlePlayerError(error: PlaybackException) {
        Timber.e(error, "Playback error occurred")

        val errorMessage = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                PlayerError.NetworkError.message
            }
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                PlayerError.SourceNotFound.message
            }
            else -> {
                PlayerError.UnknownError.message
            }
        }

        _state.value = _state.value.copy(
            error = errorMessage,
            isPlaying = false
        )

        // Retry logic
        if (config.enableRetry && retryCount < maxRetries) {
            retryPlayback()
        }
    }

    /**
     * Réessaye la lecture après une erreur
     */
    private fun retryPlayback() {
        retryCount++
        val delay = (config.retryDelayMillis * retryCount).coerceAtMost(10000L)

        Timber.d("Retrying playback, attempt $retryCount/$maxRetries after ${delay}ms")

        handler?.postDelayed({
            player?.prepare()
            player?.play()
        }, delay)
    }

    /**
     * Demande le focus audio
     */
    private fun requestAudioFocus(): Boolean {
        if (!config.enableBackgroundAudio) return true

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                val result = audioManager?.requestAudioFocus(request)
                (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED).also {
                    hasAudioFocus = it
                    _state.value = _state.value.copy(hasAudioFocus = it)
                }
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED).also {
                hasAudioFocus = it
                _state.value = _state.value.copy(hasAudioFocus = it)
            }
        }
    }

    /**
     * Abandonne le focus audio
     */
    private fun abandonAudioFocus() {
        if (!config.enableBackgroundAudio) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager?.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }

        hasAudioFocus = false
        _state.value = _state.value.copy(hasAudioFocus = false)
    }

    // ========== Public API ==========

    /**
     * Prépare le média pour la lecture
     */
    fun prepare(url: String, startPosition: Long = 0L) {
        try {
            val mediaItem = MediaItem.fromUri(url)
            player?.setMediaItem(mediaItem)
            player?.prepare()

            if (startPosition > 0) {
                player?.seekTo(startPosition)
            }

            _state.value = _state.value.copy(
                error = null,
                completed = false
            )

            Timber.d("Media prepared: $url (start: $startPosition)")
        } catch (e: Exception) {
            Timber.e(e, "Error preparing media")
            _state.value = _state.value.copy(
                error = "Erreur de préparation: ${e.message}"
            )
        }
    }

    /**
     * Lance la lecture
     */
    fun play() {
        if (requestAudioFocus()) {
            player?.play()
            retryCount = 0 // Reset retry count
        } else {
            Timber.w("Could not obtain audio focus")
        }
    }

    /**
     * Met en pause
     */
    fun pause() {
        player?.pause()
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            play()
        }
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
     * Change la vitesse de lecture
     */
    fun setPlaybackSpeed(speed: PlaybackSpeed) {
        player?.setPlaybackSpeed(speed.value)
        _state.value = _state.value.copy(playbackSpeed = speed.value)
        Timber.d("Playback speed changed to ${speed.label}")
    }

    /**
     * Change le mode de répétition
     */
    fun setRepeatMode(mode: RepeatMode) {
        player?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        _state.value = _state.value.copy(repeatMode = mode)
        Timber.d("Repeat mode changed to $mode")
    }

    /**
     * Obtient le player ExoPlayer (pour PlayerView)
     */
    fun getExoPlayer(): ExoPlayer? = player

    /**
     * Vérifie si le Picture-in-Picture est supporté
     */
    fun isPipSupported(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }

    /**
     * Entre en mode Picture-in-Picture
     */
    fun enterPipMode(): Boolean {
        if (!config.enablePictureInPicture || !isPipSupported()) {
            return false
        }

        _state.value = _state.value.copy(isPipMode = true)
        return true
    }

    /**
     * Sort du mode Picture-in-Picture
     */
    fun exitPipMode() {
        _state.value = _state.value.copy(isPipMode = false)
    }

    /**
     * Obtient la position actuelle pour sauvegarde
     */
    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    /**
     * Libère les ressources
     */
    fun release() {
        stopPositionUpdates()
        abandonAudioFocus()

        networkCallback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }

        player?.release()
        player = null
        handler = null

        Timber.d("PracticePlayerEnhanced released")
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
