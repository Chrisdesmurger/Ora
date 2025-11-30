package com.ora.wellbeing.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

/**
 * ContentItem - Firestore data model for content catalog
 * Collection: "content/{contentId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Timestamps are handled natively by Firestore SDK with @ServerTimestamp
 */
@IgnoreExtraProperties
class ContentItem() {
    var id: String = ""

    var title: String = ""
    var category: String = ""
    var duration: String = ""
    var durationMinutes: Int = 0
    var instructor: String = ""
    var description: String = ""
    var thumbnailUrl: String? = null
    var previewImageUrl: String? = null
    var videoUrl: String? = null
    var audioUrl: String? = null
    var isPremiumOnly: Boolean = false
    var isPopular: Boolean = false
    var isNew: Boolean = false
    var rating: Float = 0.0f
    var completionCount: Int = 0
    var tags: List<String> = emptyList()
    var isActive: Boolean = true
    var order: Int = 0  // Order for sorting (lower values appear first, negative for featured content)

    // Firestore Timestamp - automatically populated with @ServerTimestamp on create/update
    @ServerTimestamp
    var createdAt: Timestamp? = null

    @ServerTimestamp
    var updatedAt: Timestamp? = null

    var publishedAt: Timestamp? = null

    companion object {
        // Valid categories
        val VALID_CATEGORIES = listOf(
            "Méditation",
            "Yoga",
            "Respiration",
            "Pilates",
            "Bien-être",
            "Sommeil"
        )

        // Valid types
        val VALID_TYPES = listOf(
            "video",
            "audio",
            "article"
        )
    }

    /**
     * Checks if this content is accessible for a given user plan tier
     */
    @Exclude
    fun isAccessibleForPlan(planTier: String): Boolean {
        return !isPremiumOnly || planTier == "premium"
    }

    /**
     * Returns the formatted duration
     */
    @Exclude
    fun getFormattedDuration(): String {
        return duration.ifBlank { "${durationMinutes} min" }
    }

    /**
     * Checks if this content matches a search query
     */
    @Exclude
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return title.lowercase().contains(lowerQuery) ||
                description.lowercase().contains(lowerQuery) ||
                instructor.lowercase().contains(lowerQuery) ||
                tags.any { it.lowercase().contains(lowerQuery) }
    }

    /**
     * Returns a preview of the description (first 100 characters)
     */
    @Exclude
    fun getDescriptionPreview(): String {
        return if (description.length > 100) {
            "${description.take(97)}..."
        } else {
            description
        }
    }
}
