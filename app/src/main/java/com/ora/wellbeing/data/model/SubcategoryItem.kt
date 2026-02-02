package com.ora.wellbeing.data.model

import androidx.compose.ui.graphics.Color

/**
 * SubcategoryItem - Android UI model for subcategories
 *
 * Represents a subcategory card displayed in horizontal scroll
 * within ContentCategoryDetailScreen after user selects a main category.
 *
 * Managed via OraWebApp admin portal, stored in Firestore.
 *
 * @property id Unique identifier
 * @property parentCategory Parent category ID (Meditation, Yoga, etc.)
 * @property name Display name (localized)
 * @property description Short description (optional)
 * @property iconUrl Icon/thumbnail image URL
 * @property imageUrl Background image URL (optional)
 * @property color Background color (used when no image)
 * @property filterTags Tags used to filter content
 * @property order Display order
 * @property itemCount Number of items in this subcategory
 */
data class SubcategoryItem(
    val id: String,
    val parentCategory: String,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val imageUrl: String? = null,
    val color: Color = Color(0xFFE8D4C4), // Default warm beige
    val filterTags: List<String> = emptyList(),
    val order: Int = 0,
    val itemCount: Int = 0
) {
    companion object {
        /**
         * Parse hex color string to Compose Color
         */
        fun parseColor(hex: String?): Color {
            if (hex.isNullOrBlank()) return Color(0xFFE8D4C4)
            return try {
                val colorString = hex.removePrefix("#")
                Color(android.graphics.Color.parseColor("#$colorString"))
            } catch (e: Exception) {
                Color(0xFFE8D4C4) // Default warm beige
            }
        }
    }
}
