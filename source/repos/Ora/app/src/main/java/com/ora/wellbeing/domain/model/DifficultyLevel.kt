package com.ora.wellbeing.domain.model

import androidx.annotation.StringRes
import com.ora.wellbeing.R

/**
 * Localized difficulty level enum
 * Uses string resources for i18n support
 */
enum class DifficultyLevel(
    val id: String,
    @StringRes val nameRes: Int
) {
    BEGINNER("beginner", R.string.difficulty_beginner),
    INTERMEDIATE("intermediate", R.string.difficulty_intermediate),
    ADVANCED("advanced", R.string.difficulty_advanced),
    ALL_LEVELS("all_levels", R.string.difficulty_all_levels);

    companion object {
        fun fromId(id: String): DifficultyLevel {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: ALL_LEVELS
        }

        fun fromString(level: String): DifficultyLevel {
            return when (level.lowercase()) {
                "beginner", "débutant", "debutant", "principiante" -> BEGINNER
                "intermediate", "intermédiaire", "intermediaire", "intermedio" -> INTERMEDIATE
                "advanced", "avancé", "avance", "avanzado" -> ADVANCED
                "all_levels", "tous niveaux", "todos los niveles" -> ALL_LEVELS
                else -> ALL_LEVELS
            }
        }
    }
}
