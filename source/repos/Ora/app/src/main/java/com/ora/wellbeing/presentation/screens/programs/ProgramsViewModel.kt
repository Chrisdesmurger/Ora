package com.ora.wellbeing.presentation.screens.programs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.R
import com.ora.wellbeing.data.model.Program
import com.ora.wellbeing.data.model.UserProgram
import com.ora.wellbeing.domain.repository.ProgramRepository
import com.ora.wellbeing.domain.repository.UserProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    application: Application,
    private val programRepository: ProgramRepository,
    private val userProgramRepository: UserProgramRepository,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ProgramsUiState())
    val uiState: StateFlow<ProgramsUiState> = _uiState.asStateFlow()

    init {
        observeProgramData()
    }

    fun onEvent(event: ProgramsUiEvent) {
        when (event) {
            is ProgramsUiEvent.LoadProgramsData -> observeProgramData()
            is ProgramsUiEvent.JoinProgram -> joinProgram(event.programId)
            is ProgramsUiEvent.LeaveProgram -> leaveProgram(event.programId)
        }
    }

    private fun observeProgramData() {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("observeProgramData: No authenticated user")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = getApplication<Application>().getString(R.string.error_must_login_programs)
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Combine all programs and user enrollments for real-time sync
                combine(
                    programRepository.getAllPrograms(),
                    programRepository.getPopularPrograms(limit = 10),
                    userProgramRepository.getEnrolledPrograms(uid)
                ) { allPrograms, popularPrograms, enrolledPrograms ->
                    Triple(allPrograms, popularPrograms, enrolledPrograms)
                }.collect { (allPrograms, popularPrograms, enrolledPrograms) ->

                    // Map user enrollments to active programs for UI
                    val activePrograms = enrolledPrograms
                        .filter { !it.isCompleted }
                        .map { userProgram ->
                            // Find the corresponding program details
                            val program = allPrograms.find { it.id == userProgram.programId }
                            userProgram.toActiveProgram(program)
                        }

                    // Get recommended programs (high rating, not enrolled)
                    val enrolledProgramIds = enrolledPrograms.map { it.programId }.toSet()
                    val recommendedPrograms = allPrograms
                        .filter { it.hasHighRating() && it.id !in enrolledProgramIds }
                        .sortedByDescending { it.rating }
                        .take(5)
                        .map { it.toUiProgram(isEnrolled = false) }

                    // Get popular challenges (category = Défis)
                    val popularChallenges = allPrograms
                        .filter { it.category == "Défis" && it.isPopular() }
                        .sortedByDescending { it.participantCount }
                        .take(5)
                        .map { it.toUiProgram(isEnrolled = it.id in enrolledProgramIds) }

                    // Group all programs by category
                    val programsByCategory = allPrograms
                        .groupBy { it.category }
                        .mapValues { (_, programs) ->
                            programs.map { program ->
                                program.toUiProgram(isEnrolled = program.id in enrolledProgramIds)
                            }
                        }

                    _uiState.value = ProgramsUiState(
                        isLoading = false,
                        error = null,
                        activePrograms = activePrograms,
                        recommendedPrograms = recommendedPrograms,
                        popularChallenges = popularChallenges,
                        programsByCategory = programsByCategory
                    )

                    Timber.d("observeProgramData: Updated UI state (${allPrograms.size} programs, ${activePrograms.size} active)")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = getApplication<Application>().getString(R.string.error_loading_programs, e.message ?: "")
                )
                Timber.e(e, "Error observing program data")
            }
        }
    }

    private fun joinProgram(programId: String) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("joinProgram: No authenticated user")
            _uiState.value = _uiState.value.copy(
                error = getApplication<Application>().getString(R.string.error_must_login_programs)
            )
            return
        }

        viewModelScope.launch {
            try {
                // Get program details to get total days
                val program = programRepository.getProgram(programId)
                program.collect { prog ->
                    if (prog != null) {
                        val result = userProgramRepository.enrollInProgram(uid, programId, prog.duration)
                        result.fold(
                            onSuccess = {
                                Timber.i("joinProgram: Successfully enrolled in program $programId")
                                // UI state will update automatically via Flow
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    error = getApplication<Application>().getString(R.string.error_enrollment, error.message ?: "")
                                )
                                Timber.e(error, "Error enrolling in program")
                            }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            error = getApplication<Application>().getString(R.string.error_program_not_found)
                        )
                        Timber.e("joinProgram: Program $programId not found")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_enrollment, e.message ?: "")
                )
                Timber.e(e, "Error joining program")
            }
        }
    }

    private fun leaveProgram(programId: String) {
        val uid = auth.currentUser?.uid ?: run {
            Timber.e("leaveProgram: No authenticated user")
            return
        }

        viewModelScope.launch {
            try {
                val result = userProgramRepository.unenrollFromProgram(uid, programId)
                result.fold(
                    onSuccess = {
                        Timber.i("leaveProgram: Successfully unenrolled from program $programId")
                        // UI state will update automatically via Flow
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = getApplication<Application>().getString(R.string.error_leaving_program, error.message ?: "")
                        )
                        Timber.e(error, "Error leaving program")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = getApplication<Application>().getString(R.string.error_leaving_program, e.message ?: "")
                )
                Timber.e(e, "Error leaving program")
            }
        }
    }

    /**
     * Converts UserProgram (data model) to ActiveProgram (UI model)
     */
    private fun UserProgram.toActiveProgram(program: Program?): ProgramsUiState.ActiveProgram {
        return ProgramsUiState.ActiveProgram(
            id = programId,
            title = program?.title ?: getApplication<Application>().getString(R.string.program_unknown),
            description = program?.description ?: "",
            currentDay = currentDay,
            totalDays = totalDays,
            progressPercentage = calculateProgress(), // Use calculateProgress() method
            nextSessionTitle = "Session $currentDay",
            nextSessionDuration = "10 min", // Default, could be dynamic
            category = program?.category ?: "",
            thumbnailUrl = program?.thumbnailUrl ?: ""
        )
    }

    /**
     * Converts Program (data model) to Program (UI model)
     */
    private fun Program.toUiProgram(isEnrolled: Boolean): ProgramsUiState.Program {
        val priceText = if (isPremiumOnly) {
            getApplication<Application>().getString(R.string.plan_premium)
        } else {
            getApplication<Application>().getString(R.string.plan_free)
        }

        return ProgramsUiState.Program(
            id = id,
            title = title,
            description = description,
            category = category,
            duration = duration,
            level = level,
            participantCount = participantCount,
            rating = rating,
            thumbnailUrl = thumbnailUrl ?: "",
            instructor = instructor ?: "",
            price = priceText,
            isEnrolled = isEnrolled,
            estimatedTimePerDay = "10-15 min" // Could be dynamic based on program data
        )
    }
}

/**
 * État de l'interface utilisateur pour l'écran Programs
 */
data class ProgramsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activePrograms: List<ActiveProgram> = emptyList(),
    val recommendedPrograms: List<Program> = emptyList(),
    val popularChallenges: List<Program> = emptyList(),
    val programsByCategory: Map<String, List<Program>> = emptyMap()
) {
    /**
     * Programme actif de l'utilisateur
     */
    data class ActiveProgram(
        val id: String,
        val title: String,
        val description: String,
        val currentDay: Int,
        val totalDays: Int,
        val progressPercentage: Int,
        val nextSessionTitle: String,
        val nextSessionDuration: String,
        val category: String = "",
        val thumbnailUrl: String = ""
    )

    /**
     * Programme disponible
     */
    data class Program(
        val id: String,
        val title: String,
        val description: String,
        val category: String,
        val duration: Int, // en jours
        val level: String,
        val participantCount: Int,
        val rating: Float,
        val thumbnailUrl: String,
        val instructor: String = "",
        val price: String = "Gratuit",
        val isEnrolled: Boolean = false,
        val estimatedTimePerDay: String = "10-15 min"
    )
}

/**
 * Événements de l'interface utilisateur pour l'écran Programs
 */
sealed interface ProgramsUiEvent {
    data object LoadProgramsData : ProgramsUiEvent
    data class JoinProgram(val programId: String) : ProgramsUiEvent
    data class LeaveProgram(val programId: String) : ProgramsUiEvent
}
