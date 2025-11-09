package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * User Onboarding Response Models
 * Stores user answers to onboarding questions
 * Saved to users/{uid} document under "onboarding" field
 */

@IgnoreExtraProperties
class UserOnboardingAnswer {
    var questionId: String = ""
    var selectedOptions: List<String> = emptyList() // Option IDs
    var textAnswer: String? = null // For text_input questions
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
    var deviceType: String? = null
    var appVersion: String? = null
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
    var configVersion: String = "" // Which config version they completed
    var completed: Boolean = false
    var completedAt: Timestamp? = null
    var startedAt: Timestamp? = null
    var answers: List<UserOnboardingAnswer> = emptyList()
    var metadata: OnboardingMetadata? = null

    // Parsed responses for easy access
    var goals: List<String>? = null
    var mainGoal: String? = null
    var experienceLevels: Map<String, String>? = null
    var dailyTimeCommitment: String? = null
    var preferredTimes: List<String>? = null
    var contentPreferences: List<String>? = null
    var practiceStyle: String? = null
    var challenges: List<String>? = null
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

    /**
     * Parse answers into structured fields for personalization
     */
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
                            // Parse experience levels
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
