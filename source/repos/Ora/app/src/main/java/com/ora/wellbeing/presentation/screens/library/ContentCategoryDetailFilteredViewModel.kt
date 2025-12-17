package com.ora.wellbeing.presentation.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ContentCategoryDetailFilteredViewModel
 *
 * Issue #37: ViewModel for filtered category detail screen
 * Manages the state for a category with duration filter (< X minutes)
 * Used by Quick Sessions to show only short content
 *
 * Handles:
 * - Loading content for a specific category
 * - Filtering by maximum duration
 * - Providing available subcategories dynamically
 */
@HiltViewModel
class ContentCategoryDetailFilteredViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""
    private val maxDurationMinutes: Int = savedStateHandle.get<Int>("maxDurationMinutes") ?: 10

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _availableSubcategories = MutableStateFlow<List<String>>(emptyList())
    val availableSubcategories: StateFlow<List<String>> = _availableSubcategories.asStateFlow()

    private val _uiState = MutableStateFlow(ContentCategoryDetailFilteredUiState())
    val uiState: StateFlow<ContentCategoryDetailFilteredUiState> = _uiState.asStateFlow()

    init {
        loadFilteredCategoryContent()
    }

    private fun loadFilteredCategoryContent() {
        viewModelScope.launch {
            contentRepository.getContentByCategory(categoryId)
                .catch { exception ->
                    timber.log.Timber.e(exception, "Error loading filtered category content: $categoryId")
                    _uiState.value = ContentCategoryDetailFilteredUiState(
                        error = exception.message ?: "Erreur de chargement"
                    )
                }
                .combine(_selectedSubcategory) { allContent, selectedTag ->
                    // Filter by duration (use durationMinutes field from ContentItem)
                    val durationFiltered = allContent.filter { content ->
                        content.durationMinutes <= maxDurationMinutes
                    }

                    // Extract unique tags from duration-filtered content
                    val uniqueTags = durationFiltered
                        .flatMap { it.tags }
                        .distinct()
                        .sorted()

                    _availableSubcategories.value = uniqueTags

                    // Filter by selected tag if any
                    val finalContent = if (selectedTag != null) {
                        durationFiltered.filter { it.tags.contains(selectedTag) }
                    } else {
                        durationFiltered
                    }

                    ContentCategoryDetailFilteredUiState(
                        categoryName = categoryId,
                        maxDurationMinutes = maxDurationMinutes,
                        allContent = finalContent,
                        totalCount = finalContent.size,
                        isLoading = false
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun onSubcategoryClick(subcategory: String?) {
        viewModelScope.launch {
            _selectedSubcategory.value = if (subcategory == _selectedSubcategory.value) {
                // Deselect if clicking the same subcategory (toggle behavior)
                null
            } else {
                subcategory
            }
        }
    }

    fun clearFilter() {
        viewModelScope.launch {
            _selectedSubcategory.value = null
        }
    }
}

/**
 * UI State for filtered category detail screen
 * Issue #37: Includes maxDurationMinutes for badge display
 */
data class ContentCategoryDetailFilteredUiState(
    val categoryName: String = "",
    val maxDurationMinutes: Int = 10,
    val allContent: List<ContentItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
