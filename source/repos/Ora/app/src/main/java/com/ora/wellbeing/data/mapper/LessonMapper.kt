package com.ora.wellbeing.data.mapper

import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.firestore.LessonDocument
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * LessonMapper - Converts between Firestore LessonDocument and Android ContentItem
 *
 * This mapper handles the conversion between:
 * - Backend schema (snake_case fields from OraWebApp)
 * - Android schema (camelCase fields for Ora app)
 *
 * Key Conversions:
 * - snake_case → camelCase (program_id → programId)
 * - duration_sec → durationMinutes (seconds to minutes)
 * - type ("video"|"audio") → category (mapped via tags)
 * - renditions (multi-quality) → videoUrl (best quality)
 * - status ("ready") → isActive (boolean)
 */
object LessonMapper {

    /**
     * Converts Firestore LessonDocument to Android ContentItem
     *
     * @param id Firestore document ID
     * @param doc LessonDocument from Firestore (snake_case)
     * @return ContentItem for Android app (camelCase)
     */
    fun fromFirestore(id: String, doc: LessonDocument): ContentItem {
        Timber.d("Mapping lesson from Firestore: id=$id, title=${doc.title}, status=${doc.status}")

        return ContentItem().apply {
            this.id = id
            this.title = doc.title
            this.description = doc.description ?: ""
            this.category = mapLessonTypeToCategory(doc.type, doc.tags)
            this.duration = formatDuration(doc.duration_sec)
            this.durationMinutes = (doc.duration_sec ?: 0) / 60
            this.instructor = extractInstructorFromTags(doc.tags)
            this.thumbnailUrl = doc.thumbnail_url
            this.previewImageUrl = doc.preview_image_url
            this.videoUrl = extractBestVideoUrl(doc.renditions)
            this.audioUrl = extractBestAudioUrl(doc.audio_variants)
            this.isPremiumOnly = false // TODO: Determine from program settings
            this.isPopular = false // TODO: Calculate from usage stats
            this.isNew = isRecent(doc.created_at as? Timestamp)
            this.rating = 0.0f // TODO: Fetch from ratings collection
            this.completionCount = 0 // TODO: Fetch from stats
            this.tags = doc.tags
            this.isActive = doc.status == "ready"
            this.order = doc.order  // For sorting (lower values = higher priority, negative = featured)
            this.createdAt = doc.created_at as? Timestamp
            this.updatedAt = doc.updated_at as? Timestamp
            this.publishedAt = doc.scheduled_publish_at as? Timestamp
        }
    }

    /**
     * Extracts the best quality video URL from renditions
     * Priority: high > medium > low
     *
     * @param renditions Map of video quality variants
     * @return URL string or null if no renditions available
     */
    private fun extractBestVideoUrl(renditions: Map<String, Map<String, Any>>?): String? {
        if (renditions == null) {
            Timber.w("No video renditions available")
            return null
        }

        val path = renditions["high"]?.get("path") as? String
            ?: renditions["medium"]?.get("path") as? String
            ?: renditions["low"]?.get("path") as? String

        if (path == null) {
            Timber.w("No video path found in renditions")
            return null
        }

        val url = buildFirebaseStorageUrl(path)

        Timber.d("Extracted video URL: quality=${when {
            path == renditions["high"]?.get("path") -> "high"
            path == renditions["medium"]?.get("path") -> "medium"
            path == renditions["low"]?.get("path") -> "low"
            else -> "none"
        }}, url=$url")

        return url
    }

    /**
     * Extracts the best quality audio URL from audio variants
     * Priority: high > medium > low
     *
     * @param audioVariants Map of audio quality variants
     * @return URL string or null if no variants available
     */
    private fun extractBestAudioUrl(audioVariants: Map<String, Map<String, Any>>?): String? {
        if (audioVariants == null) {
            Timber.w("No audio variants available")
            return null
        }

        val path = audioVariants["high"]?.get("path") as? String
            ?: audioVariants["medium"]?.get("path") as? String
            ?: audioVariants["low"]?.get("path") as? String

        if (path == null) {
            Timber.w("No audio path found in variants")
            return null
        }

        val url = buildFirebaseStorageUrl(path)
        Timber.d("Extracted audio URL: quality=${when {
            path == audioVariants["high"]?.get("path") -> "high"
            path == audioVariants["medium"]?.get("path") -> "medium"
            path == audioVariants["low"]?.get("path") -> "low"
            else -> "none"
        }}, url=$url")

        return url
    }

    /**
     * Converts Firebase Storage path to public HTTPS URL
     *
     * Converts storage paths like "media/lessons/ABC/audio/high.m4a"
     * to Firebase Storage URLs like:
     * "https://firebasestorage.googleapis.com/v0/b/ora-wellbeing.firebasestorage.app/o/media%2Flessons%2FABC%2Faudio%2Fhigh.m4a?alt=media"
     *
     * @param path Firebase Storage path (e.g., "media/lessons/ABC/audio/high.m4a")
     * @return Public HTTPS URL for Firebase Storage
     */
    private fun buildFirebaseStorageUrl(path: String): String {
        val bucket = "ora-wellbeing.firebasestorage.app"
        val encodedPath = path.replace("/", "%2F")
        val url = "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media"
        Timber.d("Built Firebase Storage URL: path=$path -> url=$url")
        return url
    }

    /**
     * Maps lesson type and tags to Android category
     *
     * Uses tags to determine the most appropriate category from:
     * - Méditation, Yoga, Respiration, Pilates, Bien-être, Sommeil
     *
     * @param type Lesson type ("video" or "audio")
     * @param tags List of tags for categorization
     * @return Category string in French
     */
    private fun mapLessonTypeToCategory(type: String, tags: List<String>): String {
        // Priority-based category mapping from tags
        return when {
            tags.any { it.equals("yoga", ignoreCase = true) } -> "Yoga"
            tags.any { it.equals("meditation", ignoreCase = true) || it.equals("méditation", ignoreCase = true) } -> "Méditation"
            tags.any { it.equals("breathing", ignoreCase = true) || it.equals("respiration", ignoreCase = true) } -> "Respiration"
            tags.any { it.equals("pilates", ignoreCase = true) } -> "Pilates"
            tags.any { it.equals("sleep", ignoreCase = true) || it.equals("sommeil", ignoreCase = true) } -> "Sommeil"
            else -> "Bien-être" // Default category
        }
    }

    /**
     * Formats duration from seconds to readable string
     *
     * @param durationSec Duration in seconds
     * @return Formatted string (e.g., "10 min", "1h 30 min")
     */
    private fun formatDuration(durationSec: Int?): String {
        if (durationSec == null || durationSec == 0) return ""

        val minutes = durationSec / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60

        return when {
            hours > 0 && remainingMinutes > 0 -> "${hours}h ${remainingMinutes} min"
            hours > 0 -> "${hours}h"
            else -> "$minutes min"
        }
    }

    /**
     * Extracts instructor name from tags
     * Looks for tags like "instructor:John Doe"
     *
     * @param tags List of tags
     * @return Instructor name or empty string
     */
    private fun extractInstructorFromTags(tags: List<String>): String {
        val instructorTag = tags.find { it.startsWith("instructor:", ignoreCase = true) }
        return instructorTag?.substringAfter("instructor:", "") ?: ""
    }

    /**
     * Checks if the lesson was created recently (within last 7 days)
     *
     * @param createdAt Firestore timestamp
     * @return True if created within last 7 days
     */
    private fun isRecent(createdAt: Timestamp?): Boolean {
        if (createdAt == null) return false

        val now = System.currentTimeMillis()
        val createdTime = createdAt.toDate().time
        val daysSinceCreation = TimeUnit.MILLISECONDS.toDays(now - createdTime)

        return daysSinceCreation <= 7
    }

    /**
     * Converts ContentItem back to LessonDocument (for updates)
     * Note: This is primarily for future use when we allow editing from Android
     *
     * @param content ContentItem from Android app
     * @return LessonDocument for Firestore
     */
    fun toFirestore(content: ContentItem): LessonDocument {
        return LessonDocument().apply {
            this.title = content.title
            this.description = content.description.takeIf { it.isNotBlank() }
            this.type = mapCategoryToLessonType(content.category)
            this.duration_sec = content.durationMinutes * 60
            this.tags = content.tags
            this.status = if (content.isActive) "ready" else "draft"
            this.thumbnail_url = content.thumbnailUrl
            // Note: renditions and audio_variants are not mapped back
            // as they are generated by the backend processing pipeline
        }
    }

    /**
     * Maps Android category back to lesson type
     *
     * @param category Category string
     * @return "video" or "audio"
     */
    private fun mapCategoryToLessonType(category: String): String {
        // Most lessons are video by default
        // Audio-only lessons should have explicit tags
        return "video"
    }
}
