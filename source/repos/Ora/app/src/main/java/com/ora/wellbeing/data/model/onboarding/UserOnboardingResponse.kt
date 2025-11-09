package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * User Onboarding Response Models
 * Stores user answers to onboarding questions
 * Saved to users/{uid} document under "onboarding" field
 * IMPORTANT: Firestore uses snake_case field names
 */

@IgnoreExtraProperties
class UserOnboardingAnswer {
    @get:PropertyName("question_id")
    @set:PropertyName("question_id")
    var questionId: String = ""

    @get:PropertyName("selected_options")
    @set:PropertyName("selected_options")
    var selectedOptions: List<String> = emptyList()

    @get:PropertyName("text_answer")
    @set:PropertyName("text_answer")
    var textAnswer: String? = null

    @get:PropertyName("answered_at")
    @set:PropertyName("answered_at")
    var answeredAt: Timestamp? = null

    constructor()

    constructor(
        questionId: String,
        selectedOptions: List<String>,
        textAnswer: String? = null,
        answeredAt: Timestamp? = null
    ) {
        this.questionId = questionId
        this.selectedOptions = selectedOptions
        this.textAnswer = textAnswer
        this.answeredAt = answeredAt
    }
}

@IgnoreExtraProperties
class OnboardingMetadata {
    @get:PropertyName("device_type")
    @set:PropertyName("device_type")
    var deviceType: String? = null

    @get:PropertyName("app_version")
    @set:PropertyName("app_version")
    var appVersion: String? = null

    @get:PropertyName("total_time_seconds")
    @set:PropertyName("total_time_seconds")
    var totalTimeSeconds: Int? = null

    var locale: String? = null

    constructor()

    constructor(
        deviceType: String?,
        appVersion: String?,
        totalTimeSeconds: Int?,
        locale: String?
    ) {
        this.deviceType = deviceType
        this.appVersion = appVersion
        this.totalTimeSeconds = totalTimeSeconds
        this.locale = locale
    }
}

@IgnoreExtraProperties
class UserOnboardingResponse {
    var uid: String = ""

    @get:PropertyName("config_version")
    @set:PropertyName("config_version")
    var configVersion: String = ""

    var completed: Boolean = false

    @get:PropertyName("completed_at")
    @set:PropertyName("completed_at")
    var completedAt: Timestamp? = null

    @get:PropertyName("started_at")
    @set:PropertyName("started_at")
    var startedAt: Timestamp? = null

    var answers: List<UserOnboardingAnswer> = emptyList()
    var metadata: OnboardingMetadata? = null

    // Parsed responses for easy access
    var goals: List<String>? = null

    @get:PropertyName("main_goal")
    @set:PropertyName("main_goal")
    var mainGoal: String? = null

    @get:PropertyName("experience_levels")
    @set:PropertyName("experience_levels")
    var experienceLevels: Map<String, String>? = null

    @get:PropertyName("daily_time_commitment")
    @set:PropertyName("daily_time_commitment")
    var dailyTimeCommitment: String? = null

    @get:PropertyName("preferred_times")
    @set:PropertyName("preferred_times")
    var preferredTimes: List<String>? = null

    @get:PropertyName("content_preferences")
    @set:PropertyName("content_preferences")
    var contentPreferences: List<String>? = null

    @get:PropertyName("practice_style")
    @set:PropertyName("practice_style")
    var practiceStyle: String? = null

    var challenges: List<String>? = null

    @get:PropertyName("support_preferences")
    @set:PropertyName("support_preferences")
    var supportPreferences: List<String>? = null

    constructor()

    constructor(
        uid: String,
        configVersion: String,
        completed: Boolean = false,
        completedAt: Timestamp? = null,
        startedAt: Timestamp? = null,
        answers: List<UserOnboardingAnswer> = emptyList(),
        metadata: OnboardingMetadata? = null
    ) {
        this.uid = uid
        this.configVersion = configVersion
        this.completed = completed
        this.completedAt = completedAt
        this.startedAt = startedAt
        this.answers = answers
        this.metadata = metadata
    }

    fun parseAnswers(config: OnboardingConfig) {
        val answersMap = answers.associateBy { it.questionId }

        config.questions.forEach { question ->
            val answer = answersMap[question.id]
            if (answer != null) {
                when (question.getCategoryEnum()) {
                    QuestionCategory.GOALS -> {
                        if (question.title.contains("brings you", ignoreCase = true)) {
                            goals = answer.selectedOptions
                        } else if (question.title.contains("main", ignoreCase = true)) {
                            mainGoal = answer.selectedOptions.firstOrNull()
                        }
                    }
                    QuestionCategory.EXPERIENCE -> {
                        if (question.title.contains("familiar", ignoreCase = true)) {
                            val levels = mutableMapOf<String, String>()
                            answer.selectedOptions.forEach { optionId ->
                                val option = question.options.find { it.id == optionId }
                                option?.let {
                                    levels[it.label] = it.label
                                }
                            }
                            experienceLevels = levels
                        } else if (question.title.contains("time", ignoreCase = true)) {
                            dailyTimeCommitment = answer.selectedOptions.firstOrNull()
                        }
                    }
                    QuestionCategory.PREFERENCES -> {
                        if (question.title.contains("time of day", ignoreCase = true)) {
                            preferredTimes = answer.selectedOptions
                        } else if (question.title.contains("content", ignoreCase = true)) {
                            contentPreferences = answer.selectedOptions
                        } else if (question.title.contains("style", ignoreCase = true)) {
                            practiceStyle = answer.selectedOptions.firstOrNull()
                        }
                    }
                    QuestionCategory.PERSONALIZATION -> {
                        if (question.title.contains("challenges", ignoreCase = true)) {
                            challenges = answer.selectedOptions
                        } else if (question.title.contains("support", ignoreCase = true)) {
                            supportPreferences = answer.selectedOptions
                        }
                    }
                }
            }
        }
    }
}
