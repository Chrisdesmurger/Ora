package com.ora.wellbeing.data.cache

import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.data.repository.SubcategoryRepository
import com.ora.wellbeing.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ContentCacheManager
 *
 * Manages preloading and caching of content and subcategories for faster navigation.
 * When user views the categories screen, data for all categories is preloaded
 * so that navigating to a category detail screen is instant.
 *
 * Lifecycle: Singleton, data is preloaded once and cached until app restart.
 */
@Singleton
class ContentCacheManager @Inject constructor(
    private val contentRepository: ContentRepository,
    private val subcategoryRepository: SubcategoryRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Main categories to preload
    private val categoriesToPreload = listOf("Meditation", "Yoga", "Pilates", "Bien-etre")

    // Cache states
    private val _isPreloading = MutableStateFlow(false)
    val isPreloading: StateFlow<Boolean> = _isPreloading.asStateFlow()

    private val _isPreloaded = MutableStateFlow(false)
    val isPreloaded: StateFlow<Boolean> = _isPreloaded.asStateFlow()

    // Cached data maps
    private val subcategoriesCache = mutableMapOf<String, List<SubcategoryItem>>()
    private val contentCache = mutableMapOf<String, List<ContentItem>>()
    private val groupedContentCache = mutableMapOf<String, List<SubcategorySection>>()

    /**
     * Preload all category data (subcategories + content)
     * Called when user enters the categories screen
     */
    fun preloadAllCategories() {
        if (_isPreloading.value || _isPreloaded.value) {
            Timber.d("Already preloading or preloaded, skipping")
            return
        }

        _isPreloading.value = true
        Timber.d("Starting preload for ${categoriesToPreload.size} categories")

        scope.launch {
            try {
                // Preload each category in parallel
                categoriesToPreload.forEach { categoryId ->
                    launch {
                        preloadCategory(categoryId)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during preload")
            } finally {
                _isPreloading.value = false
                _isPreloaded.value = true
                Timber.d("Preload complete")
            }
        }
    }

    /**
     * Preload data for a single category
     */
    private suspend fun preloadCategory(categoryId: String) {
        try {
            // Load subcategories
            val subcategories = try {
                subcategoryRepository.getSubcategoriesForCategory(categoryId)
                    .catch { e ->
                        Timber.e(e, "Error loading subcategories for $categoryId")
                        emit(emptyList())
                    }
                    .first()
                    .ifEmpty {
                        subcategoryRepository.getDefaultSubcategories(categoryId)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error getting subcategories for $categoryId")
                subcategoryRepository.getDefaultSubcategories(categoryId)
            }

            subcategoriesCache[categoryId] = subcategories
            Timber.d("Cached ${subcategories.size} subcategories for $categoryId")

            // Load content
            val content = try {
                contentRepository.getContentByCategory(categoryId)
                    .catch { e ->
                        Timber.e(e, "Error loading content for $categoryId")
                        emit(emptyList())
                    }
                    .first()
            } catch (e: Exception) {
                Timber.e(e, "Error getting content for $categoryId")
                emptyList()
            }

            contentCache[categoryId] = content
            Timber.d("Cached ${content.size} content items for $categoryId")

            // Pre-compute grouped content
            val grouped = groupContentBySubcategory(categoryId, content, subcategories)
            groupedContentCache[categoryId] = grouped
            Timber.d("Cached ${grouped.size} sections for $categoryId")

        } catch (e: Exception) {
            Timber.e(e, "Error preloading category $categoryId")
        }
    }

    /**
     * Group content by subcategory (same logic as ViewModel)
     */
    private fun groupContentBySubcategory(
        categoryId: String,
        allContent: List<ContentItem>,
        subcategories: List<SubcategoryItem>
    ): List<SubcategorySection> {
        if (allContent.isEmpty()) return emptyList()

        val sections = mutableListOf<SubcategorySection>()
        val assignedContentIds = mutableSetOf<String>()

        // Group content by each subcategory's filterTags
        for (subcat in subcategories) {
            val matchingContent = allContent.filter { content ->
                content.tags.any { tag ->
                    subcat.filterTags.any { filterTag ->
                        tag.equals(filterTag, ignoreCase = true)
                    }
                }
            }

            if (matchingContent.isNotEmpty()) {
                sections.add(SubcategorySection(subcat, matchingContent))
                assignedContentIds.addAll(matchingContent.map { it.id })
            }
        }

        // Add "Autres/Others/Otros" section for unassigned content (localized)
        val unassignedContent = allContent.filter { it.id !in assignedContentIds }
        if (unassignedContent.isNotEmpty()) {
            val othersSectionName = getLocalizedOthersName()
            sections.add(
                SubcategorySection(
                    subcategory = SubcategoryItem(
                        id = "autres",
                        parentCategory = categoryId,
                        name = othersSectionName
                    ),
                    content = unassignedContent
                )
            )
        }

        return sections
    }

    /**
     * Get localized name for "Others" section based on device locale
     */
    private fun getLocalizedOthersName(): String {
        return when (Locale.getDefault().language) {
            "fr" -> "Autres"
            "en" -> "Others"
            "es" -> "Otros"
            else -> "Autres"
        }
    }

    // ========================================================================
    // Cache Access Methods
    // ========================================================================

    /**
     * Get cached subcategories for a category
     * @return cached list or null if not cached
     */
    fun getCachedSubcategories(categoryId: String): List<SubcategoryItem>? {
        return subcategoriesCache[categoryId]
    }

    /**
     * Get cached content for a category
     * @return cached list or null if not cached
     */
    fun getCachedContent(categoryId: String): List<ContentItem>? {
        return contentCache[categoryId]
    }

    /**
     * Get cached grouped content for a category
     * @return cached sections or null if not cached
     */
    fun getCachedGroupedContent(categoryId: String): List<SubcategorySection>? {
        return groupedContentCache[categoryId]
    }

    /**
     * Check if data is cached for a specific category
     */
    fun isCached(categoryId: String): Boolean {
        return groupedContentCache.containsKey(categoryId)
    }

    /**
     * Clear all cached data
     */
    fun clearCache() {
        subcategoriesCache.clear()
        contentCache.clear()
        groupedContentCache.clear()
        _isPreloaded.value = false
        Timber.d("Cache cleared")
    }

    /**
     * Update cache for a specific category (after data refresh)
     */
    fun updateCache(
        categoryId: String,
        subcategories: List<SubcategoryItem>,
        content: List<ContentItem>
    ) {
        subcategoriesCache[categoryId] = subcategories
        contentCache[categoryId] = content
        groupedContentCache[categoryId] = groupContentBySubcategory(categoryId, content, subcategories)
        Timber.d("Updated cache for $categoryId")
    }
}

/**
 * Represents a section with subcategory header and content
 * Shared between cache and ViewModel
 */
data class SubcategorySection(
    val subcategory: SubcategoryItem,
    val content: List<ContentItem>
)
