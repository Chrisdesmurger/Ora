package com.ora.wellbeing.presentation.screens.onboarding

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.BuildConfig
import com.ora.wellbeing.data.model.onboarding.OnboardingConfig
import com.ora.wellbeing.data.model.onboarding.OnboardingMetadata
import com.ora.wellbeing.data.model.onboarding.OnboardingQuestion
import com.ora.wellbeing.data.model.onboarding.QuestionTypeKind
import com.ora.wellbeing.data.model.onboarding.UserOnboardingAnswer
import com.ora.wellbeing.data.model.onboarding.UserOnboardingResponse
import com.ora.wellbeing.data.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Onboarding Flow
 * Manages question navigation, answer collection, and response persistence
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val userProfileRepository: com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var config: OnboardingConfig? = null
    private var startTime: Long = 0L
    private val answers = mutableMapOf<String, UserOnboardingAnswer>()

    init {
        loadOnboardingConfig()
    }

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.StartOnboarding -> startOnboarding()
            OnboardingUiEvent.NextQuestion -> moveToNextQuestion()
            OnboardingUiEvent.PreviousQuestion -> moveToPreviousQuestion()
            is OnboardingUiEvent.AnswerQuestion -> answerCurrentQuestion(event.selectedOptions, event.textAnswer)
            OnboardingUiEvent.SkipQuestion -> skipCurrentQuestion()
            OnboardingUiEvent.CompleteOnboarding -> completeOnboarding()
            OnboardingUiEvent.RetryLoad -> loadOnboardingConfig()
        }
    }

    private fun loadOnboardingConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            onboardingRepository.getActiveOnboardingConfig()
                .onSuccess { loadedConfig ->
                    config = loadedConfig
                    val sortedQuestions = loadedConfig.questions.sortedBy { it.order }

                    _uiState.value = OnboardingUiState(
                        isLoading = false,
                        config = loadedConfig,
                        questions = sortedQuestions,
                        currentQuestionIndex = 0,
                        totalQuestions = sortedQuestions.size,
                        isComplete = false
                    )

                    Timber.d("Onboarding config loaded: ${sortedQuestions.size} questions")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Impossible de charger le questionnaire : ${error.message}"
                    )
                    Timber.e(error, "Failed to load onboarding config")
                }
        }
    }

    private fun startOnboarding() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = _uiState.value.copy(error = "Vous devez être connecté")
            return
        }

        val configVersion = config?.id ?: run {
            _uiState.value = _uiState.value.copy(error = "Configuration non chargée")
            return
        }

        startTime = System.currentTimeMillis()

        viewModelScope.launch {
            onboardingRepository.startOnboarding(uid, configVersion)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(hasStarted = true)
                    Timber.d("Onboarding started for user $uid")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to start onboarding")
                }
        }
    }

    private fun answerCurrentQuestion(selectedOptions: List<String>, textAnswer: String?) {
        val currentQuestion = getCurrentQuestion() ?: return
        val currentState = _uiState.value

        // Create answer
        val answer = UserOnboardingAnswer(
            questionId = currentQuestion.id,
            selectedOptions = selectedOptions,
            textAnswer = textAnswer,
            answeredAt = Timestamp.now()
        )

        answers[currentQuestion.id] = answer

        // Update state with new answer
        val updatedAnswers = currentState.currentAnswers.toMutableMap()
        updatedAnswers[currentQuestion.id] = selectedOptions

        _uiState.value = currentState.copy(
            currentAnswers = updatedAnswers,
            canProceed = validateAnswer(currentQuestion, selectedOptions, textAnswer)
        )

        Timber.d("Question ${currentQuestion.id} answered with ${selectedOptions.size} options")
    }

    private fun validateAnswer(
        question: OnboardingQuestion,
        selectedOptions: List<String>,
        textAnswer: String?
    ): Boolean {
        if (!question.required) return true

        return when (question.type.toKind()) {
            com.ora.wellbeing.data.model.onboarding.QuestionTypeKind.TEXT_INPUT -> {
                !textAnswer.isNullOrBlank()
            }
            com.ora.wellbeing.data.model.onboarding.QuestionTypeKind.PROFILE_GROUP -> {
                // For profile_group, validate that textAnswer (JSON) is not empty
                // and contains all required fields
                if (textAnswer.isNullOrBlank()) return false

                // Parse JSON to check if all required fields are filled
                try {
                    val fields = question.type.fields ?: return false
                    val requiredFields = fields.filter { it.required }

                    // Simple validation: check that JSON contains all required field IDs with non-empty values
                    requiredFields.all { field ->
                        textAnswer.contains("\"${field.id}\":") &&
                        !textAnswer.contains("\"${field.id}\":\"\"")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error validating profile_group answer")
                    false
                }
            }
            else -> {
                selectedOptions.isNotEmpty()
            }
        }
    }

    private fun moveToNextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.totalQuestions - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = nextIndex,
                canProceed = isQuestionAnswered(nextIndex)
            )
            Timber.d("Moved to question $nextIndex")
        }
    }

    private fun moveToPreviousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            val prevIndex = currentState.currentQuestionIndex - 1
            _uiState.value = currentState.copy(
                currentQuestionIndex = prevIndex,
                canProceed = isQuestionAnswered(prevIndex)
            )
            Timber.d("Moved back to question $prevIndex")
        }
    }

    private fun skipCurrentQuestion() {
        val currentQuestion = getCurrentQuestion() ?: return
        if (!currentQuestion.required) {
            moveToNextQuestion()
        }
    }

    private fun completeOnboarding() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = _uiState.value.copy(error = "Vous devez être connecté")
            return
        }

        val configVersion = config?.id ?: run {
            _uiState.value = _uiState.value.copy(error = "Configuration non chargée")
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true)

        val totalTimeSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()

        val response = UserOnboardingResponse(
            uid = uid,
            configVersion = configVersion,
            completed = true,
            completedAt = Timestamp.now(),
            startedAt = Timestamp(startTime / 1000, 0),
            answers = answers.values.toList(),
            metadata = OnboardingMetadata(
                deviceType = "Android ${Build.VERSION.RELEASE}",
                appVersion = BuildConfig.VERSION_NAME,
                totalTimeSeconds = totalTimeSeconds,
                locale = Locale.getDefault().language
            )
        )

        viewModelScope.launch {
            onboardingRepository.saveUserOnboardingResponse(uid, response)
                .onSuccess {
                    // Parse profile_group data and update user profile fields
                    viewModelScope.launch {
                        parseAndUpdateProfileGroupData(uid)
                    }

                    // Update user profile to mark onboarding as completed
                    viewModelScope.launch {
                        userProfileRepository.getUserProfile(uid).collect { profile ->
                            profile?.let {
                                it.hasCompletedOnboarding = true
                                userProfileRepository.updateUserProfile(it)
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isComplete = true
                    )
                    Timber.d("Onboarding completed successfully for user $uid")
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Erreur lors de la sauvegarde : ${error.message}"
                    )
                    Timber.e(error, "Failed to save onboarding response")
                }
        }
    }

    private suspend fun parseAndUpdateProfileGroupData(uid: String) {
        try {
            // Find profile_group question in config
            val profileGroupQuestion = config?.questions?.find { question ->
                question.type.toKind() == QuestionTypeKind.PROFILE_GROUP
            }

            if (profileGroupQuestion == null) {
                Timber.d("No profile_group question found in onboarding config")
                return
            }

            // Find corresponding answer
            val profileGroupAnswer = answers[profileGroupQuestion.id]
            if (profileGroupAnswer == null) {
                Timber.w("No answer found for profile_group question ${profileGroupQuestion.id}")
                return
            }

            // Parse JSON from textAnswer
            val jsonString = profileGroupAnswer.textAnswer
            if (jsonString.isNullOrBlank()) {
                Timber.w("Profile group answer is empty")
                return
            }

            Timber.d("Parsing profile_group JSON: $jsonString")

            // Parse JSON manually (simple JSON parsing for our known structure)
            val firstName = extractJsonValue(jsonString, "firstName")
            val birthDate = extractJsonValue(jsonString, "birthDate")
            val gender = extractJsonValue(jsonString, "gender")

            Timber.d("Extracted profile data - firstName: $firstName, birthDate: $birthDate, gender: $gender")

            // Update user profile with extracted data
            val result = userProfileRepository.updateProfileGroup(
                uid = uid,
                firstName = firstName,
                birthDate = birthDate,
                gender = gender
            )

            result.onSuccess {
                Timber.d("Profile group data updated successfully in users collection")
            }.onFailure { error ->
                Timber.e(error, "Failed to update profile group data")
            }

        } catch (e: Exception) {
            Timber.e(e, "Error parsing profile_group data")
        }
    }

    /**
     * Simple JSON value extractor for our known profile_group structure
     * Extracts value from JSON like: {"firstName":"John","birthDate":"01/01/1990","gender":"male"}
     */
    private fun extractJsonValue(json: String, key: String): String? {
        val pattern = "\"$key\":\"([^\"]*)\""
        val regex = Regex(pattern)
        val matchResult = regex.find(json)
        return matchResult?.groupValues?.getOrNull(1)
    }

    private fun getCurrentQuestion(): OnboardingQuestion? {
        val currentState = _uiState.value
        return currentState.questions.getOrNull(currentState.currentQuestionIndex)
    }

    private fun isQuestionAnswered(index: Int): Boolean {
        val question = _uiState.value.questions.getOrNull(index) ?: return false
        val answer = answers[question.id]

        if (!question.required) return true

        return answer != null && when (question.type.toKind()) {
            com.ora.wellbeing.data.model.onboarding.QuestionTypeKind.TEXT_INPUT -> {
                !answer.textAnswer.isNullOrBlank()
            }
            else -> {
                answer.selectedOptions.isNotEmpty()
            }
        }
    }

    fun getProgress(): Float {
        val currentState = _uiState.value
        if (currentState.totalQuestions == 0) return 0f
        return (currentState.currentQuestionIndex + 1) / currentState.totalQuestions.toFloat()
    }
}

/**
 * UI State for Onboarding
 */
data class OnboardingUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val hasStarted: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null,
    val config: OnboardingConfig? = null,
    val questions: List<OnboardingQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val currentAnswers: Map<String, List<String>> = emptyMap(),
    val canProceed: Boolean = false
) {
    val currentQuestion: OnboardingQuestion?
        get() = questions.getOrNull(currentQuestionIndex)

    val isFirstQuestion: Boolean
        get() = currentQuestionIndex == 0

    val isLastQuestion: Boolean
        get() = currentQuestionIndex == totalQuestions - 1

    val progressPercentage: Int
        get() = if (totalQuestions > 0) {
            ((currentQuestionIndex + 1) * 100 / totalQuestions)
        } else 0
}

/**
 * UI Events for Onboarding
 */
sealed class OnboardingUiEvent {
    object StartOnboarding : OnboardingUiEvent()
    object NextQuestion : OnboardingUiEvent()
    object PreviousQuestion : OnboardingUiEvent()
    data class AnswerQuestion(
        val selectedOptions: List<String>,
        val textAnswer: String? = null
    ) : OnboardingUiEvent()
    object SkipQuestion : OnboardingUiEvent()
    object CompleteOnboarding : OnboardingUiEvent()
    object RetryLoad : OnboardingUiEvent()
}
