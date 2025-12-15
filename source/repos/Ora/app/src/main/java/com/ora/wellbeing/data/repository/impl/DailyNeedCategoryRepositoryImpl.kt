package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.mapper.DailyNeedCategoryMapper
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.firestore.DailyNeedCategoryDocument
import com.ora.wellbeing.domain.model.DailyNeedCategory
import com.ora.wellbeing.domain.repository.ContentRepository
import com.ora.wellbeing.domain.repository.DailyNeedCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DailyNeedCategoryRepositoryImpl - Implementation of DailyNeedCategoryRepository
 *
 * Fetches daily need categories from Firestore and provides content filtering.
 * Uses DailyNeedCategoryMapper for Firestore -> Domain conversion.
 *
 * Firestore Collection: daily_needs_categories/{categoryId}
 *
 * @property firestore Firebase Firestore instance
 * @property contentRepository ContentRepository for fetching lessons
 */
@Singleton
class DailyNeedCategoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentRepository: ContentRepository
) : DailyNeedCategoryRepository {

    companion object {
        private const val TAG = "DailyNeedCategoryRepo"
        private const val COLLECTION_NAME = "daily_needs_categories"
    }

    /**
     * Gets all active daily need categories from Firestore
     */
    override fun getAllCategories(): Flow<List<DailyNeedCategory>> = flow {
        try {
            Timber.d("$TAG: Fetching all active categories from Firestore")

            val snapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("is_active", true)
                .orderBy("order")
                .get()
                .await()

            val categories = snapshot.documents.mapNotNull { doc ->
                try {
                    val document = doc.toObject(DailyNeedCategoryDocument::class.java)
                    if (document != null) {
                        DailyNeedCategoryMapper.fromFirestore(doc.id, document)
                    } else {
                        Timber.w("$TAG: Failed to parse document ${doc.id}")
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "$TAG: Error parsing category ${doc.id}")
                    null
                }
            }

            Timber.d("$TAG: Fetched ${categories.size} categories")

            // If no categories found in Firestore, return defaults
            if (categories.isEmpty()) {
                Timber.w("$TAG: No categories in Firestore, using defaults")
                emit(DailyNeedCategoryMapper.createDefaultCategories())
            } else {
                emit(categories)
            }

        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error fetching categories from Firestore")
            // Return default categories as fallback
            emit(DailyNeedCategoryMapper.createDefaultCategories())
        }
    }

    /**
     * Gets a specific category by ID
     */
    override fun getCategoryById(categoryId: String): Flow<DailyNeedCategory?> = flow {
        try {
            Timber.d("$TAG: Fetching category by ID: $categoryId")

            val doc = firestore.collection(COLLECTION_NAME)
                .document(categoryId)
                .get()
                .await()

            if (doc.exists()) {
                val document = doc.toObject(DailyNeedCategoryDocument::class.java)
                if (document != null) {
                    emit(DailyNeedCategoryMapper.fromFirestore(doc.id, document))
                } else {
                    Timber.w("$TAG: Failed to parse category $categoryId")
                    emit(null)
                }
            } else {
                Timber.w("$TAG: Category not found: $categoryId")
                // Try to find in defaults
                val default = DailyNeedCategoryMapper.createDefaultCategories()
                    .find { it.id == categoryId }
                emit(default)
            }

        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error fetching category $categoryId")
            emit(null)
        }
    }

    /**
     * Gets content items that match a category's criteria (by categoryId)
     */
    override fun getContentForCategory(categoryId: String, limit: Int): Flow<List<ContentItem>> = flow {
        try {
            Timber.d("$TAG: Getting content for category: $categoryId (limit=$limit)")

            // First, get the category
            val category = getCategoryById(categoryId).first()

            if (category == null) {
                Timber.w("$TAG: Category not found: $categoryId")
                emit(emptyList())
                return@flow
            }

            // Get content using the category
            getContentForCategory(category, limit).collect { content ->
                emit(content)
            }

        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error getting content for category $categoryId")
            emit(emptyList())
        }
    }

    /**
     * Gets content items that match a category's criteria (using pre-loaded category)
     */
    override fun getContentForCategory(category: DailyNeedCategory, limit: Int): Flow<List<ContentItem>> = flow {
        try {
            Timber.d("$TAG: Getting content for category: ${category.id} (limit=$limit)")

            // Get all content from repository
            val allContent = contentRepository.getAllContent().first()

            Timber.d("$TAG: Total content items available: ${allContent.size}")

            // Filter content based on category criteria
            val filteredContent = filterContentForCategory(allContent, category, limit)

            Timber.d("$TAG: Filtered content for ${category.id}: ${filteredContent.size} items")

            emit(filteredContent)

        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error getting content for category ${category.id}")
            emit(emptyList())
        }
    }

    /**
     * Gets the count of available content for each category
     */
    override suspend fun getContentCountsForCategories(categories: List<DailyNeedCategory>): Map<String, Int> {
        return try {
            Timber.d("$TAG: Getting content counts for ${categories.size} categories")

            // Get all content once
            val allContent = contentRepository.getAllContent().first()

            // Calculate counts for each category
            val counts = categories.associate { category ->
                val count = filterContentForCategory(allContent, category, Int.MAX_VALUE).size
                category.id to count
            }

            Timber.d("$TAG: Content counts: $counts")
            counts

        } catch (e: Exception) {
            Timber.e(e, "$TAG: Error getting content counts")
            categories.associate { it.id to 0 }
        }
    }

    /**
     * Filters content items based on category criteria
     *
     * Filtering logic:
     * 1. If category has explicit lesson_ids, filter by those IDs
     * 2. Otherwise, filter by matching tags (content.tags intersects category.filterTags)
     *
     * @param allContent All available content items
     * @param category The category to filter for
     * @param limit Maximum number of items to return
     * @return Filtered and limited content list
     */
    private fun filterContentForCategory(
        allContent: List<ContentItem>,
        category: DailyNeedCategory,
        limit: Int
    ): List<ContentItem> {
        // Priority 1: If category has explicit lesson_ids, use them
        if (category.lessonIds.isNotEmpty()) {
            Timber.d("$TAG: Filtering by explicit lesson_ids: ${category.lessonIds}")
            return allContent
                .filter { it.id in category.lessonIds }
                .take(limit)
        }

        // Priority 2: Filter by matching tags
        Timber.d("$TAG: Filtering by tags: ${category.filterTags}")

        return allContent
            .filter { content ->
                // Check if any of the content's tags match the category's filter tags
                content.tags.any { contentTag ->
                    category.filterTags.any { filterTag ->
                        contentTag.equals(filterTag, ignoreCase = true)
                    }
                }
            }
            .sortedBy { it.order } // Sort by order (featured content first)
            .take(limit)
    }
}
