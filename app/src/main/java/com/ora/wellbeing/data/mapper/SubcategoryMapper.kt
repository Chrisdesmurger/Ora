package com.ora.wellbeing.data.mapper

import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.data.model.firestore.SubcategoryDocument
import timber.log.Timber
import java.util.Locale

/**
 * SubcategoryMapper - Converts between Firestore SubcategoryDocument and Android SubcategoryItem
 *
 * Handles:
 * - i18n name/description resolution (FR -> EN -> default)
 * - Color parsing from hex string
 * - Sorting by order field
 */
object SubcategoryMapper {

    /**
     * Convert Firestore document to Android model with i18n support
     *
     * @param id Document ID
     * @param doc SubcategoryDocument from Firestore
     * @param languageCode Current language code (fr, en, es)
     * @return SubcategoryItem for Android UI
     */
    fun fromFirestore(
        id: String,
        doc: SubcategoryDocument,
        languageCode: String = getCurrentLanguageCode()
    ): SubcategoryItem {
        return SubcategoryItem(
            id = id.ifBlank { doc.id },
            parentCategory = doc.parentCategory,
            name = getLocalizedName(doc, languageCode),
            description = getLocalizedDescription(doc, languageCode),
            iconUrl = doc.iconUrl,
            imageUrl = doc.imageUrl,
            color = SubcategoryItem.parseColor(doc.color),
            filterTags = doc.filterTags,
            order = doc.order,
            itemCount = doc.itemCount
        )
    }

    /**
     * Convert list of Firestore documents to Android models
     * Filters out inactive subcategories and sorts by order
     *
     * @param docs Map of document ID to SubcategoryDocument
     * @param languageCode Current language code
     * @return List of SubcategoryItem sorted by order
     */
    fun fromFirestoreList(
        docs: Map<String, SubcategoryDocument>,
        languageCode: String = getCurrentLanguageCode()
    ): List<SubcategoryItem> {
        return docs
            .filter { (_, doc) -> doc.isActive }
            .map { (id, doc) -> fromFirestore(id, doc, languageCode) }
            .sortedBy { it.order }
    }

    /**
     * Convert raw Firestore map to SubcategoryDocument
     * Used when subcategories are embedded in category document
     */
    @Suppress("UNCHECKED_CAST")
    fun fromFirestoreMap(map: Map<String, Any>): SubcategoryDocument {
        return SubcategoryDocument().apply {
            id = map["id"] as? String ?: ""
            parentCategory = map["parent_category"] as? String ?: ""
            name = map["name"] as? String ?: ""
            nameFr = map["name_fr"] as? String
            nameEn = map["name_en"] as? String
            nameEs = map["name_es"] as? String
            description = map["description"] as? String
            descriptionFr = map["description_fr"] as? String
            descriptionEn = map["description_en"] as? String
            descriptionEs = map["description_es"] as? String
            iconUrl = map["icon_url"] as? String
            imageUrl = map["image_url"] as? String
            color = map["color"] as? String
            filterTags = (map["filter_tags"] as? List<String>) ?: emptyList()
            order = (map["order"] as? Number)?.toInt() ?: 0
            isActive = map["is_active"] as? Boolean ?: true
            itemCount = (map["item_count"] as? Number)?.toInt() ?: 0
        }
    }

    /**
     * Convert list of raw Firestore maps to SubcategoryItems
     */
    fun fromFirestoreMapList(
        maps: List<Map<String, Any>>?,
        languageCode: String = getCurrentLanguageCode()
    ): List<SubcategoryItem> {
        if (maps.isNullOrEmpty()) return emptyList()

        return maps.mapNotNull { map ->
            try {
                val doc = fromFirestoreMap(map)
                if (doc.isActive) {
                    fromFirestore(doc.id, doc, languageCode)
                } else null
            } catch (e: Exception) {
                Timber.e(e, "Failed to map subcategory from map")
                null
            }
        }.sortedBy { it.order }
    }

    // ============================================================================
    // i18n Helpers
    // ============================================================================

    /**
     * Get localized name with fallback chain: locale -> FR -> default
     */
    private fun getLocalizedName(doc: SubcategoryDocument, languageCode: String): String {
        return when (languageCode.lowercase()) {
            "fr" -> doc.nameFr?.takeIf { it.isNotBlank() } ?: doc.name
            "en" -> doc.nameEn?.takeIf { it.isNotBlank() } ?: doc.nameFr ?: doc.name
            "es" -> doc.nameEs?.takeIf { it.isNotBlank() } ?: doc.nameFr ?: doc.name
            else -> doc.nameFr?.takeIf { it.isNotBlank() } ?: doc.name
        }.ifBlank { doc.name }
    }

    /**
     * Get localized description with fallback chain: locale -> FR -> default
     */
    private fun getLocalizedDescription(doc: SubcategoryDocument, languageCode: String): String? {
        val desc = when (languageCode.lowercase()) {
            "fr" -> doc.descriptionFr ?: doc.description
            "en" -> doc.descriptionEn ?: doc.descriptionFr ?: doc.description
            "es" -> doc.descriptionEs ?: doc.descriptionFr ?: doc.description
            else -> doc.descriptionFr ?: doc.description
        }
        return desc?.takeIf { it.isNotBlank() }
    }

    /**
     * Get current device language code
     */
    private fun getCurrentLanguageCode(): String {
        val locale = Locale.getDefault().language
        return when (locale) {
            "fr" -> "fr"
            "en" -> "en"
            "es" -> "es"
            else -> "fr" // Default to French
        }
    }
}
