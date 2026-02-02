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
 * - Grouping content by subcategory for vertical sections
 *
 * Layout:
 * - Subcategories displayed as SECTION HEADERS (vertical scroll)
 * - Content cards scroll HORIZONTALLY under each section
 * - Managed from OraWebApp admin portal
 */
@HiltViewModel
class ContentCategoryDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val subcategoryRepository: SubcategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""

    // Subcategories from Firestore (managed via OraWebApp)
    private val _subcategories = MutableStateFlow<List<SubcategoryItem>>(emptyList())
    val subcategories: StateFlow<List<SubcategoryItem>> = _subcategories.asStateFlow()

    // Content grouped by subcategory
    private val _groupedContent = MutableStateFlow<List<SubcategorySection>>(emptyList())
    val groupedContent: StateFlow<List<SubcategorySection>> = _groupedContent.asStateFlow()

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
                    // Re-group content when subcategories change
                    groupContentBySubcategory()
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
                .collect { allContent ->
                    _uiState.value = ContentCategoryDetailUiState(
                        categoryName = categoryId,
                        allContent = allContent,
                        totalContentCount = allContent.size,
                        isLoading = false
                    )
                    // Group content by subcategory
                    groupContentBySubcategory()
                }
        }
    }

    /**
     * Group content by subcategory for vertical section display
     * Each subcategory becomes a section with horizontally scrolling content
     */
    private fun groupContentBySubcategory() {
        val allContent = _uiState.value.allContent
        val subcats = _subcategories.value

        if (allContent.isEmpty()) {
            _groupedContent.value = emptyList()
            return
        }

        val sections = mutableListOf<SubcategorySection>()
        val assignedContentIds = mutableSetOf<String>()

        // Group content by each subcategory's filterTags
        for (subcat in subcats) {
            val matchingContent = allContent.filter { content ->
                content.tags.any { tag ->
                    subcat.filterTags.any { filterTag ->
                        tag.equals(filterTag, ignoreCase = true)
                    }
                }
            }

            if (matchingContent.isNotEmpty()) {
                sections.add(
                    SubcategorySection(
                        subcategory = subcat,
                        content = matchingContent
                    )
                )
                assignedContentIds.addAll(matchingContent.map { it.id })
            }
        }

        // Add "Autres" section for content that doesn't match any subcategory
        val unassignedContent = allContent.filter { it.id !in assignedContentIds }
        if (unassignedContent.isNotEmpty()) {
            sections.add(
                SubcategorySection(
                    subcategory = SubcategoryItem(
                        id = "autres",
                        parentCategory = categoryId,
                        name = "Autres"
                    ),
                    content = unassignedContent
                )
            )
        }

        _groupedContent.value = sections
        Timber.d("Grouped content into ${sections.size} sections")
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

/**
 * Represents a section in the category detail screen
 * Each section has a subcategory header and horizontally scrolling content
 */
data class SubcategorySection(
    val subcategory: SubcategoryItem,
    val content: List<ContentItem>
)
