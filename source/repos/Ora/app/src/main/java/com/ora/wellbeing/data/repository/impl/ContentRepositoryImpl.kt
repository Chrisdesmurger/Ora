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
import kotlinx.coroutines.flow.flow
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
            videoUrl = this@toContentItem.videoUrl
            audioUrl = this@toContentItem.audioUrl
            isPremiumOnly = false // Determined by program settings
            isPopular = false // TODO: Calculate from user activity
            isNew = isRecentFromTimestamp(this@toContentItem.updatedAt)
            rating = 0.0f // TODO: Calculate from ratings
            completionCount = 0 // TODO: From user stats
            tags = this@toContentItem.tags
            isActive = this@toContentItem.status == STATUS_READY
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
            instructorName = instructor,
            tags = tags,
            isFlashSession = durationMinutes <= 10, // Quick sessions
            equipment = emptyList(), // TODO: Extract from tags
            benefits = emptyList(), // TODO: Extract from description
            createdAt = timestampToLocalDateTime(createdAt),
            isOfflineAvailable = false, // Requires explicit download
            downloadSize = null,
            programId = null, // TODO: Extract from lesson data
            order = 0,
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

    override fun getAllContent(): Flow<List<ContentItem>> = flow {
        Timber.d("getAllContent: Fetching content (offline-first)")

        // 1. Emit cached data immediately
        val cachedContent = contentDao.getAllContentFlow()
        cachedContent.collect { contentList ->
            if (contentList.isNotEmpty()) {
                Timber.d("getAllContent: Emitting ${contentList.size} cached items")
                emit(contentList.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getAllContent: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            val freshContent = contentDao.getAllContentFlow()
            freshContent.collect { contentList ->
                Timber.d("getAllContent: Emitting ${contentList.size} fresh items after sync")
                emit(contentList.map { it.toContentItem() })
            }
        }
    }

    override fun getContentByCategory(category: String): Flow<List<ContentItem>> = flow {
        require(category.isNotBlank()) { "Category cannot be blank" }
        Timber.d("getContentByCategory: category=$category (offline-first)")

        // Map string category to Room Category enum
        val roomCategory = mapStringToCategory(category)

        // 1. Emit cached data immediately
        contentDao.getContentByCategoryFlow(roomCategory).collect { contentList ->
            if (contentList.isNotEmpty()) {
                Timber.d("getContentByCategory: Emitting ${contentList.size} cached items")
                emit(contentList.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getContentByCategory: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            contentDao.getContentByCategoryFlow(roomCategory).collect { contentList ->
                Timber.d("getContentByCategory: Emitting ${contentList.size} fresh items after sync")
                emit(contentList.map { it.toContentItem() })
            }
        }
    }

    override fun getContent(contentId: String): Flow<ContentItem?> = flow {
        require(contentId.isNotBlank()) { "Content ID cannot be blank" }
        Timber.d("getContent: contentId=$contentId (offline-first)")

        // 1. Emit cached data immediately
        contentDao.getContentByIdFlow(contentId).collect { content ->
            if (content != null) {
                Timber.d("getContent: Emitting cached content")
                emit(content.toContentItem())
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getContent: Cache stale, syncing from Firestore")
            syncLessonFromFirestore(contentId)

            // Emit fresh data after sync
            contentDao.getContentByIdFlow(contentId).collect { content ->
                if (content != null) {
                    Timber.d("getContent: Emitting fresh content after sync")
                    emit(content.toContentItem())
                }
            }
        }
    }

    override fun getPopularContent(limit: Int): Flow<List<ContentItem>> = flow {
        require(limit > 0) { "Limit must be > 0" }
        Timber.d("getPopularContent: limit=$limit (offline-first)")

        // Note: "isPopular" is computed from user activity, not stored in Room
        // For now, return all content and let UI/ViewModel filter by popularity
        // TODO: Add isPopular field to Content entity and update during sync

        // 1. Emit cached data immediately
        contentDao.getAllContentFlow().collect { contentList ->
            val limited = contentList.take(limit)
            if (limited.isNotEmpty()) {
                Timber.d("getPopularContent: Emitting ${limited.size} cached items")
                emit(limited.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getPopularContent: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            contentDao.getAllContentFlow().collect { contentList ->
                val limited = contentList.take(limit)
                Timber.d("getPopularContent: Emitting ${limited.size} fresh items after sync")
                emit(limited.map { it.toContentItem() })
            }
        }
    }

    override fun getNewContent(limit: Int): Flow<List<ContentItem>> = flow {
        require(limit > 0) { "Limit must be > 0" }
        Timber.d("getNewContent: limit=$limit (offline-first)")

        // 1. Emit cached data immediately (filter by recent updatedAt)
        contentDao.getAllContentFlow().collect { contentList ->
            val recentContent = contentList
                .filter { isRecentFromTimestamp(it.updatedAt) }
                .take(limit)

            if (recentContent.isNotEmpty()) {
                Timber.d("getNewContent: Emitting ${recentContent.size} cached items")
                emit(recentContent.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getNewContent: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            contentDao.getAllContentFlow().collect { contentList ->
                val recentContent = contentList
                    .filter { isRecentFromTimestamp(it.updatedAt) }
                    .take(limit)

                Timber.d("getNewContent: Emitting ${recentContent.size} fresh items after sync")
                emit(recentContent.map { it.toContentItem() })
            }
        }
    }

    override fun getContentByDuration(minMinutes: Int, maxMinutes: Int): Flow<List<ContentItem>> = flow {
        Timber.d("getContentByDuration: min=$minMinutes, max=$maxMinutes (offline-first)")

        // 1. Emit cached data immediately
        contentDao.getContentByMaxDurationFlow(maxMinutes).collect { contentList ->
            val filtered = contentList.filter { it.durationMinutes >= minMinutes }

            if (filtered.isNotEmpty()) {
                Timber.d("getContentByDuration: Emitting ${filtered.size} cached items")
                emit(filtered.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getContentByDuration: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            contentDao.getContentByMaxDurationFlow(maxMinutes).collect { contentList ->
                val filtered = contentList.filter { it.durationMinutes >= minMinutes }

                Timber.d("getContentByDuration: Emitting ${filtered.size} fresh items after sync")
                emit(filtered.map { it.toContentItem() })
            }
        }
    }

    override fun searchContent(query: String): Flow<List<ContentItem>> = flow {
        Timber.d("searchContent: query='$query' (offline-first, client-side)")

        if (query.isBlank()) {
            // Empty query returns all content
            getAllContent().collect { emit(it) }
        } else {
            // 1. Search in cache immediately
            contentDao.searchContentFlow(query).collect { contentList ->
                if (contentList.isNotEmpty()) {
                    Timber.d("searchContent: Found ${contentList.size} cached matches")
                    emit(contentList.map { it.toContentItem() })
                }
            }

            // 2. Sync from Firestore if needed, then search again
            if (shouldSync()) {
                Timber.d("searchContent: Cache stale, syncing from Firestore")
                syncAllLessonsFromFirestore()

                // Search in fresh data
                contentDao.searchContentFlow(query).collect { contentList ->
                    Timber.d("searchContent: Found ${contentList.size} fresh matches after sync")
                    emit(contentList.map { it.toContentItem() })
                }
            }
        }
    }

    override fun getContentByInstructor(instructor: String): Flow<List<ContentItem>> = flow {
        require(instructor.isNotBlank()) { "Instructor cannot be blank" }
        Timber.d("getContentByInstructor: instructor='$instructor' (offline-first)")

        // Room doesn't have direct instructor query, so filter client-side

        // 1. Emit cached data immediately
        contentDao.getAllContentFlow().collect { contentList ->
            val filtered = contentList.filter {
                it.instructorName?.equals(instructor, ignoreCase = true) == true
            }

            if (filtered.isNotEmpty()) {
                Timber.d("getContentByInstructor: Emitting ${filtered.size} cached items")
                emit(filtered.map { it.toContentItem() })
            }
        }

        // 2. Sync from Firestore if needed
        if (shouldSync()) {
            Timber.d("getContentByInstructor: Cache stale, syncing from Firestore")
            syncAllLessonsFromFirestore()

            // Emit fresh data after sync
            contentDao.getAllContentFlow().collect { contentList ->
                val filtered = contentList.filter {
                    it.instructorName?.equals(instructor, ignoreCase = true) == true
                }

                Timber.d("getContentByInstructor: Emitting ${filtered.size} fresh items after sync")
                emit(filtered.map { it.toContentItem() })
            }
        }
    }

    override suspend fun getTotalContentCount(): Int {
        return try {
            Timber.d("getTotalContentCount: Fetching from cache")

            // Get from cache
            val cachedCount = contentDao.getContentById("count")?.let { 1 } ?: 0

            // If cache is stale, sync and recount
            if (shouldSync()) {
                Timber.d("getTotalContentCount: Cache stale, syncing from Firestore")
                syncAllLessonsFromFirestore()
            }

            // Count from cache (post-sync if needed)
            val snapshot = firestore
                .collection(COLLECTION_LESSONS)
                .whereEqualTo("status", STATUS_READY)
                .get()
                .await()

            Timber.d("getTotalContentCount: ${snapshot.size()} total lessons")
            snapshot.size()
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

            // Get unique instructors from Firestore
            val snapshot = firestore
                .collection(COLLECTION_LESSONS)
                .whereEqualTo("status", STATUS_READY)
                .get()
                .await()

            val instructors = snapshot.documents
                .mapNotNull { doc ->
                    val lessonDoc = doc.toObject(LessonDocument::class.java)
                    lessonDoc?.tags?.find { it.startsWith("instructor:", ignoreCase = true) }
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
