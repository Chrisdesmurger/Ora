package com.ora.wellbeing.presentation.screens.onboarding

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.ora.wellbeing.BuildConfig
import com.ora.wellbeing.R
import com.ora.wellbeing.core.localization.LocalizationProvider
import com.ora.wellbeing.data.model.onboarding.OnboardingConfig
import com.ora.wellbeing.data.model.onboarding.OnboardingMetadata
import com.ora.wellbeing.data.model.onboarding.OnboardingQuestion
import com.ora.wellbeing.data.model.onboarding.QuestionTypeKind
import com.ora.wellbeing.data.model.onboarding.UserOnboardingAnswer
import com.ora.wellbeing.data.model.onboarding.UserOnboardingResponse
import com.ora.wellbeing.data.repository.OnboardingRepository
import com.ora.wellbeing.data.service.EmailNotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Onboarding Flow
 * Manages question navigation, answer collection, and response persistence
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val userProfileRepository: com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository,
    private val auth: FirebaseAuth,
    private val localizationProvider: LocalizationProvider,
    private val emailNotificationService: EmailNotificationService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(currentLocale = localizationProvider.getCurrentLocale())
    )
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

                    // Merge questions and information screens
                    val allQuestions = mergeQuestionsAndInformationScreens(
                        loadedConfig.questions,
                        loadedConfig.informationScreens
                    )

                    _uiState.value = OnboardingUiState(
                        isLoading = false,
                        config = loadedConfig,
                        questions = allQuestions,
                        currentQuestionIndex = 0,
                        totalQuestions = allQuestions.size,
                        isComplete = false,
                        currentLocale = localizationProvider.getCurrentLocale()
                    )

                    Timber.d("Onboarding config loaded: ${loadedConfig.questions.size} questions + ${loadedConfig.informationScreens.size} information screens = ${allQuestions.size} total items")
                }
                .onFailure { error ->
                    // Note: Error messages with dynamic data stay in ViewModel
                    // Static error strings are shown via UI from strings.xml
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = context.getString(
                            R.string.onboarding_error_load_config_with_reason,
                            error.message ?: ""
                        )
                    )
                    Timber.e(error, "Failed to load onboarding config")
                }
        }
    }

    /**
     * Merge questions and information screens into a single sorted list
     * Information screens are inserted at their specified positions
     */
    private fun mergeQuestionsAndInformationScreens(
        questions: List<OnboardingQuestion>,
        informationScreens: List<com.ora.wellbeing.data.model.onboarding.InformationScreen>
    ): List<OnboardingQuestion> {
        // Convert information screens to virtual questions
        val virtualQuestions = informationScreens.map { screen ->
            convertInformationScreenToQuestion(screen)
        }

        // Combine all items and sort by order
        val allItems = (questions + virtualQuestions).sortedBy { it.order }

        Timber.d("Merged ${questions.size} questions + ${informationScreens.size} info screens = ${allItems.size} total")

        return allItems
    }

    /**
     * Convert an InformationScreen to a virtual OnboardingQuestion
     * The UI already handles INFORMATION_SCREEN type questions
     */
    private fun convertInformationScreenToQuestion(
        screen: com.ora.wellbeing.data.model.onboarding.InformationScreen
    ): OnboardingQuestion {
        return OnboardingQuestion(
            id = "info_screen_${screen.position}",
            category = "information",
            order = screen.order,
            title = screen.title,
            titleFr = screen.titleFr,
            titleEn = screen.titleEn,
            titleEs = screen.titleEs,
            subtitle = screen.subtitle,
            subtitleFr = screen.subtitleFr,
            subtitleEn = screen.subtitleEn,
            subtitleEs = screen.subtitleEs,
            type = com.ora.wellbeing.data.model.onboarding.QuestionTypeConfig().apply {
                kind = "information_screen"
                content = screen.content
                contentFr = screen.contentFr
                contentEn = screen.contentEn
                contentEs = screen.contentEs
                bulletPoints = screen.bulletPoints
                bulletPointsFr = screen.bulletPointsFr
                bulletPointsEn = screen.bulletPointsEn
                bulletPointsEs = screen.bulletPointsEs
                features = screen.features
                ctaText = screen.ctaText
                ctaTextFr = screen.ctaTextFr
                ctaTextEn = screen.ctaTextEn
                ctaTextEs = screen.ctaTextEs
                backgroundColor = screen.backgroundColor
            },
            options = emptyList(),
            required = false // Information screens don't require user input
        )
    }

    private fun startOnboarding() {
        val uid = auth.currentUser?.uid ?: run {
            // Static error string - matches R.string.error_must_be_authenticated
            _uiState.value = _uiState.value.copy(
                error = context.getString(R.string.error_must_be_authenticated)
            )
            return
        }

        val configVersion = config?.id ?: run {
            // Static error string - matches R.string.error_config_not_loaded
            _uiState.value = _uiState.value.copy(error = context.getString(R.string.error_config_not_loaded))
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
            com.ora.wellbeing.data.model.onboarding.QuestionTypeKind.INFORMATION_SCREEN -> {
                // Information screens are auto-acknowledged, always valid
                true
            }
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
            // Static error string - matches R.string.error_must_be_authenticated
            _uiState.value = _uiState.value.copy(error = "context.getString(R.string.error_must_be_authenticated) // replaced")
            return
        }

        val configVersion = config?.id ?: run {
            // Static error string - matches R.string.error_config_not_loaded
            _uiState.value = _uiState.value.copy(error = context.getString(R.string.error_config_not_loaded))
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
                    locale = localizationProvider.getCurrentLocale()
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

                    // Send onboarding complete email (silent failure - does not block flow)
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val email = auth.currentUser?.email
                            email?.let {
                                val extractedFirstName = extractFirstNameFromAnswers()
                                val recommendations = extractRecommendationsFromAnswers()

                                Timber.d("Sending onboarding complete email to $it (firstName: $extractedFirstName, recommendations: $recommendations)")

                                emailNotificationService.sendOnboardingCompleteEmail(
                                    uid = uid,
                                    email = it,
                                    firstName = extractedFirstName,
                                    recommendations = recommendations
                                )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to send onboarding complete email")
                        }
                    }
                }
                .onFailure { error ->
                    // Error message with dynamic data stays in ViewModel
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Erreur lors de la sauvegarde : ${error.message}"
                    )
                    Timber.e(error, "Failed to save onboarding response")
                }
        }
    }

    /**
     * Extract firstName from profile_group answer
     */
    private fun extractFirstNameFromAnswers(): String? {
        val profileGroupQuestion = config?.questions?.find { question ->
            question.type.toKind() == QuestionTypeKind.PROFILE_GROUP
        }

        if (profileGroupQuestion == null) {
            return null
        }

        val profileGroupAnswer = answers[profileGroupQuestion.id]
        val jsonString = profileGroupAnswer?.textAnswer ?: return null

        return extractJsonValue(jsonString, "firstName")
    }

    /**
     * Extract recommendations from onboarding answers
     * Uses selected options from goals/interests questions
     */
    private fun extractRecommendationsFromAnswers(): List<String> {
        val recommendations = mutableListOf<String>()

        // Collect all selected options from multi-choice questions
        // that are likely goal/interest related (categories: goals, interests, preferences)
        val relevantCategories = listOf("goals", "interests", "preferences", "objectifs", "needs")

        config?.questions?.forEach { question ->
            if (question.category.lowercase() in relevantCategories) {
                val answer = answers[question.id]
                answer?.selectedOptions?.let { options ->
                    recommendations.addAll(options)
                }
            }
        }

        // Also include any selected options from single/multi choice questions
        // if no specific categories were found
        if (recommendations.isEmpty()) {
            answers.values.forEach { answer ->
                if (answer.selectedOptions.isNotEmpty()) {
                    recommendations.addAll(answer.selectedOptions)
                }
            }
        }

        return recommendations.distinct().take(5) // Limit to 5 recommendations
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

        // Information screens are always considered "answered"
        if (question.type.toKind() == com.ora.wellbeing.data.model.onboarding.QuestionTypeKind.INFORMATION_SCREEN) {
            return true
        }

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
    val currentLocale: String = LocalizationProvider.DEFAULT_LOCALE,
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
