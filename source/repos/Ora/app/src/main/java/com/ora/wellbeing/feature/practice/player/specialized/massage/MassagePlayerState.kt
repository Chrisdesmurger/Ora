package com.ora.wellbeing.feature.practice.player.specialized.massage

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
    val currentInstruction: String = "",          // Instruction textuelle actuelle
    val showBodyMap: Boolean = true               // Afficher la carte corporelle
)

/**
 * Zone du corps à masser
 */
data class BodyZone(
    val id: String,
    val name: String,
    val icon: String,
    val duration: Long,                          // Durée recommandée en ms
    val instructions: List<String>,              // Instructions étape par étape
    val pressureRecommended: PressureLevel,
    val state: ZoneState = ZoneState.PENDING
) {
    companion object {
        fun defaultZones(): List<BodyZone> = listOf(
            BodyZone(
                id = "neck",
                name = "Nuque",
                icon = "🔵",
                duration = 60000L,
                instructions = listOf(
                    "Placez vos doigts à la base du crâne",
                    "Effectuez des mouvements circulaires",
                    "Descendez progressivement vers les épaules"
                ),
                pressureRecommended = PressureLevel.MEDIUM
            ),
            BodyZone(
                id = "shoulders",
                name = "Épaules",
                icon = "🟢",
                duration = 90000L,
                instructions = listOf(
                    "Pétrissez le trapèze avec le pouce",
                    "Alternez pression et relâchement",
                    "Travaillez des deux côtés"
                ),
                pressureRecommended = PressureLevel.HIGH
            ),
            BodyZone(
                id = "back",
                name = "Dos",
                icon = "🟡",
                duration = 120000L,
                instructions = listOf(
                    "Utilisez une balle de massage",
                    "Roulez le long de la colonne",
                    "Insistez sur les points de tension"
                ),
                pressureRecommended = PressureLevel.MEDIUM
            ),
            BodyZone(
                id = "arms",
                name = "Bras",
                icon = "🟠",
                duration = 60000L,
                instructions = listOf(
                    "Massez de l'épaule vers le poignet",
                    "Insistez sur l'avant-bras",
                    "Étirez les doigts"
                ),
                pressureRecommended = PressureLevel.LOW
            ),
            BodyZone(
                id = "hands",
                name = "Mains",
                icon = "🔴",
                duration = 45000L,
                instructions = listOf(
                    "Massez la paume avec le pouce",
                    "Étirez chaque doigt",
                    "Terminez par le poignet"
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
enum class PressureLevel(val displayName: String, val intensity: Float) {
    LOW("Légère", 0.33f),
    MEDIUM("Moyenne", 0.66f),
    HIGH("Forte", 1f)
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
