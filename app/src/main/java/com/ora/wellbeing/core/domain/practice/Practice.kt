package com.ora.wellbeing.core.domain.practice

/**
 * Type de média de pratique
 */
enum class MediaType {
    VIDEO, // Yoga, Pilates
    AUDIO  // Respiration, Méditation, Bien-être
}

/**
 * Discipline de pratique
 */
enum class Discipline(val displayName: String) {
    YOGA("Yoga"),
    PILATES("Pilates"),
    RESPIRATION("Respiration"),
    MEDITATION("Méditation"),
    WELLNESS("Bien-être");

    fun getMediaType(): MediaType {
        return when (this) {
            YOGA, PILATES -> MediaType.VIDEO
            RESPIRATION, MEDITATION, WELLNESS -> MediaType.AUDIO
        }
    }
}

/**
 * Niveau de difficulté
 */
enum class Level(val displayName: String) {
    BEGINNER("Débutant"),
    INTERMEDIATE("Intermédiaire"),
    ADVANCED("Avancé")
}

/**
 * Modèle de pratique (séance)
 */
data class Practice(
    val id: String,
    val title: String,
    val discipline: Discipline,
    val level: Level,
    val durationMin: Int,
    val description: String,
    val mediaType: MediaType,
    val mediaUrl: String,
    val thumbnailUrl: String,
    val tags: List<String> = emptyList(),
    val similarIds: List<String> = emptyList(),
    val downloadable: Boolean = true,
    val instructor: String? = null,
    val benefits: List<String> = emptyList()
)

/**
 * État de téléchargement d'une pratique
 */
enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    FAILED
}

/**
 * Informations de téléchargement
 */
data class DownloadInfo(
    val practiceId: String,
    val state: DownloadState,
    val progress: Float = 0f,
    val localPath: String? = null,
    val errorMessage: String? = null
)
