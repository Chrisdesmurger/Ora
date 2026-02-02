package com.ora.wellbeing.feature.practice.player.specialized.yoga

import androidx.annotation.StringRes
import com.ora.wellbeing.R
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.data.model.firestore.YogaPoseDocument
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
    val difficultyLevel: Int = 1,                // Niveau de difficulté en temps réel (1-3)
    val poseDescription: PoseDescription? = null, // Description de la pose actuelle

    // Données des poses depuis Firestore (optionnel)
    val yogaPoses: List<YogaPoseDocument> = emptyList() // Poses chargées depuis le backend
)

/**
 * Côté actuel pour les postures asymétriques
 */
enum class YogaSide(@StringRes val nameRes: Int) {
    NONE(R.string.yoga_side_none),
    LEFT(R.string.yoga_side_left),
    RIGHT(R.string.yoga_side_right)
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
 * Description d'une pose de yoga avec instructions
 * Utilisé pour afficher les cartes d'information pendant la pratique
 */
data class PoseDescription(
    val stimulus: String = "",           // Stimulus sensoriel / description courte
    val instructions: List<String> = emptyList(), // Instructions pas à pas
    val targetZones: List<String> = emptyList(),  // Zones du corps ciblées
    val benefits: List<String> = emptyList()      // Bienfaits de la pose
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
