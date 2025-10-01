package com.ora.wellbeing.data.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _exoPlayer: ExoPlayer? = null
    val exoPlayer: ExoPlayer
        get() = _exoPlayer ?: createExoPlayer()

    private fun createExoPlayer(): ExoPlayer {
        return ExoPlayer.Builder(context)
            .build()
            .also { _exoPlayer = it }
    }

    fun playVideo(videoUrl: String) {
        val mediaItem = MediaItem.fromUri(videoUrl)

        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        exoPlayer.apply {
            setMediaSource(mediaSource)
            prepare()
            play()
        }
    }

    fun pauseVideo() {
        exoPlayer.pause()
    }

    fun resumeVideo() {
        exoPlayer.play()
    }

    fun stopVideo() {
        exoPlayer.stop()
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    fun release() {
        _exoPlayer?.release()
        _exoPlayer = null
    }

    fun addListener(listener: Player.Listener) {
        exoPlayer.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        exoPlayer.removeListener(listener)
    }
}