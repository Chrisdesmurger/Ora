package com.ora.wellbeing.domain.model

/**
 * FIX(build-debug-android): Modèle pour les vidéos de contenu
 */
data class Video(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Int, // en secondes
    val category: ContentCategory,
    val level: DifficultyLevel = DifficultyLevel.BEGINNER,
    val instructor: String? = null,
    val tags: List<String> = emptyList()
)

enum class DifficultyLevel(val displayName: String) {
    BEGINNER("Débutant"),
    INTERMEDIATE("Intermédiaire"),
    ADVANCED("Avancé")
}