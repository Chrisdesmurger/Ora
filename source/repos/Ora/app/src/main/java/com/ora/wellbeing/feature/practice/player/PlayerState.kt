package com.ora.wellbeing.feature.practice.player

/**
 * État du player avec fonctionnalités avancées
 */
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val buffering: Boolean = false,
    val error: String? = null,
    val completed: Boolean = false,
    val playbackSpeed: Float = 1f,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isPipMode: Boolean = false,
    val hasAudioFocus: Boolean = false,
    val isNetworkAvailable: Boolean = true
)

/**
 * Modes de répétition
 */
enum class RepeatMode {
    OFF,     // Pas de répétition
    ONE,     // Répéter une seule fois
    ALL      // Répéter indéfiniment
}

/**
 * Vitesses de lecture disponibles
 */
enum class PlaybackSpeed(val value: Float, val label: String) {
    SPEED_0_5X(0.5f, "0.5x"),
    SPEED_0_75X(0.75f, "0.75x"),
    SPEED_1X(1f, "1x"),
    SPEED_1_25X(1.25f, "1.25x"),
    SPEED_1_5X(1.5f, "1.5x"),
    SPEED_2X(2f, "2x");

    companion object {
        fun fromValue(value: Float): PlaybackSpeed {
            return values().find { it.value == value } ?: SPEED_1X
        }
    }
}

/**
 * États d'erreur possibles
 */
sealed class PlayerError(val message: String) {
    object NetworkError : PlayerError("Erreur de connexion réseau")
    object SourceNotFound : PlayerError("Source média introuvable")
    object UnknownError : PlayerError("Erreur inconnue")
    data class CustomError(val errorMessage: String) : PlayerError(errorMessage)
}
