package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Question Type Definitions
 * Matches TypeScript types from OraWebApp
 */

enum class QuestionTypeKind {
    MULTIPLE_CHOICE,
    RATING,
    TIME_SELECTION,
    TEXT_INPUT
}

@IgnoreExtraProperties
class QuestionTypeConfig {
    var kind: String = "multiple_choice"
    var allowMultiple: Boolean? = null
    var minSelections: Int? = null
    var maxSelections: Int? = null

    constructor()

    constructor(
        kind: String,
        allowMultiple: Boolean? = null,
        minSelections: Int? = null,
        maxSelections: Int? = null
    ) {
        this.kind = kind
        this.allowMultiple = allowMultiple
        this.minSelections = minSelections
        this.maxSelections = maxSelections
    }

    fun getKind(): QuestionTypeKind {
        return when (kind) {
            "multiple_choice" -> QuestionTypeKind.MULTIPLE_CHOICE
            "rating" -> QuestionTypeKind.RATING
            "time_selection" -> QuestionTypeKind.TIME_SELECTION
            "text_input" -> QuestionTypeKind.TEXT_INPUT
            else -> QuestionTypeKind.MULTIPLE_CHOICE
        }
    }
}
