package com.ora.wellbeing.feature.practice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ora.wellbeing.data.local.entities.Content
import com.ora.wellbeing.data.local.entities.ContentType
import com.ora.wellbeing.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel léger pour déterminer le type de pratique sans créer de player.
 * Utilisé uniquement pour le routage vers le bon lecteur spécialisé.
 */
@HiltViewModel
class PracticeTypeViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeTypeState())
    val state: StateFlow<PracticeTypeState> = _state.asStateFlow()

    fun loadPracticeType(practiceId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val content = contentRepository.getContentById(practiceId)
                _state.value = _state.value.copy(
                    contentType = content?.type,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

data class PracticeTypeState(
    val contentType: ContentType? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
