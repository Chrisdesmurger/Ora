package com.ora.wellbeing.data.model.firestore

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * SubcategoryDocument - Firestore data model for subcategories
 *
 * Collection: "subcategories/{subcategoryId}"
 * OR embedded in "categories/{categoryId}" as subcategories array
 *
 * These subcategories are managed via OraWebApp admin portal and displayed
 * as horizontal scrolling cards in ContentCategoryDetailScreen.
 *
 * **i18n Support**:
 * - French (fr): name_fr, description_fr
 * - English (en): name_en, description_en
 * - Spanish (es): name_es, description_es
 *
 * @see com.ora.wellbeing.data.model.SubcategoryItem for Android model
 */
@IgnoreExtraProperties
class SubcategoryDocument() {

    // ============================================================================
    // Identity
    // ============================================================================

    /**
     * Unique identifier (Firestore document ID or explicit field)
     */
    var id: String = ""

    /**
     * Parent category ID (e.g., "Meditation", "Yoga", "Pilates", "Bien-etre")
     */
    @get:PropertyName("parent_category")
    @set:PropertyName("parent_category")
    var parentCategory: String = ""

    // ============================================================================
    // Display Information
    // ============================================================================

    /**
     * Subcategory name (default/fallback)
     * e.g., "Méditation guidée", "Yoga doux", "Pilates débutant"
     */
    var name: String = ""

    /**
     * French name
     */
    @get:PropertyName("name_fr")
    @set:PropertyName("name_fr")
    var nameFr: String? = null

    /**
     * English name
     */
    @get:PropertyName("name_en")
    @set:PropertyName("name_en")
    var nameEn: String? = null

    /**
     * Spanish name
     */
    @get:PropertyName("name_es")
    @set:PropertyName("name_es")
    var nameEs: String? = null

    /**
     * Short description (default/fallback)
     */
    var description: String? = null

    /**
     * French description
     */
    @get:PropertyName("description_fr")
    @set:PropertyName("description_fr")
    var descriptionFr: String? = null

    /**
     * English description
     */
    @get:PropertyName("description_en")
    @set:PropertyName("description_en")
    var descriptionEn: String? = null

    /**
     * Spanish description
     */
    @get:PropertyName("description_es")
    @set:PropertyName("description_es")
    var descriptionEs: String? = null

    // ============================================================================
    // Visual Styling
    // ============================================================================

    /**
     * Icon/image URL for the subcategory card
     * Can be Firebase Storage URL or external URL
     */
    @get:PropertyName("icon_url")
    @set:PropertyName("icon_url")
    var iconUrl: String? = null

    /**
     * Background image URL for the card (optional)
     */
    @get:PropertyName("image_url")
    @set:PropertyName("image_url")
    var imageUrl: String? = null

    /**
     * Background color hex code (e.g., "#E8D4C4")
     * Used when no image is available
     */
    var color: String? = null

    // ============================================================================
    // Filtering
    // ============================================================================

    /**
     * Tags used to filter content for this subcategory
     * Content items with matching tags will be shown when this subcategory is selected
     * e.g., ["guided", "beginner"], ["sleep", "relaxation"]
     */
    @get:PropertyName("filter_tags")
    @set:PropertyName("filter_tags")
    var filterTags: List<String> = emptyList()

    // ============================================================================
    // Ordering & Status
    // ============================================================================

    /**
     * Display order (lower values appear first)
     */
    var order: Int = 0

    /**
     * Whether this subcategory is active/visible
     */
    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true

    /**
     * Number of content items in this subcategory (cached count)
     * Updated periodically by backend
     */
    @get:PropertyName("item_count")
    @set:PropertyName("item_count")
    var itemCount: Int = 0
}
