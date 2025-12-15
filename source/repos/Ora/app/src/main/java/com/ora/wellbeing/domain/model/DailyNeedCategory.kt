package com.ora.wellbeing.domain.model

import androidx.compose.ui.graphics.Color

/**
 * DailyNeedCategory - Domain model for daily need categories
 *
 * This is the Android representation of a "Ton besoin du jour" category.
 * Converted from DailyNeedCategoryDocument via DailyNeedCategoryMapper.
 *
 * Categories help users quickly find content matching their current needs:
 * - Anti-stress: Calming practices for anxiety relief
 * - Energie matinale: Energizing morning routines
 * - Relaxation: Body relaxation and tension release
 * - Pratique du soir: Evening practices for better sleep
 */
data class DailyNeedCategory(
    /**
     * Unique identifier (e.g., "anti-stress")
     */
    val id: String,

    /**
     * Display name in French (e.g., "Anti-stress")
     */
    val nameFr: String,

    /**
     * Display name in English (e.g., "Anti-stress")
     */
    val nameEn: String,

    /**
     * Short description in French
     */
    val descriptionFr: String,

    /**
     * Short description in English
     */
    val descriptionEn: String,

    /**
     * Background color for the category card
     * Parsed from color_hex in Firestore document
     */
    val color: Color,

    /**
     * Icon URL from Firebase Storage (optional)
     */
    val iconUrl: String?,

    /**
     * Display order (lower = first)
     */
    val order: Int,

    /**
     * Whether this category is visible
     */
    val isActive: Boolean,

    /**
     * Tags used to filter matching lessons
     */
    val filterTags: List<String>,

    /**
     * Explicit lesson IDs (takes priority over filterTags)
     */
    val lessonIds: List<String>
) {
    /**
     * Returns the localized name based on locale
     */
    fun getLocalizedName(locale: String = "fr"): String {
        return if (locale == "en") nameEn else nameFr
    }

    /**
     * Returns the localized description based on locale
     */
    fun getLocalizedDescription(locale: String = "fr"): String {
        return if (locale == "en") descriptionEn else descriptionFr
    }

    /**
     * Checks if a lesson matches this category
     * @param lessonId The lesson ID to check
     * @param lessonNeedTags The lesson's need_tags
     * @return True if the lesson belongs to this category
     */
    fun matchesLesson(lessonId: String, lessonNeedTags: List<String>): Boolean {
        // Priority 1: Check explicit lesson_ids
        if (lessonIds.isNotEmpty()) {
            return lessonId in lessonIds
        }

        // Priority 2: Check filter_tags match
        return filterTags.any { filterTag ->
            lessonNeedTags.any { lessonTag ->
                lessonTag.equals(filterTag, ignoreCase = true)
            }
        }
    }

    companion object {
        /**
         * Default fallback color (Ora coral orange)
         */
        val DEFAULT_COLOR = Color(0xFFF18D5C)

        /**
         * Pre-defined category colors
         */
        val COLOR_ANTI_STRESS = Color(0xFFA78BFA)      // Lavender
        val COLOR_MORNING_ENERGY = Color(0xFFFCD34D)   // Yellow
        val COLOR_RELAXATION = Color(0xFF86EFAC)       // Green
        val COLOR_EVENING_PRACTICE = Color(0xFF7C3AED) // Purple
    }
}
