package com.ora.wellbeing.data.repository.impl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.local.dao.ContentDao
import com.ora.wellbeing.data.local.entities.Category
import com.ora.wellbeing.data.local.entities.Content
import com.ora.wellbeing.data.local.entities.ContentType
import com.ora.wellbeing.data.local.entities.ExperienceLevel
import com.ora.wellbeing.data.mapper.LessonMapper
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.firestore.LessonDocument
import com.ora.wellbeing.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentDao: ContentDao
) : ContentRepository {

    companion object {
        private const val COLLECTION_LESSONS = "lessons"
        private const val SYNC_INTERVAL_HOURS = 1L
        private const val STATUS_READY = "ready"
    }

    // Track last sync time for smart caching
    private var lastSyncTime: Long = 0L

    /**
     * Checks if sync is needed based on time interval
     */
    private fun shouldSync(): Boolean {
        val now = System.currentTimeMillis()
        val hoursSinceSync = (now - lastSyncTime) / (1000 * 60 * 60)
        return hoursSinceSync >= SYNC_INTERVAL_HOURS
    }

    /**
     * Updates the last sync timestamp
     */
    private fun markSynced() {
        lastSyncTime = System.currentTimeMillis()
    }

    // ============================================================================
    // Mapper Functions: Content (Room) <-> ContentItem (Domain)
    // ============================================================================

    /**
     * Converts Room Content entity to domain ContentItem
     */
    private fun Content.toContentItem(): ContentItem {
        return ContentItem().apply {
            id = this@toContentItem.id
            title = this@toContentItem.title
            description = this@toContentItem.description
            category = mapCategoryToString(this@toContentItem.category)
            durationMinutes = this@toContentItem.durationMinutes
            duration = formatDuration(this@toContentItem.durationMinutes)
            instructor = this@toContentItem.instructorName ?: ""
            thumbnailUrl = this@toContentItem.thumbnailUrl
            previewImageUrl = this@toContentItem.previewImageUrl
            videoUrl = this@toContentItem.videoUrl
            audioUrl = this@toContentItem.audioUrl
            isPremiumOnly = false // Determined by program settings
            isPopular = false // TODO: Calculate from user activity
            isNew = isRecentFromTimestamp(this@toContentItem.updatedAt)
            rating = 0.0f // TODO: Calculate from ratings
            completionCount = 0 // TODO: From user stats
            tags = this@toContentItem.tags
            isActive = this@toContentItem.status == STATUS_READY
            order = this@toContentItem.order  // Order for sorting (lower = higher priority)
            // FIX(build-debug-android): Use correct Timestamp constructor with seconds (Long) and nanoseconds (Int)
            createdAt = Timestamp(this@toContentItem.createdAt.toEpochSecond(ZoneOffset.UTC), 0)
            updatedAt = Timestamp(this@toContentItem.updatedAt / 1000, 0)
            publishedAt = null // Not tracked in Room
        }
    }

    /**
     * Converts domain ContentItem to Room Content entity
     */
    private fun ContentItem.toContent(): Content {
        return Content(
            id = id,
            title = title,
            description = description,
            type = mapStringToContentType(category),
            category = mapStringToCategory(category),
            durationMinutes = durationMinutes,
            level = ExperienceLevel.BEGINNER, // Default, TODO: extract from tags
            videoUrl = videoUrl,
            audioUrl = audioUrl,
            thumbnailUrl = thumbnailUrl,
            previewImageUrl = previewImageUrl,
            instructorName = instructor,
            tags = tags,
            isFlashSession = durationMinutes <= 10, // Quick sessions
            equipment = emptyList(), // TODO: Extract from tags
            benefits = emptyList(), // TODO: Extract from description
            createdAt = timestampToLocalDateTime(createdAt),
            isOfflineAvailable = false, // Requires explicit download
            downloadSize = null,
            programId = null, // TODO: Extract from lesson data
            order = order,  // Preserve sort order
            status = if (isActive) STATUS_READY else "draft",
            updatedAt = System.currentTimeMillis()
        )
    }

    // ============================================================================
    // Helper Mapping Functions
    // ============================================================================

    private fun mapCategoryToString(category: Category): String {
        return when (category) {
            Category.MORNING_ROUTINE -> "Méditation"
            Category.DAY_BOOST -> "Bien-être"
            Category.EVENING_WIND_DOWN -> "Sommeil"
            Category.STRESS_RELIEF -> "Respiration"
            Category.FLEXIBILITY -> "Yoga"
            Category.STRENGTH -> "Pilates"
            Category.MINDFULNESS -> "Méditation"
            Category.RELAXATION -> "Sommeil"
            Category.ENERGY_BOOST -> "Bien-être"
        }
    }

    private fun mapStringToCategory(categoryStr: String): Category {
        return when (categoryStr.lowercase()) {
            "méditation", "meditation" -> Category.MINDFULNESS
            "yoga" -> Category.FLEXIBILITY
            "respiration", "breathing" -> Category.STRESS_RELIEF
            "pilates" -> Category.STRENGTH
            "sommeil", "sleep" -> Category.RELAXATION
            "bien-être", "wellness" -> Category.DAY_BOOST
            else -> Category.DAY_BOOST
        }
    }

    private fun mapStringToContentType(categoryStr: String): ContentType {
        return when (categoryStr.lowercase()) {
            "méditation", "meditation" -> ContentType.MEDITATION
            "yoga" -> ContentType.YOGA
            "respiration", "breathing" -> ContentType.BREATHING
            "pilates" -> ContentType.PILATES
            else -> ContentType.MEDITATION
        }
    }

    private fun formatDuration(durationMinutes: Int): String {
        return if (durationMinutes >= 60) {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            if (minutes > 0) "${hours}h ${minutes} min" else "${hours}h"
        } else {
            "$durationMinutes min"
        }
    }

    private fun timestampToLocalDateTime(timestamp: Timestamp?): LocalDateTime {
        return timestamp?.toDate()?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDateTime()
            ?: LocalDateTime.now()
    }

    private fun isRecentFromTimestamp(timestampMillis: Long): Boolean {
        val now = System.currentTimeMillis()
        val daysSince = (now - timestampMillis) / (1000 * 60 * 60 * 24)
        return daysSince <= 7
    }

    // ============================================================================
    // Firestore Sync Operations
    // ============================================================================

    /**
     * Syncs all ready lessons from Firestore to Room cache
     */
    private suspend fun syncAllLessonsFromFirestore() {
        try {
            Timber.d("syncAllLessonsFromFirestore: Starting sync from 'lessons' collection")

            val snapshot = firestore
                .collection(COLLECTION_LESSONS)
                .whereEqualTo("status", STATUS_READY)
                .get()
                .await()

            val lessons = snapshot.documents.mapNotNull { doc ->
                try {
                    val lessonDoc = doc.toObject(LessonDocument::class.java)
                    if (lessonDoc != null) {
                        val contentItem = LessonMapper.fromFirestore(doc.id, lessonDoc)
                        contentItem.toContent()
                    } else {
                        Timber.w("syncAllLessonsFromFirestore: Failed to parse lesson ${doc.id}")
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "syncAllLessonsFromFirestore: Error parsing lesson ${doc.id}")
                    null
                }
            }

            if (lessons.isNotEmpty()) {
                contentDao.insertAllContent(lessons)
                Timber.d("syncAllLessonsFromFirestore: Synced ${lessons.size} lessons to cache")
            }

            markSynced()
        } catch (e: Exception) {
            Timber.e(e, "syncAllLessonsFromFirestore: Sync failed")
            // Don't throw - continue with cached data
        }
    }

    /**
     * Syncs a single lesson from Firestore by ID
     */
    private suspend fun syncLessonFromFirestore(lessonId: String) {
        try {
            Timber.d("syncLessonFromFirestore: Syncing lesson $lessonId")

            val doc = firestore
                .collection(COLLECTION_LESSONS)
                .document(lessonId)
                .get()
                .await()

            if (doc.exists()) {
                val lessonDoc = doc.toObject(LessonDocument::class.java)
                if (lessonDoc != null && lessonDoc.status == STATUS_READY) {
                    val contentItem = LessonMapper.fromFirestore(doc.id, lessonDoc)
                    contentDao.insertContent(contentItem.toContent())
                    Timber.d("syncLessonFromFirestore: Synced lesson $lessonId to cache")
                }
            } else {
                Timber.w("syncLessonFromFirestore: Lesson $lessonId not found")
            }
        } catch (e: Exception) {
            Timber.e(e, "syncLessonFromFirestore: Failed to sync lesson $lessonId")
            // Don't throw - continue with cached data
        }
    }

    // ============================================================================
    // Repository Interface Implementation (Offline-First)
    // ============================================================================

    // FIX(build-debug-android): Remove .collect{} inside flow{} to prevent infinite loops
    // Use .map{} + .onStart{} pattern like ProgramRepositoryImpl

    override fun getAllContent(): Flow<List<ContentItem>> {
        Timber.d("getAllContent: Returning offline-first Flow")

        return contentDao.getAllContentFlow()
            .map { contentList ->
                contentList.map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getAllContent: Triggering background sync")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override fun getContentByCategory(category: String): Flow<List<ContentItem>> {
        require(category.isNotBlank()) { "Category cannot be blank" }
        Timber.d("getContentByCategory: Returning offline-first Flow for category=$category")

        // Map string category to Room Category enum
        val roomCategory = mapStringToCategory(category)

        return contentDao.getContentByCategoryFlow(roomCategory)
            .map { contentList ->
                contentList.map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getContentByCategory: Triggering background sync for category=$category")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override fun getContent(contentId: String): Flow<ContentItem?> {
        require(contentId.isNotBlank()) { "Content ID cannot be blank" }
        Timber.d("getContent: Returning offline-first Flow for id=$contentId")

        return contentDao.getContentByIdFlow(contentId)
            .map { content ->
                content?.toContentItem()
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getContent: Triggering background sync for id=$contentId")
                    syncLessonFromFirestore(contentId)
                }
            }
    }

    override fun getPopularContent(limit: Int): Flow<List<ContentItem>> {
        require(limit > 0) { "Limit must be > 0" }
        Timber.d("getPopularContent: Returning offline-first Flow (limit=$limit)")

        // Note: "isPopular" is computed from user activity, not stored in Room
        // For now, return all content and let UI/ViewModel filter by popularity
        // TODO: Add isPopular field to Content entity and update during sync

        return contentDao.getAllContentFlow()
            .map { contentList ->
                contentList.take(limit).map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getPopularContent: Triggering background sync")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override fun getNewContent(limit: Int): Flow<List<ContentItem>> {
        require(limit > 0) { "Limit must be > 0" }
        Timber.d("getNewContent: Returning offline-first Flow (limit=$limit)")

        return contentDao.getAllContentFlow()
            .map { contentList ->
                contentList
                    .filter { isRecentFromTimestamp(it.updatedAt) }
                    .take(limit)
                    .map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getNewContent: Triggering background sync")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override fun getContentByDuration(minMinutes: Int, maxMinutes: Int): Flow<List<ContentItem>> {
        Timber.d("getContentByDuration: Returning offline-first Flow (min=$minMinutes, max=$maxMinutes)")

        return contentDao.getContentByMaxDurationFlow(maxMinutes)
            .map { contentList ->
                contentList
                    .filter { it.durationMinutes >= minMinutes }
                    .map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getContentByDuration: Triggering background sync")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override fun searchContent(query: String): Flow<List<ContentItem>> {
        Timber.d("searchContent: Returning offline-first Flow for query='$query'")

        return if (query.isBlank()) {
            // Empty query returns all content
            getAllContent()
        } else {
            contentDao.searchContentFlow(query)
                .map { contentList ->
                    contentList.map { it.toContentItem() }
                }
                .onStart {
                    // Trigger sync in background if needed
                    if (shouldSync()) {
                        Timber.d("searchContent: Triggering background sync for query='$query'")
                        syncAllLessonsFromFirestore()
                    }
                }
        }
    }

    override fun getContentByInstructor(instructor: String): Flow<List<ContentItem>> {
        require(instructor.isNotBlank()) { "Instructor cannot be blank" }
        Timber.d("getContentByInstructor: Returning offline-first Flow for instructor='$instructor'")

        // Room doesn't have direct instructor query, so filter client-side
        return contentDao.getAllContentFlow()
            .map { contentList ->
                contentList
                    .filter { it.instructorName?.equals(instructor, ignoreCase = true) == true }
                    .map { it.toContentItem() }
            }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSync()) {
                    Timber.d("getContentByInstructor: Triggering background sync for instructor='$instructor'")
                    syncAllLessonsFromFirestore()
                }
            }
    }

    override suspend fun getTotalContentCount(): Int {
        return try {
            Timber.d("getTotalContentCount: Fetching from cache")

            // Sync if needed
            if (shouldSync()) {
                Timber.d("getTotalContentCount: Cache stale, syncing from Firestore")
                syncAllLessonsFromFirestore()
            }

            // Count from cache using .first() to get single value
            val count = contentDao.getAllContentFlow().first().size
            Timber.d("getTotalContentCount: $count total lessons")
            count
        } catch (e: Exception) {
            Timber.e(e, "getTotalContentCount: Error, returning 0")
            0
        }
    }

    override suspend fun getAllInstructors(): List<String> {
        return try {
            Timber.d("getAllInstructors: Fetching from cache")

            // Sync if needed
            if (shouldSync()) {
                Timber.d("getAllInstructors: Cache stale, syncing from Firestore")
                syncAllLessonsFromFirestore()
            }

            // Get unique instructors from cache using .first()
            val instructors = contentDao.getAllContentFlow()
                .first()
                .mapNotNull { content ->
                    content.tags.find { it.startsWith("instructor:", ignoreCase = true) }
                        ?.substringAfter("instructor:")
                }
                .distinct()
                .sorted()

            Timber.d("getAllInstructors: Found ${instructors.size} unique instructors")
            instructors
        } catch (e: Exception) {
            Timber.e(e, "getAllInstructors: Error, returning empty list")
            emptyList()
        }
    }
}
