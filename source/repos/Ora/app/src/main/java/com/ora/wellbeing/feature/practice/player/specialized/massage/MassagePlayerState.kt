package com.ora.wellbeing.feature.practice.player.specialized.massage

import androidx.annotation.StringRes
import com.ora.wellbeing.R
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.feature.practice.player.PlayerState

/**
 * État spécifique au lecteur Auto-massage/Bien-être
 */
data class MassagePlayerState(
    // État de base
    val isLoading: Boolean = false,
    val practice: Practice? = null,
    val error: String? = null,
    val playerState: PlayerState = PlayerState(),
    val sessionStartTime: Long = 0L,
    val sessionDuration: Long = 0L,

    // Fonctionnalités spécifiques Auto-massage
    val bodyZones: List<BodyZone> = BodyZone.defaultZones(),
    val currentZoneIndex: Int = 0,
    val zoneTimer: Long = 0L,                    // Temps restant sur la zone en ms
    val zoneRepetitions: Int = 0,                // Répétitions restantes
    val targetRepetitions: Int = 3,              // Nombre cible de répétitions
    val pressureLevel: PressureLevel = PressureLevel.MEDIUM,
    @StringRes val currentInstructionRes: Int = 0, // Instruction textuelle actuelle (@StringRes)
    val showBodyMap: Boolean = true               // Afficher la carte corporelle
)

/**
 * Zone du corps à masser
 */
data class BodyZone(
    val id: String,
    @StringRes val nameRes: Int,
    val icon: String,
    val duration: Long,                          // Durée recommandée en ms
    val instructionRes: List<Int>,               // Instructions étape par étape (@StringRes)
    val pressureRecommended: PressureLevel,
    val state: ZoneState = ZoneState.PENDING
) {
    companion object {
        fun defaultZones(): List<BodyZone> = listOf(
            BodyZone(
                id = "neck",
                nameRes = R.string.massage_zone_neck,
                icon = "🔵",
                duration = 60000L,
                instructionRes = listOf(
                    R.string.massage_instruction_neck_1,
                    R.string.massage_instruction_neck_2,
                    R.string.massage_instruction_neck_3
                ),
                pressureRecommended = PressureLevel.MEDIUM
            ),
            BodyZone(
                id = "shoulders",
                nameRes = R.string.massage_zone_shoulders,
                icon = "🟢",
                duration = 90000L,
                instructionRes = listOf(
                    R.string.massage_instruction_shoulders_1,
                    R.string.massage_instruction_shoulders_2,
                    R.string.massage_instruction_shoulders_3
                ),
                pressureRecommended = PressureLevel.HIGH
            ),
            BodyZone(
                id = "back",
                nameRes = R.string.massage_zone_back,
                icon = "🟡",
                duration = 120000L,
                instructionRes = listOf(
                    R.string.massage_instruction_back_1,
                    R.string.massage_instruction_back_2,
                    R.string.massage_instruction_back_3
                ),
                pressureRecommended = PressureLevel.MEDIUM
            ),
            BodyZone(
                id = "arms",
                nameRes = R.string.massage_zone_arms,
                icon = "🟠",
                duration = 60000L,
                instructionRes = listOf(
                    R.string.massage_instruction_arms_1,
                    R.string.massage_instruction_arms_2,
                    R.string.massage_instruction_arms_3
                ),
                pressureRecommended = PressureLevel.LOW
            ),
            BodyZone(
                id = "hands",
                nameRes = R.string.massage_zone_hands,
                icon = "🔴",
                duration = 45000L,
                instructionRes = listOf(
                    R.string.massage_instruction_hands_1,
                    R.string.massage_instruction_hands_2,
                    R.string.massage_instruction_hands_3
                ),
                pressureRecommended = PressureLevel.LOW
            )
        )
    }
}

/**
 * État d'une zone de massage
 */
enum class ZoneState {
    PENDING,    // Pas encore massée
    ACTIVE,     // En cours de massage
    COMPLETED   // Terminée
}

/**
 * Niveau de pression recommandé
 */
enum class PressureLevel(@StringRes val nameRes: Int, val intensity: Float) {
    LOW(R.string.massage_pressure_low, 0.33f),
    MEDIUM(R.string.massage_pressure_medium, 0.66f),
    HIGH(R.string.massage_pressure_high, 1f)
}

/**
 * Événements UI spécifiques au lecteur Auto-massage
 */
sealed class MassagePlayerEvent {
    // Contrôles de base
    object TogglePlayPause : MassagePlayerEvent()
    data class SeekTo(val position: Long) : MassagePlayerEvent()
    object Retry : MassagePlayerEvent()

    // Contrôles spécifiques Auto-massage
    data class SelectZone(val index: Int) : MassagePlayerEvent()
    object NextZone : MassagePlayerEvent()
    object PreviousZone : MassagePlayerEvent()
    object CompleteCurrentZone : MassagePlayerEvent()
    object RepeatCurrentZone : MassagePlayerEvent()
    data class SetPressureLevel(val level: PressureLevel) : MassagePlayerEvent()
    object ToggleBodyMap : MassagePlayerEvent()
}
