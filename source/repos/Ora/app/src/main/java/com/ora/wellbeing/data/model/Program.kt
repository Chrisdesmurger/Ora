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
 *
 * **i18n Support** (Issue #39 - Phase 2):
 * - French (fr): titleFr, descriptionFr, categoryFr, levelFr
 * - English (en): titleEn, descriptionEn, categoryEn, levelEn
 * - Spanish (es): titleEs, descriptionEs, categoryEs, levelEs
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

    // ============================================================================
    // i18n Fields (Issue #39 - Phase 2)
    // ============================================================================

    /**
     * French title
     * NEW (Issue #39): Added for i18n support
     */
    var titleFr: String? = null

    /**
     * English title
     * NEW (Issue #39): Added for i18n support
     */
    var titleEn: String? = null

    /**
     * Spanish title
     * NEW (Issue #39): Added for i18n support
     */
    var titleEs: String? = null

    /**
     * French description
     * NEW (Issue #39): Added for i18n support
     */
    var descriptionFr: String? = null

    /**
     * English description
     * NEW (Issue #39): Added for i18n support
     */
    var descriptionEn: String? = null

    /**
     * Spanish description
     * NEW (Issue #39): Added for i18n support
     */
    var descriptionEs: String? = null

    /**
     * French category
     * NEW (Issue #39): Added for i18n support
     */
    var categoryFr: String? = null

    /**
     * English category
     * NEW (Issue #39): Added for i18n support
     */
    var categoryEn: String? = null

    /**
     * Spanish category
     * NEW (Issue #39): Added for i18n support
     */
    var categoryEs: String? = null

    /**
     * French level/difficulty
     * NEW (Issue #39): Added for i18n support
     */
    var levelFr: String? = null

    /**
     * English level/difficulty
     * NEW (Issue #39): Added for i18n support
     */
    var levelEn: String? = null

    /**
     * Spanish level/difficulty
     * NEW (Issue #39): Added for i18n support
     */
    var levelEs: String? = null

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

    // ============================================================================
    // i18n Helper Methods (Issue #39 - Phase 2)
    // ============================================================================

    /**
     * Get localized title based on locale
     *
     * Fallback chain: locale → French → default
     *
     * @param locale Locale code ("fr", "en", "es")
     * @return Localized title
     */
    @Exclude
    fun getLocalizedTitle(locale: String = "fr"): String {
        return when (locale) {
            "en" -> titleEn?.takeIf { it.isNotBlank() } ?: title
            "es" -> titleEs?.takeIf { it.isNotBlank() } ?: titleFr ?: title
            else -> titleFr?.takeIf { it.isNotBlank() } ?: title
        }
    }

    /**
     * Get localized description based on locale
     *
     * Fallback chain: locale → French → default
     *
     * @param locale Locale code ("fr", "en", "es")
     * @return Localized description
     */
    @Exclude
    fun getLocalizedDescription(locale: String = "fr"): String {
        return when (locale) {
            "en" -> descriptionEn?.takeIf { it.isNotBlank() } ?: description
            "es" -> descriptionEs?.takeIf { it.isNotBlank() } ?: descriptionFr ?: description
            else -> descriptionFr?.takeIf { it.isNotBlank() } ?: description
        }
    }

    /**
     * Get localized category based on locale
     *
     * Fallback chain: locale → French → default
     *
     * @param locale Locale code ("fr", "en", "es")
     * @return Localized category
     */
    @Exclude
    fun getLocalizedCategory(locale: String = "fr"): String {
        return when (locale) {
            "en" -> categoryEn?.takeIf { it.isNotBlank() } ?: category
            "es" -> categoryEs?.takeIf { it.isNotBlank() } ?: categoryFr ?: category
            else -> categoryFr?.takeIf { it.isNotBlank() } ?: category
        }
    }

    /**
     * Get localized level/difficulty based on locale
     *
     * Fallback chain: locale → French → default
     *
     * @param locale Locale code ("fr", "en", "es")
     * @return Localized level
     */
    @Exclude
    fun getLocalizedLevel(locale: String = "fr"): String {
        return when (locale) {
            "en" -> levelEn?.takeIf { it.isNotBlank() } ?: level
            "es" -> levelEs?.takeIf { it.isNotBlank() } ?: levelFr ?: level
            else -> levelFr?.takeIf { it.isNotBlank() } ?: level
        }
    }
}
