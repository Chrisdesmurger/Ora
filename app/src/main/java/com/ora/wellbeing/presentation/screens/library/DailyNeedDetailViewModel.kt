package com.ora.wellbeing.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.core.localization.LocalizationProvider
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.DailyNeedCategory
import com.ora.wellbeing.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * NEW (Issue #33): ViewModel pour DailyNeedDetailScreen
 * Gère le chargement et le filtrage du contenu par need_tags
 * Utilise les catégories codées en dur et filtre depuis ContentRepository
 */
@HiltViewModel
class DailyNeedDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val localizationProvider: LocalizationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyNeedDetailUiState())
    val uiState: StateFlow<DailyNeedDetailUiState> = _uiState.asStateFlow()

    fun onEvent(event: DailyNeedDetailUiEvent) {
        when (event) {
            is DailyNeedDetailUiEvent.LoadCategory -> loadCategoryContent(event.categoryId)
        }
    }

    private fun loadCategoryContent(categoryId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Récupérer la catégorie depuis les catégories codées en dur
                val category = DailyNeedCategory.getCategoryById(categoryId)

                if (category == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Catégorie non trouvée"
                        )
                    }
                    return@launch
                }

                // Collecter le contenu et filtrer par need_tags
                contentRepository.getAllContent()
                    .collect { allContent ->
                        // Filtrer par need_tags de la catégorie
                        val filteredContent = allContent.filter { content ->
                            // Vérifier si le contenu a au moins un tag qui matche les filter_tags de la catégorie
                            content.needTags.any { tag -> tag in category.filterTags }
                        }

                        val currentLocale = localizationProvider.getCurrentLocale()
                        val localizedName = category.getLocalizedName(currentLocale)

                        Timber.d("DailyNeed: Category=$localizedName, FilterTags=${category.filterTags}, Total=${allContent.size}, Filtered=${filteredContent.size}")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categoryName = localizedName,
                                filteredContent = filteredContent,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors du chargement de la catégorie $categoryId")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur de chargement: ${e.message}"
                    )
                }
            }
        }
    }
}

/**
 * UI State pour DailyNeedDetailScreen
 */
data class DailyNeedDetailUiState(
    val isLoading: Boolean = false,
    val categoryName: String = "",
    val filteredContent: List<ContentItem> = emptyList(),
    val error: String? = null
)

/**
 * Events pour DailyNeedDetailScreen
 */
sealed class DailyNeedDetailUiEvent {
    data class LoadCategory(val categoryId: String) : DailyNeedDetailUiEvent()
}
