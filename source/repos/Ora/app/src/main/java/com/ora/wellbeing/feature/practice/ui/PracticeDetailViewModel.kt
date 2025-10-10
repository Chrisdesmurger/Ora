package com.ora.wellbeing.feature.practice.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.ora.wellbeing.core.data.ambient.AmbientRepository
import com.ora.wellbeing.core.data.practice.PracticeRepository
import com.ora.wellbeing.core.domain.ambient.AmbientType
import com.ora.wellbeing.core.domain.practice.DownloadInfo
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.core.domain.stats.IncrementSessionUseCase
import com.ora.wellbeing.core.domain.user.UserNotesRepository
import com.ora.wellbeing.feature.practice.ambient.AmbientController
import com.ora.wellbeing.feature.practice.player.PlayerState
import com.ora.wellbeing.feature.practice.player.PracticePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * UI State pour l'écran de détail de pratique
 */
data class PracticeUiState(
    val isLoading: Boolean = false,
    val practice: Practice? = null,
    val error: String? = null,
    val playerState: PlayerState = PlayerState(),
    val similarPractices: List<Practice> = emptyList(),
    val downloadState: DownloadInfo? = null,
    val isDownloaded: Boolean = false,
    val showNotesDialog: Boolean = false,
    val currentNotes: String = "",
    val sessionStartTime: Long = 0L
)

/**
 * UI Events
 */
sealed class PracticeUiEvent {
    object StartSession : PracticeUiEvent()
    object TogglePlayPause : PracticeUiEvent()
    object SeekForward : PracticeUiEvent()
    object SeekBackward : PracticeUiEvent()
    object AddToProgram : PracticeUiEvent()
    object OpenNotes : PracticeUiEvent()
    object DismissNotesDialog : PracticeUiEvent()
    data class SaveNotes(val notes: String) : PracticeUiEvent()
    object ToggleDownload : PracticeUiEvent()
    data class SelectAmbientTrack(val type: AmbientType) : PracticeUiEvent()
    data class SetAmbientVolume(val volume: Float) : PracticeUiEvent()
    data class ToggleCrossfade(val enabled: Boolean) : PracticeUiEvent()
}

@HiltViewModel
class PracticeDetailViewModel @Inject constructor(
    application: Application,
    private val practiceRepository: PracticeRepository,
    private val ambientRepository: AmbientRepository,
    private val notesRepository: UserNotesRepository,
    private val incrementSessionUseCase: IncrementSessionUseCase,
    private val analytics: FirebaseAnalytics
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var player: PracticePlayer? = null
    private var ambientController: AmbientController? = null

    init {
        // Initialize players
        player = PracticePlayer(application.applicationContext)
        ambientController = AmbientController(application.applicationContext, viewModelScope)

        // Observe player state
        viewModelScope.launch {
            player?.state?.collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }

                // If session completed
                if (playerState.completed && _uiState.value.sessionStartTime > 0L) {
                    onSessionCompleted()
                }
            }
        }
    }

    /**
     * Charge la pratique
     */
    fun loadPractice(practiceId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load practice
            practiceRepository.getById(practiceId).fold(
                onSuccess = { practice ->
                    _uiState.update { it.copy(practice = practice) }

                    // Load similar practices
                    loadSimilarPractices(practiceId)

                    // Load notes
                    loadNotes(practiceId)

                    // Prepare player
                    player?.prepare(practice.mediaUrl)

                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erreur de chargement"
                        )
                    }
                }
            )
        }
    }

    /**
     * Charge les pratiques similaires
     */
    private suspend fun loadSimilarPractices(practiceId: String) {
        practiceRepository.getSimilar(practiceId).fold(
            onSuccess = { similar ->
                _uiState.update { it.copy(similarPractices = similar) }
            },
            onFailure = { error ->
                Timber.e(error, "Erreur lors du chargement des pratiques similaires")
            }
        )
    }

    /**
     * Charge les notes
     */
    private suspend fun loadNotes(practiceId: String) {
        notesRepository.getNotes(practiceId).fold(
            onSuccess = { notes ->
                _uiState.update { it.copy(currentNotes = notes) }
            },
            onFailure = { error ->
                Timber.e(error, "Erreur lors du chargement des notes")
            }
        )
    }

    /**
     * Gère les événements UI
     */
    fun onEvent(event: PracticeUiEvent) {
        when (event) {
            is PracticeUiEvent.StartSession -> startSession()
            is PracticeUiEvent.TogglePlayPause -> togglePlayPause()
            is PracticeUiEvent.SeekForward -> player?.seekForward()
            is PracticeUiEvent.SeekBackward -> player?.seekBackward()
            is PracticeUiEvent.AddToProgram -> addToProgram()
            is PracticeUiEvent.OpenNotes -> _uiState.update { it.copy(showNotesDialog = true) }
            is PracticeUiEvent.DismissNotesDialog -> _uiState.update { it.copy(showNotesDialog = false) }
            is PracticeUiEvent.SaveNotes -> saveNotes(event.notes)
            is PracticeUiEvent.ToggleDownload -> toggleDownload()
            is PracticeUiEvent.SelectAmbientTrack -> selectAmbientTrack(event.type)
            is PracticeUiEvent.SetAmbientVolume -> ambientController?.setVolume(event.volume)
            is PracticeUiEvent.ToggleCrossfade -> ambientController?.setCrossfadeEnabled(event.enabled)
        }
    }

    /**
     * Démarre la session
     */
    private fun startSession() {
        val practice = _uiState.value.practice ?: return

        // Log analytics
        analytics.logEvent("session_started") {
            param("practice_id", practice.id)
            param("media_type", practice.mediaType.name)
            param("discipline", practice.discipline.name)
        }

        _uiState.update { it.copy(sessionStartTime = System.currentTimeMillis()) }
        player?.play()
    }

    /**
     * Toggle play/pause
     */
    private fun togglePlayPause() {
        if (_uiState.value.playerState.isPlaying) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    /**
     * Ajoute au programme (stub)
     */
    private fun addToProgram() {
        // TODO: Implémenter l'ajout au programme
        Timber.d("Add to program clicked")
        viewModelScope.launch {
            // Show toast or navigate to program selection
        }
    }

    /**
     * Enregistre les notes
     */
    private fun saveNotes(notes: String) {
        val practiceId = _uiState.value.practice?.id ?: return

        viewModelScope.launch {
            notesRepository.saveNotes(practiceId, notes).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            currentNotes = notes,
                            showNotesDialog = false
                        )
                    }
                    Timber.d("Notes saved")
                },
                onFailure = { error ->
                    Timber.e(error, "Erreur lors de l'enregistrement des notes")
                }
            )
        }
    }

    /**
     * Toggle téléchargement
     */
    private fun toggleDownload() {
        val practiceId = _uiState.value.practice?.id ?: return

        viewModelScope.launch {
            if (_uiState.value.isDownloaded) {
                // Delete download
                practiceRepository.deleteDownload(practiceId)
                _uiState.update { it.copy(isDownloaded = false) }
            } else {
                // Start download
                practiceRepository.startDownload(practiceId)
            }
        }
    }

    /**
     * Sélectionne une piste d'ambiance
     */
    private fun selectAmbientTrack(type: AmbientType) {
        if (type == AmbientType.NONE) {
            ambientController?.stop()
            return
        }

        viewModelScope.launch {
            ambientRepository.getByType(type).fold(
                onSuccess = { track ->
                    track?.let {
                        ambientController?.start(it.url, it.id, it.defaultVolume)
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Erreur lors du chargement de la piste d'ambiance")
                }
            )
        }
    }

    /**
     * Session terminée
     */
    private fun onSessionCompleted() {
        val practice = _uiState.value.practice ?: return
        val startTime = _uiState.value.sessionStartTime

        if (startTime == 0L) return

        viewModelScope.launch {
            // Increment stats
            incrementSessionUseCase(practice.durationMin, practice.discipline.name).fold(
                onSuccess = {
                    Timber.d("Stats updated")
                },
                onFailure = { error ->
                    Timber.e(error, "Erreur lors de la mise à jour des stats")
                }
            )

            // Log analytics
            analytics.logEvent("session_completed") {
                param("practice_id", practice.id)
                param("duration_min", practice.durationMin.toLong())
                param("ambient_used", if (ambientController?.state?.value?.isPlaying == true) "yes" else "no")
            }

            // Reset session start time
            _uiState.update { it.copy(sessionStartTime = 0L) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        ambientController?.release()
    }
}
