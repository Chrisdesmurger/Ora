package com.ora.wellbeing.domain.model

import androidx.annotation.StringRes
import com.ora.wellbeing.R

/**
 * Localized content category enum
 * Uses string resources for i18n support
 */
enum class ContentCategory(
    val id: String,
    @StringRes val nameRes: Int
) {
    MEDITATION("meditation", R.string.category_meditation),
    YOGA("yoga", R.string.category_yoga),
    BREATHING("breathing", R.string.category_breathing),
    PILATES("pilates", R.string.category_pilates),
    SLEEP("sleep", R.string.category_sleep),
    MASSAGE("massage", R.string.category_massage),
    WELLNESS("wellness", R.string.category_wellness);

    companion object {
        fun fromId(id: String): ContentCategory {
            return values().find { it.id.equals(id, ignoreCase = true) } ?: WELLNESS
        }

        fun fromString(category: String): ContentCategory {
            return when (category.lowercase()) {
                "meditation", "méditation", "meditación" -> MEDITATION
                "yoga" -> YOGA
                "breathing", "respiration", "respiración" -> BREATHING
                "pilates" -> PILATES
                "sleep", "sommeil", "sueño" -> SLEEP
                "massage", "masaje" -> MASSAGE
                "wellness", "bien-être", "bien-etre", "bienestar" -> WELLNESS
                else -> WELLNESS
            }
        }
    }
}
