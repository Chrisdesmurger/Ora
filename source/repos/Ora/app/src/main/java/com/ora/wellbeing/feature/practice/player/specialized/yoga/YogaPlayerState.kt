package com.ora.wellbeing.feature.practice.player.specialized.yoga

import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.feature.practice.player.PlayerState
import com.ora.wellbeing.feature.practice.ui.Chapter

/**
 * État spécifique au lecteur Yoga/Pilates
 */
data class YogaPlayerState(
    // État de base
    val isLoading: Boolean = false,
    val practice: Practice? = null,
    val error: String? = null,
    val playerState: PlayerState = PlayerState(),
    val isFullscreen: Boolean = false,
    val sessionStartTime: Long = 0L,
    val sessionDuration: Long = 0L,

    // Fonctionnalités spécifiques Yoga
    val isMirrorMode: Boolean = false,          // Mode miroir pour suivre plus facilement
    val currentSide: YogaSide = YogaSide.NONE,  // Côté actuel (Gauche/Droit)
    val chapters: List<Chapter> = emptyList(),   // Chapitres/postures
    val currentChapterIndex: Int = 0,            // Index du chapitre actuel
    val nextPosePreview: PosePreview? = null,    // Aperçu de la prochaine posture
    val difficultyLevel: Int = 1                 // Niveau de difficulté en temps réel (1-3)
)

/**
 * Côté actuel pour les postures asymétriques
 */
enum class YogaSide(val displayName: String) {
    NONE(""),
    LEFT("Côté Gauche"),
    RIGHT("Côté Droit")
}

/**
 * Aperçu de la prochaine posture
 */
data class PosePreview(
    val name: String,
    val thumbnailUrl: String? = null,
    val timeUntil: Long // Temps restant avant cette posture en ms
)

/**
 * Événements UI spécifiques au lecteur Yoga
 */
sealed class YogaPlayerEvent {
    // Contrôles de base
    object TogglePlayPause : YogaPlayerEvent()
    object SeekForward : YogaPlayerEvent()
    object SeekBackward : YogaPlayerEvent()
    data class SeekTo(val position: Long) : YogaPlayerEvent()
    object ToggleFullscreen : YogaPlayerEvent()
    object Retry : YogaPlayerEvent()

    // Contrôles spécifiques Yoga
    object ToggleMirrorMode : YogaPlayerEvent()
    object SwitchSide : YogaPlayerEvent()
    data class GoToChapter(val index: Int) : YogaPlayerEvent()
    object NextChapter : YogaPlayerEvent()
    object PreviousChapter : YogaPlayerEvent()
}
