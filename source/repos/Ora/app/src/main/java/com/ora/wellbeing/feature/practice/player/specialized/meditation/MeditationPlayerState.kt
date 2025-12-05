package com.ora.wellbeing.feature.practice.player.specialized.meditation

import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.feature.practice.player.PlayerState

/**
 * État spécifique au lecteur Méditation/Respiration
 */
data class MeditationPlayerState(
    // État de base
    val isLoading: Boolean = false,
    val practice: Practice? = null,
    val error: String? = null,
    val playerState: PlayerState = PlayerState(),
    val sessionStartTime: Long = 0L,
    val sessionDuration: Long = 0L,

    // Fonctionnalités spécifiques Méditation
    val breathingPhase: BreathingPhase = BreathingPhase.IDLE,
    val breathingCycleProgress: Float = 0f,     // 0f à 1f pour l'animation
    val currentPhaseIndex: Int = 0,              // Phase actuelle dans la méditation
    val totalPhases: Int = 6,                    // Nombre total de phases
    val phaseName: String = "",                  // Nom de la phase actuelle

    // Sons ambiants
    val ambientSounds: List<AmbientSound> = AmbientSound.defaultSounds(),
    val activeAmbientSound: AmbientSound? = null,
    val ambientVolume: Float = 0.5f,

    // Timer de fin
    val sleepTimerEnabled: Boolean = false,
    val sleepTimerDuration: Long = 0L,          // Durée en ms
    val sleepTimerRemaining: Long = 0L,         // Temps restant en ms

    // Mode nuit
    val isNightMode: Boolean = false
)

/**
 * Phase de respiration
 */
enum class BreathingPhase(val displayName: String, val durationMs: Long) {
    IDLE("Prêt", 0L),
    INHALE("Inspirez", 4000L),
    HOLD_IN("Retenez", 4000L),
    EXHALE("Expirez", 6000L),
    HOLD_OUT("Pause", 2000L);

    fun next(): BreathingPhase {
        return when (this) {
            IDLE -> INHALE
            INHALE -> HOLD_IN
            HOLD_IN -> EXHALE
            EXHALE -> HOLD_OUT
            HOLD_OUT -> INHALE
        }
    }
}

/**
 * Son ambiant disponible
 */
data class AmbientSound(
    val id: String,
    val name: String,
    val icon: String, // Emoji pour simplicité
    val resourceName: String? = null // Nom du fichier audio (pour implémentation future)
) {
    companion object {
        fun defaultSounds(): List<AmbientSound> = listOf(
            AmbientSound("rain", "Pluie", "🌧️"),
            AmbientSound("forest", "Forêt", "🌲"),
            AmbientSound("ocean", "Océan", "🌊"),
            AmbientSound("fire", "Feu", "🔥"),
            AmbientSound("silence", "Silence", "🔇")
        )
    }
}

/**
 * Durées de timer disponibles
 */
enum class SleepTimerOption(val displayName: String, val durationMs: Long) {
    FIVE_MIN("5 min", 5 * 60 * 1000L),
    TEN_MIN("10 min", 10 * 60 * 1000L),
    FIFTEEN_MIN("15 min", 15 * 60 * 1000L),
    THIRTY_MIN("30 min", 30 * 60 * 1000L),
    INFINITE("∞", Long.MAX_VALUE)
}

/**
 * Événements UI spécifiques au lecteur Méditation
 */
sealed class MeditationPlayerEvent {
    // Contrôles de base
    object TogglePlayPause : MeditationPlayerEvent()
    data class SeekTo(val position: Long) : MeditationPlayerEvent()
    object Retry : MeditationPlayerEvent()

    // Contrôles spécifiques Méditation
    data class SetAmbientSound(val sound: AmbientSound?) : MeditationPlayerEvent()
    data class SetAmbientVolume(val volume: Float) : MeditationPlayerEvent()
    data class SetSleepTimer(val option: SleepTimerOption) : MeditationPlayerEvent()
    object CancelSleepTimer : MeditationPlayerEvent()
    object ToggleNightMode : MeditationPlayerEvent()
    object StartBreathingExercise : MeditationPlayerEvent()
    object StopBreathingExercise : MeditationPlayerEvent()
}
