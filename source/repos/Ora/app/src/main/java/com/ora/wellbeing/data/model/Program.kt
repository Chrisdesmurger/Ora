package com.ora.wellbeing.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

/**
 * Program - Firestore data model for program catalog
 * Collection: "programs/{programId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Timestamps are handled natively by Firestore SDK with @ServerTimestamp
 */
@IgnoreExtraProperties
class Program() {
    var id: String = ""

    var title: String = ""
    var description: String = ""
    var category: String = ""
    var duration: Int = 0 // days
    var level: String = ""
    var participantCount: Int = 0
    var rating: Float = 0.0f
    var thumbnailUrl: String? = null
    var instructor: String? = null
    var isPremiumOnly: Boolean = false
    var sessions: List<Map<String, Any>> = emptyList()
    var isActive: Boolean = true

    // Firestore Timestamp - automatically populated with @ServerTimestamp on create/update
    @ServerTimestamp
    var createdAt: Timestamp? = null

    @ServerTimestamp
    var updatedAt: Timestamp? = null

    companion object {
        // Valid categories
        val VALID_CATEGORIES = listOf(
            "Méditation",
            "Yoga",
            "Bien-être",
            "Défis",
            "Sommeil",
            "Pilates"
        )

        // Valid levels
        val VALID_LEVELS = listOf(
            "Débutant",
            "Intermédiaire",
            "Avancé",
            "Tous niveaux"
        )
    }

    /**
     * Formats duration for display (e.g., "7 jours", "21 jours")
     */
    @Exclude
    fun getFormattedDuration(): String {
        return when {
            duration == 1 -> "1 jour"
            duration < 7 -> "$duration jours"
            duration == 7 -> "1 semaine"
            duration % 7 == 0 -> "${duration / 7} semaines"
            else -> "$duration jours"
        }
    }

    /**
     * Checks if this program is accessible for a given user plan tier
     */
    @Exclude
    fun isAccessibleForPlan(planTier: String): Boolean {
        return !isPremiumOnly || planTier == "premium"
    }

    /**
     * Returns progress percentage for a given current day
     */
    @Exclude
    fun getProgressPercentage(currentDay: Int): Int {
        return if (duration > 0) {
            ((currentDay.toFloat() / duration) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }

    /**
     * Checks if program is completed
     */
    @Exclude
    fun isCompleted(currentDay: Int): Boolean {
        return currentDay >= duration
    }

    /**
     * Returns the description preview (first 150 characters)
     */
    @Exclude
    fun getDescriptionPreview(): String {
        return if (description.length > 150) {
            "${description.take(147)}..."
        } else {
            description
        }
    }

    /**
     * Gets the number of sessions in this program
     */
    @Exclude
    fun getSessionCount(): Int = sessions.size

    /**
     * Checks if this program has a high rating (>= 4.0)
     */
    @Exclude
    fun hasHighRating(): Boolean = rating >= 4.0f

    /**
     * Checks if this program is popular (high participant count)
     */
    @Exclude
    fun isPopular(): Boolean = participantCount >= 100
}
