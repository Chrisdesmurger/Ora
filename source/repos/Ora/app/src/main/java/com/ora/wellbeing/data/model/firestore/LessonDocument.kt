package com.ora.wellbeing.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * LessonDocument - Firestore data model for lessons collection
 * Collection: "lessons/{lessonId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Field names use snake_case to match OraWebApp backend schema
 *
 * This model represents lessons uploaded via OraWebApp admin portal.
 * Lessons can be video or audio content, with multiple quality renditions.
 *
 * @see com.ora.wellbeing.data.mapper.LessonMapper for conversion to ContentItem
 */
@IgnoreExtraProperties
class LessonDocument() {

    // ============================================================================
    // Basic Information
    // ============================================================================

    /**
     * Lesson title (e.g., "Morning Meditation", "Gentle Yoga Flow")
     */
    var title: String = ""

    /**
     * Detailed description of the lesson content (optional)
     */
    var description: String? = null

    /**
     * Lesson type: "video" or "audio"
     */
    var type: String = "video"

    // ============================================================================
    // Program Association
    // ============================================================================

    /**
     * ID of the program this lesson belongs to
     * References: programs/{program_id}
     */
    var program_id: String = ""

    /**
     * Order/position of this lesson within the program (0-indexed)
     * Used to sort lessons in correct sequence
     */
    var order: Int = 0

    // ============================================================================
    // Media Details
    // ============================================================================

    /**
     * Duration in seconds (e.g., 600 for 10 minutes)
     */
    var duration_sec: Int? = null

    /**
     * Tags for categorization and search (e.g., ["meditation", "beginner", "morning"])
     */
    var tags: List<String> = emptyList()

    /**
     * Full transcript of the lesson (optional, for accessibility)
     */
    var transcript: String? = null

    // ============================================================================
    // Storage & Processing
    // ============================================================================

    /**
     * Processing status of the lesson
     * - "draft": Created but not uploaded
     * - "uploading": File upload in progress
     * - "processing": Transcoding to multiple renditions
     * - "ready": Available for users
     * - "failed": Processing failed
     */
    var status: String = "draft"

    /**
     * Firebase Storage path to original uploaded file
     * Example: "lessons/lesson-123/original.mp4"
     */
    var storage_path_original: String? = null

    /**
     * Video renditions in different quality levels (for adaptive streaming)
     * Structure: {
     *   "high": { "path": "gs://...", "width": 1920, "height": 1080, "bitrate_kbps": 5000 },
     *   "medium": { "path": "gs://...", "width": 1280, "height": 720, "bitrate_kbps": 2500 },
     *   "low": { "path": "gs://...", "width": 854, "height": 480, "bitrate_kbps": 1000 }
     * }
     */
    var renditions: Map<String, Map<String, Any>>? = null

    /**
     * Audio variants in different bitrates (for audio lessons or audio-only mode)
     * Structure: {
     *   "high": { "path": "gs://...", "bitrate_kbps": 320 },
     *   "medium": { "path": "gs://...", "bitrate_kbps": 192 },
     *   "low": { "path": "gs://...", "bitrate_kbps": 128 }
     * }
     */
    var audio_variants: Map<String, Map<String, Any>>? = null

    /**
     * Video/audio codec used (e.g., "h264", "aac")
     */
    var codec: String? = null

    /**
     * File size in bytes
     */
    var size_bytes: Long? = null

    // ============================================================================
    // Metadata
    // ============================================================================

    /**
     * Thumbnail image URL (for video preview)
     * Small image used in lists and grids
     */
    var thumbnail_url: String? = null

    /**
     * Preview image URL (for featured content)
     * High-quality image used for daily recommendations and hero sections
     */
    var preview_image_url: String? = null

    /**
     * MIME type of the original file (e.g., "video/mp4", "audio/m4a")
     */
    var mime_type: String? = null

    // ============================================================================
    // Timestamps
    // ============================================================================

    /**
     * Lesson creation timestamp (set automatically by Firestore)
     * CRITICAL: OraWebApp stores this as ISO 8601 String (e.g., "2025-11-04T10:30:00.000Z")
     * but Android expects Timestamp. This property accepts both formats.
     */
    @get:com.google.firebase.firestore.PropertyName("created_at")
    @set:com.google.firebase.firestore.PropertyName("created_at")
    var created_at: Any? = null
        set(value) {
            field = when (value) {
                is String -> parseIsoStringToTimestamp(value)
                is Timestamp -> value
                else -> null
            }
        }

    /**
     * Last update timestamp (set automatically by Firestore)
     * CRITICAL: OraWebApp stores this as ISO 8601 String
     */
    @get:com.google.firebase.firestore.PropertyName("updated_at")
    @set:com.google.firebase.firestore.PropertyName("updated_at")
    var updated_at: Any? = null
        set(value) {
            field = when (value) {
                is String -> parseIsoStringToTimestamp(value)
                is Timestamp -> value
                else -> null
            }
        }

    // ============================================================================
    // Authorship
    // ============================================================================

    /**
     * Firebase Auth UID of the user who created this lesson
     * Typically an admin or teacher from OraWebApp
     */
    var author_id: String = ""

    // ============================================================================
    // Scheduling (Optional)
    // ============================================================================

    /**
     * Scheduled publish timestamp (optional)
     * Lesson will become "ready" at this time
     */
    @get:com.google.firebase.firestore.PropertyName("scheduled_publish_at")
    @set:com.google.firebase.firestore.PropertyName("scheduled_publish_at")
    var scheduled_publish_at: Any? = null
        set(value) {
            field = when (value) {
                is String -> parseIsoStringToTimestamp(value)
                is Timestamp -> value
                else -> null
            }
        }

    /**
     * Scheduled archive timestamp (optional)
     * Lesson will be hidden after this time
     */
    @get:com.google.firebase.firestore.PropertyName("scheduled_archive_at")
    @set:com.google.firebase.firestore.PropertyName("scheduled_archive_at")
    var scheduled_archive_at: Any? = null
        set(value) {
            field = when (value) {
                is String -> parseIsoStringToTimestamp(value)
                is Timestamp -> value
                else -> null
            }
        }

    /**
     * Whether auto-publishing is enabled for this lesson
     */
    var auto_publish_enabled: Boolean = false

    // ============================================================================
    // Helper Functions
    // ============================================================================

    /**
     * Converts ISO 8601 string to Firestore Timestamp
     * Example: "2025-11-04T10:30:00.000Z" -> Timestamp(seconds, nanoseconds)
     */
    private fun parseIsoStringToTimestamp(isoString: String): Timestamp? {
        return try {
            val instant = java.time.Instant.parse(isoString)
            Timestamp(instant.epochSecond, instant.nano)
        } catch (e: Exception) {
            timber.log.Timber.w(e, "Failed to parse ISO timestamp: $isoString")
            null
        }
    }
}
