package com.ora.wellbeing.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    // TODO: Injecter les use cases quand ils seront créés
    // private val getContentLibraryUseCase: GetContentLibraryUseCase,
    // private val searchContentUseCase: SearchContentUseCase,
    // private val filterContentUseCase: FilterContentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.LoadLibraryData -> loadLibraryData()
            is LibraryUiEvent.FilterByCategory -> filterByCategory(event.category)
            is LibraryUiEvent.FilterByDuration -> filterByDuration(event.duration)
            is LibraryUiEvent.SearchContent -> searchContent(event.query)
            is LibraryUiEvent.ClearFilters -> clearFilters()
        }
    }

    private fun loadLibraryData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // TODO: Remplacer par de vraies données depuis les use cases
                val mockData = generateMockLibraryData()

                _uiState.value = mockData.copy(isLoading = false)

                Timber.d("Library data loaded successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement de la bibliothèque"
                )
                Timber.e(e, "Error loading library data")
            }
        }
    }

    private fun filterByCategory(category: String) {
        val currentState = _uiState.value
        val selectedCategory = if (category.isEmpty()) null else category

        val filteredContent = if (selectedCategory == null) {
            currentState.allContent
        } else {
            currentState.allContent.filter { it.category == selectedCategory }
        }

        _uiState.value = currentState.copy(
            selectedCategory = selectedCategory,
            filteredContent = filteredContent
        )

        Timber.d("Filtered by category: $selectedCategory")
    }

    private fun filterByDuration(duration: String) {
        val currentState = _uiState.value
        val selectedDuration = if (duration.isEmpty()) null else duration

        val filteredContent = if (selectedDuration == null) {
            currentState.allContent
        } else {
            currentState.allContent.filter { content ->
                when (selectedDuration) {
                    "5-10 min" -> content.durationMinutes in 5..10
                    "10-20 min" -> content.durationMinutes in 11..20
                    "20+ min" -> content.durationMinutes > 20
                    else -> true
                }
            }
        }

        _uiState.value = currentState.copy(
            selectedDuration = selectedDuration,
            filteredContent = filteredContent
        )

        Timber.d("Filtered by duration: $selectedDuration")
    }

    private fun searchContent(query: String) {
        val currentState = _uiState.value

        val filteredContent = if (query.isEmpty()) {
            currentState.allContent
        } else {
            currentState.allContent.filter { content ->
                content.title.contains(query, ignoreCase = true) ||
                content.category.contains(query, ignoreCase = true) ||
                content.instructor.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredContent = filteredContent
        )

        Timber.d("Searched content with query: $query")
    }

    private fun clearFilters() {
        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            selectedCategory = null,
            selectedDuration = null,
            searchQuery = "",
            filteredContent = currentState.allContent
        )

        Timber.d("Filters cleared")
    }

    private fun generateMockLibraryData(): LibraryUiState {
        val allContent = listOf(
            LibraryUiState.ContentItem(
                id = "1",
                title = "Méditation matinale",
                category = "Méditation",
                duration = "10 min",
                durationMinutes = 10,
                instructor = "Sophie Martin",
                thumbnailUrl = "",
                isPopular = true,
                isNew = false
            ),
            LibraryUiState.ContentItem(
                id = "2",
                title = "Yoga pour débutants",
                category = "Yoga",
                duration = "15 min",
                durationMinutes = 15,
                instructor = "Pierre Dubois",
                thumbnailUrl = "",
                isPopular = true,
                isNew = false
            ),
            LibraryUiState.ContentItem(
                id = "3",
                title = "Respiration anti-stress",
                category = "Respiration",
                duration = "5 min",
                durationMinutes = 5,
                instructor = "Marie Leroy",
                thumbnailUrl = "",
                isPopular = false,
                isNew = true
            ),
            LibraryUiState.ContentItem(
                id = "4",
                title = "Méditation du soir",
                category = "Méditation",
                duration = "12 min",
                durationMinutes = 12,
                instructor = "Sophie Martin",
                thumbnailUrl = "",
                isPopular = false,
                isNew = true
            ),
            LibraryUiState.ContentItem(
                id = "5",
                title = "Yoga avancé",
                category = "Yoga",
                duration = "25 min",
                durationMinutes = 25,
                instructor = "Pierre Dubois",
                thumbnailUrl = "",
                isPopular = true,
                isNew = false
            ),
            LibraryUiState.ContentItem(
                id = "6",
                title = "Exercices de gratitude",
                category = "Bien-être",
                duration = "8 min",
                durationMinutes = 8,
                instructor = "Claire Moreau",
                thumbnailUrl = "",
                isPopular = false,
                isNew = true
            )
        )

        return LibraryUiState(
            allContent = allContent,
            filteredContent = allContent,
            popularContent = allContent.filter { it.isPopular },
            newContent = allContent.filter { it.isNew },
            categories = allContent.map { it.category }.distinct()
        )
    }
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