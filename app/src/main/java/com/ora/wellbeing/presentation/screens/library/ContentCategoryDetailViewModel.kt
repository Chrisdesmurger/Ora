package com.ora.wellbeing.presentation.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.data.repository.SubcategoryRepository
import com.ora.wellbeing.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ContentCategoryDetailViewModel
 *
 * Manages the state for a single category detail screen
 * Handles:
 * - Loading content for a specific category
 * - Loading subcategories from Firestore (managed via OraWebApp)
 * - Filtering by subcategory
 *
 * Subcategories are displayed as horizontal scrolling cards
 * and are managed from the OraWebApp admin portal.
 */
@HiltViewModel
class ContentCategoryDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val subcategoryRepository: SubcategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""

    // Selected subcategory for filtering
    private val _selectedSubcategory = MutableStateFlow<SubcategoryItem?>(null)
    val selectedSubcategory: StateFlow<SubcategoryItem?> = _selectedSubcategory.asStateFlow()

    // Subcategories from Firestore (managed via OraWebApp)
    private val _subcategories = MutableStateFlow<List<SubcategoryItem>>(emptyList())
    val subcategories: StateFlow<List<SubcategoryItem>> = _subcategories.asStateFlow()

    // Legacy: tag-based subcategories (fallback)
    private val _availableSubcategories = MutableStateFlow<List<String>>(emptyList())
    val availableSubcategories: StateFlow<List<String>> = _availableSubcategories.asStateFlow()

    private val _uiState = MutableStateFlow(ContentCategoryDetailUiState())
    val uiState: StateFlow<ContentCategoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubcategories()
        loadCategoryContent()
    }

    /**
     * Load subcategories from Firestore
     * Falls back to default subcategories if none found
     */
    private fun loadSubcategories() {
        viewModelScope.launch {
            subcategoryRepository.getSubcategoriesForCategory(categoryId)
                .catch { e ->
                    Timber.e(e, "Error loading subcategories for $categoryId")
                }
                .collect { loadedSubcategories ->
                    if (loadedSubcategories.isNotEmpty()) {
                        _subcategories.value = loadedSubcategories
                        Timber.d("Loaded ${loadedSubcategories.size} subcategories from Firestore")
                    } else {
                        // Use default subcategories as fallback
                        val defaults = subcategoryRepository.getDefaultSubcategories(categoryId)
                        _subcategories.value = defaults
                        Timber.d("Using ${defaults.size} default subcategories")
                    }
                }
        }
    }

    private fun loadCategoryContent() {
        viewModelScope.launch {
            contentRepository.getContentByCategory(categoryId)
                .catch { exception ->
                    Timber.e(exception, "Error loading category content: $categoryId")
                    _uiState.value = ContentCategoryDetailUiState(
                        error = exception.message ?: "Erreur de chargement"
                    )
                }
                .combine(_selectedSubcategory) { allContent, selectedSub ->
                    // Extract unique tags from all content (for legacy chip display)
                    val uniqueTags = allContent
                        .flatMap { it.tags }
                        .distinct()
                        .sorted()
                    _availableSubcategories.value = uniqueTags

                    // Filter content based on selected subcategory
                    val filteredContent = if (selectedSub != null) {
                        // Filter by subcategory's filter_tags
                        allContent.filter { content ->
                            content.tags.any { tag ->
                                selectedSub.filterTags.any { filterTag ->
                                    tag.equals(filterTag, ignoreCase = true)
                                }
                            }
                        }
                    } else {
                        allContent
                    }

                    ContentCategoryDetailUiState(
                        categoryName = categoryId,
                        allContent = filteredContent,
                        totalContentCount = allContent.size,
                        isLoading = false
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Select a subcategory card to filter content
     */
    fun onSubcategorySelect(subcategory: SubcategoryItem?) {
        _selectedSubcategory.value = if (subcategory == _selectedSubcategory.value) {
            // Toggle off if same subcategory clicked
            null
        } else {
            subcategory
        }
        Timber.d("Selected subcategory: ${subcategory?.name ?: "All"}")
    }

    /**
     * Legacy: click on tag-based subcategory (text chip)
     */
    fun onSubcategoryClick(subcategory: String?) {
        viewModelScope.launch {
            // Find matching SubcategoryItem by name or create temporary one
            val matchingSubcategory = _subcategories.value.find {
                it.name.equals(subcategory, ignoreCase = true) ||
                it.filterTags.any { tag -> tag.equals(subcategory, ignoreCase = true) }
            }

            if (matchingSubcategory != null) {
                onSubcategorySelect(matchingSubcategory)
            } else if (subcategory != null) {
                // Create temporary subcategory for legacy tag-based filtering
                val tempSubcategory = SubcategoryItem(
                    id = subcategory,
                    parentCategory = categoryId,
                    name = subcategory,
                    filterTags = listOf(subcategory)
                )
                _selectedSubcategory.value = tempSubcategory
            }
        }
    }

    /**
     * Clear subcategory filter (show all content)
     */
    fun clearFilter() {
        _selectedSubcategory.value = null
        Timber.d("Filter cleared - showing all content")
    }
}

/**
 * UI State for category detail screen
 */
data class ContentCategoryDetailUiState(
    val categoryName: String = "",
    val allContent: List<ContentItem> = emptyList(),
    val totalContentCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
