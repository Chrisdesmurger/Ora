package com.ora.wellbeing.feature.practice.player

/**
 * Configuration du player
 */
data class PlayerConfig(
    val enableBackgroundAudio: Boolean = true,
    val enablePictureInPicture: Boolean = true,
    val defaultPlaybackSpeed: PlaybackSpeed = PlaybackSpeed.SPEED_1X,
    val defaultRepeatMode: RepeatMode = RepeatMode.OFF,
    val seekIncrement: Long = 15000L, // 15 seconds in milliseconds
    val enableRetry: Boolean = true,
    val maxRetryCount: Int = 3,
    val retryDelayMillis: Long = 2000L,
    val cacheSize: Long = 100 * 1024 * 1024L // 100 MB
)

/**
 * Options de récupération d'erreur
 */
data class RetryOptions(
    val enabled: Boolean = true,
    val maxAttempts: Int = 3,
    val delayMillis: Long = 2000L,
    val backoffMultiplier: Float = 2f
)
