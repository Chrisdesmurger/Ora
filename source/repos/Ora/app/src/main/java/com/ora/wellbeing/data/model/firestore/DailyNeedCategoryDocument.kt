package com.ora.wellbeing.data.model.firestore

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * DailyNeedCategoryDocument - Firestore data model for daily_needs_categories collection
 * Collection: "daily_needs_categories/{categoryId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Field names use snake_case to match OraWebApp backend schema
 *
 * This model represents the "Ton besoin du jour" categories displayed on HomeScreen.
 * Categories include: Anti-stress, Energie matinale, Relaxation, Pratique du soir
 *
 * @see com.ora.wellbeing.data.mapper.DailyNeedCategoryMapper for conversion to DailyNeedCategory
 */
@IgnoreExtraProperties
class DailyNeedCategoryDocument() {

    // ============================================================================
    // Basic Information
    // ============================================================================

    /**
     * Category ID (e.g., "anti-stress", "energie-matinale")
     */
    var id: String = ""

    /**
     * Category name in French (e.g., "Anti-stress")
     */
    var name_fr: String = ""

    /**
     * Category name in English (e.g., "Anti-stress")
     */
    var name_en: String = ""

    /**
     * Short description in French (e.g., "Calme ton esprit et reduis ton anxiete")
     */
    var description_fr: String = ""

    /**
     * Short description in English (e.g., "Calm your mind and reduce anxiety")
     */
    var description_en: String = ""

    // ============================================================================
    // Visual Styling
    // ============================================================================

    /**
     * Background color in HEX format (e.g., "#A78BFA" for Lavender)
     * Used for card background with alpha transparency
     */
    var color_hex: String = "#F18D5C"  // Default: Ora coral orange

    /**
     * Icon URL from Firebase Storage (optional)
     * Used for category visual identification
     */
    var icon_url: String? = null

    // ============================================================================
    // Display & Filtering
    // ============================================================================

    /**
     * Display order (0-indexed, lower values appear first)
     */
    var order: Int = 0

    /**
     * Whether this category is visible to users
     */
    var is_active: Boolean = true

    /**
     * Tags used to filter lessons for this category
     * Example: ["relaxation", "breathing", "meditation"]
     * Lessons with matching need_tags will appear in this category
     */
    var filter_tags: List<String> = emptyList()

    /**
     * Explicit lesson IDs to include in this category (optional)
     * Takes priority over filter_tags if not empty
     * Allows admins to manually curate specific lessons
     */
    var lesson_ids: List<String> = emptyList()

    // ============================================================================
    // Companion
    // ============================================================================

    companion object {
        /**
         * Default categories for "Ton besoin du jour" section
         */
        val CATEGORY_ANTI_STRESS = "anti-stress"
        val CATEGORY_MORNING_ENERGY = "energie-matinale"
        val CATEGORY_RELAXATION = "relaxation"
        val CATEGORY_EVENING_PRACTICE = "pratique-du-soir"

        /**
         * Default colors per category
         */
        val DEFAULT_COLORS = mapOf(
            CATEGORY_ANTI_STRESS to "#A78BFA",       // Lavender
            CATEGORY_MORNING_ENERGY to "#FCD34D",    // Yellow
            CATEGORY_RELAXATION to "#86EFAC",        // Green
            CATEGORY_EVENING_PRACTICE to "#7C3AED"   // Purple
        )
    }
}
