package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.domain.model.DailyNeedCategory
import kotlinx.coroutines.flow.Flow

/**
 * DailyNeedCategoryRepository - Repository interface for daily need categories
 *
 * This repository manages the "Ton besoin du jour" categories displayed on HomeScreen.
 * It fetches categories from Firestore and provides content filtering capabilities.
 *
 * Data Flow:
 * 1. Firestore: daily_needs_categories collection
 * 2. Mapper: DailyNeedCategoryMapper converts to domain model
 * 3. Repository: Provides Flow-based access to categories and filtered content
 *
 * @see com.ora.wellbeing.data.repository.impl.DailyNeedCategoryRepositoryImpl
 */
interface DailyNeedCategoryRepository {

    /**
     * Gets all active daily need categories
     *
     * Categories are sorted by order field (ascending).
     * Only categories with is_active=true are returned.
     *
     * @return Flow emitting list of active DailyNeedCategory objects
     */
    fun getAllCategories(): Flow<List<DailyNeedCategory>>

    /**
     * Gets a specific category by ID
     *
     * @param categoryId The category ID (e.g., "anti-stress")
     * @return Flow emitting the DailyNeedCategory or null if not found
     */
    fun getCategoryById(categoryId: String): Flow<DailyNeedCategory?>

    /**
     * Gets content items that match a category's criteria
     *
     * Filtering logic:
     * 1. If category has lesson_ids, return those specific lessons
     * 2. Otherwise, filter lessons by matching need_tags with filter_tags
     *
     * @param categoryId The category ID to filter content for
     * @param limit Maximum number of content items to return (default: 10)
     * @return Flow emitting list of matching ContentItem objects
     */
    fun getContentForCategory(categoryId: String, limit: Int = 10): Flow<List<ContentItem>>

    /**
     * Gets content items for a category using pre-loaded category data
     *
     * More efficient when category is already loaded (avoids extra Firestore fetch)
     *
     * @param category The DailyNeedCategory to filter content for
     * @param limit Maximum number of content items to return (default: 10)
     * @return Flow emitting list of matching ContentItem objects
     */
    fun getContentForCategory(category: DailyNeedCategory, limit: Int = 10): Flow<List<ContentItem>>

    /**
     * Gets the count of available content for each category
     *
     * Useful for displaying "X pratiques" on category cards
     *
     * @param categories List of categories to count content for
     * @return Map of categoryId to content count
     */
    suspend fun getContentCountsForCategories(categories: List<DailyNeedCategory>): Map<String, Int>
}
