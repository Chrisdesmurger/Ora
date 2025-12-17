package com.ora.wellbeing.presentation.screens.library

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.R
import com.ora.wellbeing.data.model.CategoryItem
import com.ora.wellbeing.presentation.theme.CategoryMeditationLavender
import com.ora.wellbeing.presentation.theme.CategoryPilatesPeach
import com.ora.wellbeing.presentation.theme.CategoryYogaGreen
import com.ora.wellbeing.presentation.theme.CategoryWellnessBeige
import com.ora.wellbeing.presentation.theme.CategoryBreathingBlue
import com.ora.wellbeing.presentation.theme.CategoryAutoMassageWarm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ContentLibraryCategoriesViewModel
 *
 * Manages the state for the content library categories screen
 * Provides the list of main categories to display
 *
 * Issue #37: Added Respiration and Auto-massage categories
 */
@HiltViewModel
class ContentLibraryCategoriesViewModel @Inject constructor(
    // Future: inject ContentRepository to get real item counts
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentLibraryCategoriesUiState())
    val uiState: StateFlow<ContentLibraryCategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = ContentLibraryCategoriesUiState(
                categories = createCategories(),
                isLoading = false
            )
        }
    }

    /**
     * Creates the MVP category list with predefined colors
     * Issue #37: Added Respiration and Auto-massage categories
     * Order: Meditation, Yoga, Pilates, Respiration, Auto-massage, Bien-etre
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
            // Issue #37: New category - Respiration
            CategoryItem(
                id = "Respiration",
                name = "Respiration",
                color = CategoryBreathingBlue,
                iconResId = R.drawable.category_respiration,
                itemCount = 0
            ),
            // Issue #37: New category - Auto-massage
            CategoryItem(
                id = "Auto-massage",
                name = "Auto-massage",
                color = CategoryAutoMassageWarm,
                iconResId = R.drawable.category_auto_massage,
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
