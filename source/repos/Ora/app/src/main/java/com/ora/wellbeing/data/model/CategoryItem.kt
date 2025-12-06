package com.ora.wellbeing.data.model

import androidx.compose.ui.graphics.Color

/**
 * CategoryItem - Model for content library categories
 *
 * Represents a main category in the content library (Méditation, Yoga, Pilates, Bien-être)
 * Used for displaying category cards on the library home screen
 */
data class CategoryItem(
    /**
     * Category ID (matches ContentItem.VALID_CATEGORIES)
     * Examples: "Méditation", "Yoga", "Pilates", "Bien-être"
     */
    val id: String,

    /**
     * Display name of the category (French)
     */
    val name: String,

    /**
     * Dominant color for this category (from OraTheme)
     */
    val color: Color,

    /**
     * Optional image URL for category card background
     * If null, will use solid color background
     */
    val imageUrl: String? = null,

    /**
     * Optional icon resource ID for the category
     */
    val iconResId: Int? = null,

    /**
     * Number of items in this category (optional, for display purposes)
     */
    val itemCount: Int = 0
) {
    companion object {
        /**
         * MVP categories (Version 1)
         * Excludes "Respiration" and "Sommeil" for now
         */
        val MVP_CATEGORIES = listOf(
            "Méditation",
            "Yoga",
            "Pilates",
            "Bien-être"
        )

        /**
         * All available categories (for future expansion)
         */
        val ALL_CATEGORIES = listOf(
            "Méditation",
            "Yoga",
            "Pilates",
            "Bien-être",
            "Respiration",
            "Sommeil"
        )
    }
}
