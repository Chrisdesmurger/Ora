package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Onboarding Question Model
 * Matches TypeScript OnboardingQuestion from OraWebApp
 * IMPORTANT: Firestore uses snake_case field names
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

    @get:PropertyName("title_fr")
    @set:PropertyName("title_fr")
    var titleFr: String? = null

    @get:PropertyName("title_en")
    @set:PropertyName("title_en")
    var titleEn: String? = null

    var subtitle: String? = null

    @get:PropertyName("subtitle_fr")
    @set:PropertyName("subtitle_fr")
    var subtitleFr: String? = null

    @get:PropertyName("subtitle_en")
    @set:PropertyName("subtitle_en")
    var subtitleEn: String? = null

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

    fun getLocalizedTitle(locale: String = "fr"): String {
        return when (locale.lowercase()) {
            "fr" -> titleFr ?: title
            "en" -> titleEn ?: title
            else -> title
        }
    }

    fun getLocalizedSubtitle(locale: String = "fr"): String? {
        return when (locale.lowercase()) {
            "fr" -> subtitleFr ?: subtitle
            "en" -> subtitleEn ?: subtitle
            else -> subtitle
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
