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
 * Manages the state for a category detail screen with optional duration filtering
 * Used by Quick Sessions to show only short content (< 10 minutes)
 *
 * Handles:
 * - Loading content for a specific category
 * - Filtering by duration (maxDurationMinutes)
 * - Filtering by subcategory (tags)
 * - Providing available subcategories dynamically
 */
@HiltViewModel
class ContentCategoryDetailFilteredViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle.get<String>("categoryId") ?: ""
    private val maxDurationMinutesParam: Int = savedStateHandle.get<Int>("maxDuration") ?: -1

    private val _maxDurationMinutes = MutableStateFlow<Int?>(
        if (maxDurationMinutesParam > 0) maxDurationMinutesParam else null
    )
    val maxDurationMinutes: StateFlow<Int?> = _maxDurationMinutes.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null)
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _availableSubcategories = MutableStateFlow<List<String>>(emptyList())
    val availableSubcategories: StateFlow<List<String>> = _availableSubcategories.asStateFlow()

    private val _uiState = MutableStateFlow(ContentCategoryDetailFilteredUiState())
    val uiState: StateFlow<ContentCategoryDetailFilteredUiState> = _uiState.asStateFlow()

    init {
        loadCategoryContent()
    }

    /**
     * Sets the max duration filter from the composable
     */
    fun setMaxDurationFilter(maxMinutes: Int?) {
        _maxDurationMinutes.value = maxMinutes
    }

    private fun loadCategoryContent() {
        viewModelScope.launch {
            contentRepository.getContentByCategory(categoryId)
                .catch { exception ->
                    timber.log.Timber.e(exception, "Error loading category content: $categoryId")
                    _uiState.value = ContentCategoryDetailFilteredUiState(
                        error = exception.message ?: "Erreur de chargement"
                    )
                }
                .combine(_selectedSubcategory) { allContent, selectedTag ->
                    Pair(allContent, selectedTag)
                }
                .combine(_maxDurationMinutes) { (allContent, selectedTag), maxDuration ->
                    // Filter by duration first (if specified)
                    val durationFilteredContent = if (maxDuration != null && maxDuration > 0) {
                        allContent.filter { content ->
                            // Parse duration string like "5 min" or "10 min"
                            val durationMinutes = parseDurationMinutes(content.duration)
                            durationMinutes <= maxDuration
                        }
                    } else {
                        allContent
                    }

                    // Extract unique tags from duration-filtered content
                    val uniqueTags = durationFilteredContent
                        .flatMap { it.tags }
                        .distinct()
                        .sorted()

                    _availableSubcategories.value = uniqueTags

                    // Filter by selected tag if any
                    val finalFilteredContent = if (selectedTag != null) {
                        durationFilteredContent.filter { it.tags.contains(selectedTag) }
                    } else {
                        durationFilteredContent
                    }

                    // Generate title based on category and duration filter
                    val title = generateTitle(categoryId, maxDuration)

                    ContentCategoryDetailFilteredUiState(
                        categoryName = title,
                        allContent = finalFilteredContent,
                        isLoading = false,
                        maxDurationFilter = maxDuration
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Parses duration string like "5 min", "10 min", "1h 30min" to minutes
     */
    private fun parseDurationMinutes(duration: String): Int {
        return try {
            // Handle formats like "5 min", "10min", "1h 30min"
            val lowerDuration = duration.lowercase().trim()

            // Check for hour format
            if (lowerDuration.contains("h")) {
                val parts = lowerDuration.split("h")
                val hours = parts[0].trim().toIntOrNull() ?: 0
                val minutes = parts.getOrNull(1)?.replace("min", "")?.trim()?.toIntOrNull() ?: 0
                hours * 60 + minutes
            } else {
                // Simple minutes format
                lowerDuration.replace("min", "").replace(" ", "").toIntOrNull() ?: Int.MAX_VALUE
            }
        } catch (e: Exception) {
            Int.MAX_VALUE // If we can't parse, assume it's long
        }
    }

    /**
     * Generates a user-friendly title based on category and filter
     */
    private fun generateTitle(categoryId: String, maxDuration: Int?): String {
        val baseTitle = when (categoryId.lowercase()) {
            "meditation", "mediation" -> "Meditation"
            "yoga" -> "Yoga"
            "respiration" -> "Respiration"
            "auto-massage" -> "Auto-massage"
            "pilates" -> "Pilates"
            "bien-etre", "bien-être" -> "Bien-etre"
            else -> categoryId
        }

        return if (maxDuration != null && maxDuration > 0) {
            "$baseTitle - Sessions courtes"
        } else {
            baseTitle
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
 * UI State for category detail screen with filtering
 */
data class ContentCategoryDetailFilteredUiState(
    val categoryName: String = "",
    val allContent: List<ContentItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val maxDurationFilter: Int? = null
)
