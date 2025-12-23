package com.ora.wellbeing.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * ProgramDocument - Firestore data model for programs collection
 * Collection: "programs/{programId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Field names use snake_case to match OraWebApp backend schema
 *
 * This model represents programs (meditation/yoga courses) created via OraWebApp admin portal.
 * Programs consist of multiple lessons arranged in a specific order.
 *
 * **i18n Support** (Issue #39 - Phase 2):
 * - French (fr): title_fr, description_fr, category_fr, difficulty_fr
 * - English (en): title_en, description_en, category_en, difficulty_en
 * - Spanish (es): title_es, description_es, category_es, difficulty_es
 *
 * @see com.ora.wellbeing.data.mapper.ProgramMapper for conversion to Program
 */
@IgnoreExtraProperties
class ProgramDocument() {

    // ============================================================================
    // Basic Information
    // ============================================================================

    /**
     * Program title (e.g., "7-Day Meditation Challenge", "Beginner Yoga Series")
     * Default/fallback title (usually French)
     */
    var title: String = ""

    /**
     * Detailed description of the program content and goals
     * Default/fallback description (usually French)
     */
    var description: String = ""

    // ============================================================================
    // i18n Fields (Issue #39 - Phase 2)
    // ============================================================================

    /**
     * French title
     * NEW (Issue #39): Added for i18n support
     */
    var title_fr: String? = null

    /**
     * English title
     * NEW (Issue #39): Added for i18n support
     */
    var title_en: String? = null

    /**
     * Spanish title
     * NEW (Issue #39): Added for i18n support
     */
    var title_es: String? = null

    /**
     * French description
     * NEW (Issue #39): Added for i18n support
     */
    var description_fr: String? = null

    /**
     * English description
     * NEW (Issue #39): Added for i18n support
     */
    var description_en: String? = null

    /**
     * Spanish description
     * NEW (Issue #39): Added for i18n support
     */
    var description_es: String? = null

    /**
     * French category (e.g., "Méditation", "Yoga")
     * NEW (Issue #39): Added for i18n support
     */
    var category_fr: String? = null

    /**
     * English category (e.g., "Meditation", "Yoga")
     * NEW (Issue #39): Added for i18n support
     */
    var category_en: String? = null

    /**
     * Spanish category (e.g., "Meditación", "Yoga")
     * NEW (Issue #39): Added for i18n support
     */
    var category_es: String? = null

    /**
     * French difficulty level (e.g., "Débutant", "Intermédiaire", "Avancé")
     * NEW (Issue #39): Added for i18n support
     */
    var difficulty_fr: String? = null

    /**
     * English difficulty level (e.g., "Beginner", "Intermediate", "Advanced")
     * NEW (Issue #39): Added for i18n support
     */
    var difficulty_en: String? = null

    /**
     * Spanish difficulty level (e.g., "Principiante", "Intermedio", "Avanzado")
     * NEW (Issue #39): Added for i18n support
     */
    var difficulty_es: String? = null

    // ============================================================================
    // Classification
    // ============================================================================

    /**
     * Program category
     * Valid values: "meditation", "yoga", "mindfulness", "wellness"
     * Default/fallback category (usually English/lowercase)
     */
    var category: String = "meditation"

    /**
     * Difficulty level
     * Valid values: "beginner", "intermediate", "advanced"
     * Default/fallback difficulty (usually English/lowercase)
     */
    var difficulty: String = "beginner"

    // ============================================================================
    // Structure
    // ============================================================================

    /**
     * Program duration in days (e.g., 7 for a week-long program)
     */
    var duration_days: Int = 7

    /**
     * Array of lesson IDs in the correct order
     * Each ID references lessons/{lessonId}
     *
     * Example: ["lesson-001", "lesson-002", "lesson-003"]
     * The order in this array determines the lesson sequence in the program
     */
    var lessons: List<String> = emptyList()

    // ============================================================================
    // Media
    // ============================================================================

    /**
     * Cover image URL for the program (public URL)
     */
    var cover_image_url: String? = null

    /**
     * Firebase Storage path to cover image (for deletion)
     * Example: "programs/prog-123/cover.jpg"
     */
    var cover_storage_path: String? = null

    // ============================================================================
    // Publishing
    // ============================================================================

    /**
     * Publication status
     * - "draft": Not visible to users
     * - "published": Active and visible to users
     * - "archived": No longer active but kept for records
     */
    var status: String = "draft"

    /**
     * Scheduled publish timestamp (optional)
     * Program will become "published" at this time
     */
    var scheduled_publish_at: Timestamp? = null

    /**
     * Scheduled archive timestamp (optional)
     * Program will be archived after this time
     */
    var scheduled_archive_at: Timestamp? = null

    /**
     * Whether auto-publishing is enabled for this program
     */
    var auto_publish_enabled: Boolean = false

    // ============================================================================
    // Metadata
    // ============================================================================

    /**
     * Tags for categorization and search (e.g., ["beginner", "morning", "stress-relief"])
     */
    var tags: List<String> = emptyList()

    /**
     * Firebase Auth UID of the user who created this program
     * Typically an admin or teacher from OraWebApp
     */
    var author_id: String = ""

    // ============================================================================
    // Timestamps
    // ============================================================================

    /**
     * Program creation timestamp (set automatically by Firestore)
     */
    var created_at: Timestamp? = null

    /**
     * Last update timestamp (set automatically by Firestore)
     */
    var updated_at: Timestamp? = null

    // ============================================================================
    // Statistics (Not yet implemented in OraWebApp)
    // ============================================================================

    /**
     * Number of users who have started this program (optional, future use)
     */
    var participant_count: Int = 0

    /**
     * Average rating from users (optional, future use)
     */
    var rating: Float = 0.0f
}
