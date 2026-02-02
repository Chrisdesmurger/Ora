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
 * ContentCategoryDetailViewModel
 *
 * Manages the state for a single category detail screen
 * Handles:
 * - Loading content for a specific category
 * - Filtering by subcategory (tags)
 * - Providing available subcategories dynamically
 */
@HiltViewModel
class ContentCategoryDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _availableSubcategories = MutableStateFlow<List<String>>(emptyList())
    val availableSubcategories: StateFlow<List<String>> = _availableSubcategories.asStateFlow()

    private val _uiState = MutableStateFlow(ContentCategoryDetailUiState())
    val uiState: StateFlow<ContentCategoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadCategoryContent()
    }

    private fun loadCategoryContent() {
        viewModelScope.launch {
            contentRepository.getContentByCategory(categoryId)
                .catch { exception ->
                    timber.log.Timber.e(exception, "Error loading category content: $categoryId")
                    _uiState.value = ContentCategoryDetailUiState(
                        error = exception.message ?: "Erreur de chargement"
                    )
                }
                .combine(_selectedSubcategory) { allContent, selectedTag ->
                    // Extract unique tags from all content
                    val uniqueTags = allContent
                        .flatMap { it.tags }
                        .distinct()
                        .sorted()

                    _availableSubcategories.value = uniqueTags

                    // Filter by selected tag if any
                    val filteredContent = if (selectedTag != null) {
                        allContent.filter { it.tags.contains(selectedTag) }
                    } else {
                        allContent
                    }

                    ContentCategoryDetailUiState(
                        categoryName = categoryId,
                        allContent = filteredContent,
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
 * UI State for category detail screen
 */
data class ContentCategoryDetailUiState(
    val categoryName: String = "",
    val allContent: List<ContentItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
