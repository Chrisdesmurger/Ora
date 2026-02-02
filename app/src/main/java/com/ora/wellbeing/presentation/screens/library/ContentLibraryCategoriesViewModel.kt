package com.ora.wellbeing.presentation.screens.library

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.R
import com.ora.wellbeing.data.cache.ContentCacheManager
import com.ora.wellbeing.data.model.CategoryItem
import com.ora.wellbeing.domain.repository.ContentRepository
import com.ora.wellbeing.presentation.theme.CategoryMeditationLavender
import com.ora.wellbeing.presentation.theme.CategoryPilatesPeach
import com.ora.wellbeing.presentation.theme.CategoryYogaGreen
import com.ora.wellbeing.presentation.theme.CategoryWellnessBeige
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ContentLibraryCategoriesViewModel
 *
 * Manages the state for the content library categories screen
 * Provides the list of main categories to display
 *
 * Also triggers preloading of subcategories and content for all categories
 * to ensure smooth navigation to category detail screens.
 */
@HiltViewModel
class ContentLibraryCategoriesViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val contentCacheManager: ContentCacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentLibraryCategoriesUiState())
    val uiState: StateFlow<ContentLibraryCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        // Start preloading all category data for smooth navigation
        preloadCategoryData()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load categories and get real item counts
            val categories = createCategories()

            // Update item counts from content repository
            val categoriesWithCounts = categories.map { category ->
                try {
                    val count = contentRepository.getContentByCategory(category.id)
                        .catch { emit(emptyList()) }
                        .let { flow ->
                            var itemCount = 0
                            flow.collect { items -> itemCount = items.size }
                            itemCount
                        }
                    category.copy(itemCount = count)
                } catch (e: Exception) {
                    Timber.e(e, "Error getting count for ${category.id}")
                    category
                }
            }

            _uiState.value = ContentLibraryCategoriesUiState(
                categories = categoriesWithCounts,
                isLoading = false
            )
        }
    }

    /**
     * Preload subcategories and content for all categories
     * This enables instant navigation to category detail screens
     */
    private fun preloadCategoryData() {
        contentCacheManager.preloadAllCategories()
        Timber.d("Triggered preload for all categories")
    }

    /**
     * Creates the MVP category list with predefined colors
     * Order: Meditation, Yoga, Pilates, Bien-etre
     */
    private fun createCategories(): List<CategoryItem> {
        return listOf(
            CategoryItem(
                id = "Meditation",
                name = "Meditation",
                color = CategoryMeditationLavender,
                iconResId = R.drawable.category_meditation,
                itemCount = 0 // TODO: Get real count from ContentRepository
            ),
            CategoryItem(
                id = "Yoga",
                name = "Yoga",
                color = CategoryYogaGreen,
                iconResId = R.drawable.category_yoga,
                itemCount = 0
            ),
            CategoryItem(
                id = "Pilates",
                name = "Pilates",
                color = CategoryPilatesPeach,
                iconResId = R.drawable.category_pilates,
                itemCount = 0
            ),
            CategoryItem(
                id = "Bien-etre",
                name = "Bien-etre",
                color = CategoryWellnessBeige,
                iconResId = R.drawable.category_wellness,
                itemCount = 0
            )
        )
    }

    fun onCategoryClick(categoryId: String) {
        // Navigation is handled in the screen composable
        timber.log.Timber.d("Category clicked: $categoryId")
    }
}

/**
 * UI State for content library categories screen
 */
data class ContentLibraryCategoriesUiState(
    val categories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
