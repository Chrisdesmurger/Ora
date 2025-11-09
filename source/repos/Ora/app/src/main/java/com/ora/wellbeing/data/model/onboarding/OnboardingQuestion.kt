package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Onboarding Question Model
 * Matches TypeScript OnboardingQuestion from OraWebApp
 */

enum class QuestionCategory {
    GOALS,
    EXPERIENCE,
    PREFERENCES,
    PERSONALIZATION
}

@IgnoreExtraProperties
class SkipLogic {
    var condition: String = ""
    var conditionType: String = "equals" // contains, equals, not_contains, not_equals
    var targetValue: Any? = null // String or List<String>
    var nextQuestionId: String = ""

    constructor()

    constructor(
        condition: String,
        conditionType: String,
        targetValue: Any?,
        nextQuestionId: String
    ) {
        this.condition = condition
        this.conditionType = conditionType
        this.targetValue = targetValue
        this.nextQuestionId = nextQuestionId
    }
}

@IgnoreExtraProperties
class OnboardingQuestion {
    var id: String = ""
    var category: String = "goals"
    var order: Int = 0
    var title: String = ""
    var titleFr: String? = null
    var titleEn: String? = null
    var subtitle: String? = null
    var subtitleFr: String? = null
    var subtitleEn: String? = null
    var type: QuestionTypeConfig = QuestionTypeConfig()
    var options: List<AnswerOption> = emptyList()
    var required: Boolean = true
    var skipLogic: SkipLogic? = null

    constructor()

    constructor(
        id: String,
        category: String,
        order: Int,
        title: String,
        titleFr: String? = null,
        titleEn: String? = null,
        subtitle: String? = null,
        subtitleFr: String? = null,
        subtitleEn: String? = null,
        type: QuestionTypeConfig,
        options: List<AnswerOption>,
        required: Boolean = true,
        skipLogic: SkipLogic? = null
    ) {
        this.id = id
        this.category = category
        this.order = order
        this.title = title
        this.titleFr = titleFr
        this.titleEn = titleEn
        this.subtitle = subtitle
        this.subtitleFr = subtitleFr
        this.subtitleEn = subtitleEn
        this.type = type
        this.options = options
        this.required = required
        this.skipLogic = skipLogic
    }

    /**
     * Get localized title based on user's locale
     */
    fun getLocalizedTitle(locale: String = "fr"): String {
        return when (locale.lowercase()) {
            "fr" -> titleFr ?: title
            "en" -> titleEn ?: title
            else -> title
        }
    }

    /**
     * Get localized subtitle based on user's locale
     */
    fun getLocalizedSubtitle(locale: String = "fr"): String? {
        return when (locale.lowercase()) {
            "fr" -> subtitleFr ?: subtitle
            "en" -> subtitleEn ?: subtitle
            else -> subtitle
        }
    }

    /**
     * Get category enum
     */
    fun getCategoryEnum(): QuestionCategory {
        return when (category.lowercase()) {
            "goals" -> QuestionCategory.GOALS
            "experience" -> QuestionCategory.EXPERIENCE
            "preferences" -> QuestionCategory.PREFERENCES
            "personalization" -> QuestionCategory.PERSONALIZATION
            else -> QuestionCategory.GOALS
        }
    }
}
