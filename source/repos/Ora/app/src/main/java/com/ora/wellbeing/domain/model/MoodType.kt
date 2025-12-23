package com.ora.wellbeing.domain.model

import androidx.annotation.StringRes
import com.ora.wellbeing.R

/**
 * Localized mood type enum
 * Uses string resources for i18n support
 */
enum class MoodType(
    val id: String,
    @StringRes val nameRes: Int,
    val emoji: String
) {
    HAPPY("happy", R.string.mood_happy, "😊"),
    CALM("calm", R.string.mood_calm, "😌"),
    TIRED("tired", R.string.mood_tired, "😴"),
    STRESSED("stressed", R.string.mood_stressed, "😰"),
    ENERGIZED("energized", R.string.mood_energized, "⚡"),
    PEACEFUL("peaceful", R.string.mood_peaceful, "🧘");

    companion object {
        fun fromId(id: String): MoodType {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: CALM
        }

        fun fromString(mood: String): MoodType {
            return when (mood.lowercase()) {
                "happy", "heureux", "feliz" -> HAPPY
                "calm", "calme", "tranquilo" -> CALM
                "tired", "fatigué", "fatigue", "cansado" -> TIRED
                "stressed", "stressé", "stresse", "estresado" -> STRESSED
                "energized", "énergisé", "energise", "energizado" -> ENERGIZED
                "peaceful", "paisible", "tranquilo", "paz" -> PEACEFUL
                else -> CALM
            }
        }
    }
}
