package com.ora.wellbeing.presentation.screens.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.R
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    application: Application,
    private val contentRepository: ContentRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    // Filter state
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedDuration = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    init {
        observeContentData()
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.LoadLibraryData -> observeContentData()
            is LibraryUiEvent.FilterByCategory -> filterByCategory(event.category)
            is LibraryUiEvent.FilterByDuration -> filterByDuration(event.duration)
            is LibraryUiEvent.SearchContent -> searchContent(event.query)
            is LibraryUiEvent.ClearFilters -> clearFilters()
        }
    }

    private fun observeContentData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Combine content flows with filter states for real-time filtering
                combine(
                    contentRepository.getAllContent(),
                    contentRepository.getPopularContent(limit = 10),
                    contentRepository.getNewContent(limit = 10),
                    _selectedCategory,
                    _selectedDuration,
                    _searchQuery
                ) { flows: Array<Any?> ->
                    val allContent = flows[0] as List<com.ora.wellbeing.data.model.ContentItem>
                    val popularContent = flows[1] as List<com.ora.wellbeing.data.model.ContentItem>
                    val newContent = flows[2] as List<com.ora.wellbeing.data.model.ContentItem>
                    val category = flows[3] as String?
                    val duration = flows[4] as String?
                    val query = flows[5] as String
                    FilteredContentData(allContent, popularContent, newContent, category, duration, query)
                }.collect { data ->
                    // Apply all filters
                    val filteredContent = applyFilters(data.allContent, data.category, data.duration, data.query)

                    // Extract unique categories from all content
                    val categories = data.allContent.map { it.category }.distinct().sorted()

                    _uiState.value = LibraryUiState(
                        isLoading = false,
                        error = null,
                        allContent = data.allContent.map { it.toUiContentItem() },
                        filteredContent = filteredContent.map { it.toUiContentItem() },
                        popularContent = data.popularContent.map { it.toUiContentItem() },
                        newContent = data.newContent.map { it.toUiContentItem() },
                        categories = categories,
                        selectedCategory = data.category,
                        selectedDuration = data.duration,
                        searchQuery = data.query
                    )

                    Timber.d("observeContentData: Updated UI state (${data.allContent.size} total, ${filteredContent.size} filtered)")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = getApplication<Application>().getString(R.string.error_loading_library, e.message ?: "")
                )
                Timber.e(e, "Error observing content data")
            }
        }
    }

    private fun filterByCategory(category: String) {
        _selectedCategory.value = if (category.isEmpty()) null else category
        Timber.d("filterByCategory: $category")
    }

    private fun filterByDuration(duration: String) {
        _selectedDuration.value = if (duration.isEmpty()) null else duration
        Timber.d("filterByDuration: $duration")
    }

    private fun searchContent(query: String) {
        _searchQuery.value = query
        Timber.d("searchContent: $query")
    }

    private fun clearFilters() {
        _selectedCategory.value = null
        _selectedDuration.value = null
        _searchQuery.value = ""
        Timber.d("Filters cleared")
    }

    /**
     * Apply all filters to content list
     */
    private fun applyFilters(
        content: List<ContentItem>,
        category: String?,
        duration: String?,
        query: String
    ): List<ContentItem> {
        var filtered = content

        // Filter by category
        if (!category.isNullOrEmpty()) {
            filtered = filtered.filter { it.category == category }
        }

        // Filter by duration
        if (!duration.isNullOrEmpty()) {
            filtered = filtered.filter { item ->
                when (duration) {
                    "5-10 min" -> item.durationMinutes in 5..10
                    "10-20 min" -> item.durationMinutes in 11..20
                    "20+ min" -> item.durationMinutes > 20
                    else -> true
                }
            }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered = filtered.filter { it.matchesQuery(query) }
        }

        return filtered
    }

    /**
     * Converts ContentItem (data model) to ContentItem (UI model)
     */
    private fun ContentItem.toUiContentItem(): LibraryUiState.ContentItem {
        return LibraryUiState.ContentItem(
            id = id,
            title = title,
            category = category,
            duration = duration,
            durationMinutes = durationMinutes,
            instructor = instructor,
            thumbnailUrl = thumbnailUrl ?: "",
            description = description,
            isPopular = isPopular,
            isNew = isNew,
            rating = rating,
            completionCount = completionCount
        )
    }

    /**
     * Data class to hold filtered content data
     */
    private data class FilteredContentData(
        val allContent: List<ContentItem>,
        val popularContent: List<ContentItem>,
        val newContent: List<ContentItem>,
        val category: String?,
        val duration: String?,
        val query: String
    )
}

/**
 * État de l'interface utilisateur pour l'écran Library
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allContent: List<ContentItem> = emptyList(),
    val filteredContent: List<ContentItem> = emptyList(),
    val popularContent: List<ContentItem> = emptyList(),
    val newContent: List<ContentItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val selectedDuration: String? = null,
    val searchQuery: String = ""
) {
    /**
     * Élément de contenu dans la bibliothèque
     */
    data class ContentItem(
        val id: String,
        val title: String,
        val category: String,
        val duration: String,
        val durationMinutes: Int,
        val instructor: String,
        val thumbnailUrl: String,
        val description: String = "",
        val isPopular: Boolean = false,
        val isNew: Boolean = false,
        val rating: Float = 0f,
        val completionCount: Int = 0
    )
}

/**
 * Événements de l'interface utilisateur pour l'écran Library
 */
sealed interface LibraryUiEvent {
    data object LoadLibraryData : LibraryUiEvent
    data class FilterByCategory(val category: String) : LibraryUiEvent
    data class FilterByDuration(val duration: String) : LibraryUiEvent
    data class SearchContent(val query: String) : LibraryUiEvent
    data object ClearFilters : LibraryUiEvent
}
