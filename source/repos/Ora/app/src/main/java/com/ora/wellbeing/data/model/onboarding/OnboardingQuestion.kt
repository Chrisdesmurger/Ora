package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.ora.wellbeing.core.localization.LocalizationProvider

/**
 * Onboarding Question Model
 * Matches TypeScript OnboardingQuestion from OraWebApp
 * IMPORTANT: Firestore uses camelCase field names (consistent with ContentItem)
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

    @get:PropertyName("condition_type")
    @set:PropertyName("condition_type")
    var conditionType: String = "equals"

    @get:PropertyName("target_value")
    @set:PropertyName("target_value")
    var targetValue: Any? = null

    @get:PropertyName("next_question_id")
    @set:PropertyName("next_question_id")
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

    // i18n fields - camelCase (consistent with ContentItem)
    var titleFr: String? = null
    var titleEn: String? = null
    var titleEs: String? = null

    var subtitle: String? = null

    // i18n fields - camelCase
    var subtitleFr: String? = null
    var subtitleEn: String? = null
    var subtitleEs: String? = null

    var type: QuestionTypeConfig = QuestionTypeConfig()
    var options: List<AnswerOption> = emptyList()
    var required: Boolean = true

    @get:PropertyName("skip_logic")
    @set:PropertyName("skip_logic")
    var skipLogic: SkipLogic? = null

    constructor()

    constructor(
        id: String,
        category: String,
        order: Int,
        title: String,
        titleFr: String? = null,
        titleEn: String? = null,
        titleEs: String? = null,
        subtitle: String? = null,
        subtitleFr: String? = null,
        subtitleEn: String? = null,
        subtitleEs: String? = null,
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
        this.titleEs = titleEs
        this.subtitle = subtitle
        this.subtitleFr = subtitleFr
        this.subtitleEn = subtitleEn
        this.subtitleEs = subtitleEs
        this.type = type
        this.options = options
        this.required = required
        this.skipLogic = skipLogic
    }

    fun getLocalizedTitle(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "fr" -> titleFr ?: title
            "en" -> titleEn ?: title
            "es" -> titleEs ?: titleEn ?: title
            else -> titleEn ?: titleFr ?: title
        }
    }

    fun getLocalizedSubtitle(locale: String = LocalizationProvider.DEFAULT_LOCALE): String? {
        return when (locale.lowercase()) {
            "fr" -> subtitleFr ?: subtitle
            "en" -> subtitleEn ?: subtitle
            "es" -> subtitleEs ?: subtitleEn ?: subtitle
            else -> subtitleEn ?: subtitleFr ?: subtitle
        }
    }

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
