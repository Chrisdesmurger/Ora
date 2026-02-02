package com.ora.wellbeing.data.mapper

import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.firestore.LessonDocument
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * LessonMapper - Converts between Firestore LessonDocument and Android ContentItem
 *
 * This mapper handles the conversion between:
 * - Backend schema (snake_case fields from OraWebApp)
 * - Android schema (camelCase fields for Ora app)
 *
 * Key Conversions:
 * - snake_case -> camelCase (program_id -> programId)
 * - duration_sec -> durationMinutes (seconds to minutes)
 * - type ("video"|"audio") -> category (mapped via tags)
 * - renditions (multi-quality) -> videoUrl (best quality)
 * - status ("ready") -> isActive (boolean)
 * - need_tags -> needTags (NEW: for "Ton besoin du jour" filtering)
 */
object LessonMapper {

    /**
     * Converts Firestore LessonDocument to Android ContentItem
     *
     * @param id Firestore document ID
     * @param doc LessonDocument from Firestore (snake_case)
     * @param languageCode Current language code (fr, en, es) - defaults to device locale
     * @return ContentItem for Android app (camelCase)
     */
    fun fromFirestore(
        id: String,
        doc: LessonDocument,
        languageCode: String = getCurrentLanguageCode()
    ): ContentItem {

        return ContentItem().apply {
            this.id = id
            // Use i18n title based on device locale with fallback chain
            this.title = getLocalizedTitle(doc, languageCode).ifBlank { id }
            // Use i18n description based on device locale
            this.description = getLocalizedDescription(doc, languageCode)
            // Use i18n category based on device locale
            this.category = getLocalizedCategory(doc, languageCode)
                ?: mapLessonTypeToCategory(doc.type, doc.tags)
            this.duration = formatDuration(doc.duration_sec)
            this.durationMinutes = (doc.duration_sec ?: 0) / 60
            this.instructor = extractInstructorFromTags(doc.tags)
            // Priority: preview_image_url (always valid URL) > thumbnail_url (may be relative path)
            // Some lessons have thumbnail_url as relative path (media/lessons/.../thumb.jpg) that doesn't exist
            // preview_image_url is always a complete URL when present
            val previewUrl = doc.preview_image_url?.takeIf { it.isNotBlank() }
            val thumbUrl = doc.thumbnail_url?.takeIf { it.isNotBlank() }
            this.previewImageUrl = toFullUrl(previewUrl ?: thumbUrl)
            this.thumbnailUrl = toFullUrl(previewUrl ?: thumbUrl)
            // FIX: Use storage_path_original as fallback if renditions not available
            this.videoUrl = extractBestVideoUrl(doc.renditions, doc.storage_path_original, doc.type)
            this.audioUrl = extractBestAudioUrl(doc.audio_variants, doc.storage_path_original, doc.type)
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
            // NEW: Map need_tags for "Ton besoin du jour" filtering (Issue #33)
            this.needTags = doc.need_tags.ifEmpty {
                // Fallback: derive need_tags from regular tags if not explicitly set
                deriveNeedTagsFromTags(doc.tags, doc.type)
            }
        }
    }

    /**
     * Extracts the best quality video path from renditions
     * Priority: high > medium > low > storage_path_original
     *
     * Returns the Firebase Storage path, which will be converted to a signed download URL
     * by PracticeRepository when loading the practice for playback.
     *
     * FIX: Added fallback to storage_path_original for lessons without processed renditions
     *
     * @param renditions Map of video quality variants
     * @param storagePathOriginal Original file path (fallback)
     * @param type Lesson type ("video" or "audio")
     * @return Storage path string or null if no video available
     */
    private fun extractBestVideoUrl(
        renditions: Map<String, Map<String, Any>>?,
        storagePathOriginal: String?,
        type: String
    ): String? {
        // Try renditions first (processed, optimized files)
        if (renditions != null) {
            val path = renditions["high"]?.get("path") as? String
                ?: renditions["medium"]?.get("path") as? String
                ?: renditions["low"]?.get("path") as? String

            if (path != null) {
                Timber.d("✅ Extracted video path from renditions: quality=${when {
                    path == renditions["high"]?.get("path") -> "high"
                    path == renditions["medium"]?.get("path") -> "medium"
                    path == renditions["low"]?.get("path") -> "low"
                    else -> "unknown"
                }}, path=$path")
                return path
            }
        }

        // Fallback: Use original file if it's a video lesson
        if (type == "video" && !storagePathOriginal.isNullOrBlank()) {
            Timber.d("⚠️ No renditions, using storage_path_original: $storagePathOriginal")
            return storagePathOriginal
        }

        Timber.w("❌ No video path available (renditions=null, original=${storagePathOriginal ?: "null"})")
        return null
    }

    /**
     * Extracts the best quality audio path from audio variants
     * Priority: high > medium > low > storage_path_original
     *
     * Returns the Firebase Storage path, which will be converted to a signed download URL
     * by PracticeRepository when loading the practice for playback.
     *
     * FIX: Added fallback to storage_path_original for lessons without processed audio variants
     *
     * @param audioVariants Map of audio quality variants
     * @param storagePathOriginal Original file path (fallback)
     * @param type Lesson type ("video" or "audio")
     * @return Storage path string or null if no audio available
     */
    private fun extractBestAudioUrl(
        audioVariants: Map<String, Map<String, Any>>?,
        storagePathOriginal: String?,
        type: String
    ): String? {
        // Try audio variants first (processed, optimized files)
        if (audioVariants != null) {
            val path = audioVariants["high"]?.get("path") as? String
                ?: audioVariants["medium"]?.get("path") as? String
                ?: audioVariants["low"]?.get("path") as? String

            if (path != null) {
                Timber.d("✅ Extracted audio path from variants: quality=${when {
                    path == audioVariants["high"]?.get("path") -> "high"
                    path == audioVariants["medium"]?.get("path") -> "medium"
                    path == audioVariants["low"]?.get("path") -> "low"
                    else -> "unknown"
                }}, path=$path")
                return path
            }
        }

        // Fallback: Use original file if it's an audio lesson
        if (type == "audio" && !storagePathOriginal.isNullOrBlank()) {
            Timber.d("⚠️ No audio variants, using storage_path_original: $storagePathOriginal")
            return storagePathOriginal
        }

        Timber.d("ℹ️ No audio path available for type=$type (this is normal for video-only lessons)")
        return null
    }

    /**
     * Maps lesson type and tags to Android category
     *
     * Uses tags to determine the most appropriate category from:
     * - Meditation, Yoga, Respiration, Pilates, Bien-etre, Sommeil, Massage
     *
     * @param type Lesson type ("video" or "audio")
     * @param tags List of tags for categorization
     * @return Category string in French
     */
    private fun mapLessonTypeToCategory(type: String, tags: List<String>): String {
        // Priority-based category mapping from tags
        return when {
            tags.any { it.equals("yoga", ignoreCase = true) } -> "Yoga"
            tags.any { it.equals("meditation", ignoreCase = true) || it.equals("meditation", ignoreCase = true) } -> "Meditation"
            tags.any { it.equals("breathing", ignoreCase = true) || it.equals("respiration", ignoreCase = true) } -> "Respiration"
            tags.any { it.equals("pilates", ignoreCase = true) } -> "Pilates"
            tags.any { it.equals("sleep", ignoreCase = true) || it.equals("sommeil", ignoreCase = true) } -> "Sommeil"
            tags.any { it.equals("massage", ignoreCase = true) || it.equals("auto-massage", ignoreCase = true) || it.equals("self-massage", ignoreCase = true) } -> "Massage"
            tags.any { it.equals("wellness", ignoreCase = true) || it.equals("bien-etre", ignoreCase = true) } -> "Bien-etre"
            else -> "Bien-etre" // Default category
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
     * Derives need_tags from regular tags when not explicitly set
     * This provides backward compatibility for lessons without need_tags
     *
     * NEW: Added for Issue #33 - Daily Needs Section
     *
     * @param tags Regular lesson tags
     * @param type Lesson type ("video" or "audio")
     * @return Derived need_tags list
     */
    private fun deriveNeedTagsFromTags(tags: List<String>, type: String): List<String> {
        val derivedTags = mutableListOf<String>()

        // Map regular tags to need tags
        tags.forEach { tag ->
            when {
                // Morning/energizing content
                tag.equals("morning", ignoreCase = true) ||
                tag.equals("matin", ignoreCase = true) ||
                tag.equals("energizing", ignoreCase = true) ||
                tag.equals("wake-up", ignoreCase = true) ||
                tag.equals("reveil", ignoreCase = true) -> {
                    derivedTags.add("morning")
                    derivedTags.add("energizing")
                }

                // Evening/bedtime content
                tag.equals("evening", ignoreCase = true) ||
                tag.equals("soir", ignoreCase = true) ||
                tag.equals("bedtime", ignoreCase = true) ||
                tag.equals("sleep", ignoreCase = true) ||
                tag.equals("sommeil", ignoreCase = true) -> {
                    derivedTags.add("evening")
                    derivedTags.add("bedtime")
                    derivedTags.add("sleep")
                }

                // Relaxation content
                tag.equals("relaxation", ignoreCase = true) ||
                tag.equals("relax", ignoreCase = true) ||
                tag.equals("detente", ignoreCase = true) ||
                tag.equals("calm", ignoreCase = true) ||
                tag.equals("stress", ignoreCase = true) ||
                tag.equals("anti-stress", ignoreCase = true) -> {
                    derivedTags.add("relaxation")
                    derivedTags.add("stress-relief")
                }

                // Breathing content
                tag.equals("breathing", ignoreCase = true) ||
                tag.equals("respiration", ignoreCase = true) ||
                tag.equals("breathwork", ignoreCase = true) -> {
                    derivedTags.add("breathing")
                    derivedTags.add("relaxation")
                }

                // Stretching content
                tag.equals("stretching", ignoreCase = true) ||
                tag.equals("etirement", ignoreCase = true) ||
                tag.equals("gentle", ignoreCase = true) ||
                tag.equals("doux", ignoreCase = true) -> {
                    derivedTags.add("stretching")
                    derivedTags.add("gentle")
                }

                // Meditation content
                tag.equals("meditation", ignoreCase = true) -> {
                    derivedTags.add("meditation")
                    derivedTags.add("relaxation")
                }
            }
        }

        return derivedTags.distinct()
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
            this.need_tags = content.needTags  // NEW: Map needTags back to need_tags
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

    /**
     * Converts a storage path or URL to a full Firebase Storage URL
     *
     * Some lessons have relative paths (e.g., "media/lessons/.../thumb.jpg")
     * while others have full URLs. This normalizes them all to full URLs.
     *
     * @param path Storage path or URL (nullable)
     * @return Full URL or null if input is null/blank
     */
    private fun toFullUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null

        // Already a full URL
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }

        // Convert relative path to Firebase Storage URL
        // Format: https://storage.googleapis.com/BUCKET/PATH
        val bucket = "ora-wellbeing.firebasestorage.app"
        return "https://storage.googleapis.com/$bucket/$path"
    }

    // ============================================================================
    // i18n Helpers
    // ============================================================================

    /**
     * Get localized title based on device locale
     * Fallback chain: locale -> FR -> default title
     */
    private fun getLocalizedTitle(doc: LessonDocument, languageCode: String): String {
        return when (languageCode.lowercase()) {
            "fr" -> doc.title_fr?.takeIf { it.isNotBlank() } ?: doc.title
            "en" -> doc.title_en?.takeIf { it.isNotBlank() } ?: doc.title_fr ?: doc.title
            "es" -> doc.title_es?.takeIf { it.isNotBlank() } ?: doc.title_fr ?: doc.title
            else -> doc.title_fr?.takeIf { it.isNotBlank() } ?: doc.title
        }.ifBlank { doc.title }
    }

    /**
     * Get localized description based on device locale
     * Fallback chain: locale -> FR -> default description
     */
    private fun getLocalizedDescription(doc: LessonDocument, languageCode: String): String {
        val desc = when (languageCode.lowercase()) {
            "fr" -> doc.description_fr ?: doc.description
            "en" -> doc.description_en ?: doc.description_fr ?: doc.description
            "es" -> doc.description_es ?: doc.description_fr ?: doc.description
            else -> doc.description_fr ?: doc.description
        }
        return desc?.takeIf { it.isNotBlank() } ?: ""
    }

    /**
     * Get localized category based on device locale
     * Fallback chain: locale -> FR -> default category
     */
    private fun getLocalizedCategory(doc: LessonDocument, languageCode: String): String? {
        return when (languageCode.lowercase()) {
            "fr" -> doc.category_fr?.takeIf { it.isNotBlank() }
            "en" -> doc.category_en?.takeIf { it.isNotBlank() } ?: doc.category_fr
            "es" -> doc.category_es?.takeIf { it.isNotBlank() } ?: doc.category_fr
            else -> doc.category_fr
        }
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
