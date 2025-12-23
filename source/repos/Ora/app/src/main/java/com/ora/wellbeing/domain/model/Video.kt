package com.ora.wellbeing.domain.model

/**
 * FIX(build-debug-android): Modèle pour les vidéos de contenu
 * Migrated to use i18n-enabled enums (ContentCategory, DifficultyLevel)
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

// ContentCategory and DifficultyLevel enums moved to separate files for i18n support