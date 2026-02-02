package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Question Type Definitions
 * Matches TypeScript types from OraWebApp
 */

enum class QuestionTypeKind {
    MULTIPLE_CHOICE,
    RATING,
    TIME_SELECTION,
    TEXT_INPUT,
    GRID_SELECTION,
    TOGGLE_LIST,
    SLIDER,
    CIRCULAR_PICKER,
    IMAGE_CARD,
    INFORMATION_SCREEN,
    PROFILE_GROUP
}

@IgnoreExtraProperties
class QuestionTypeConfig {
    var kind: String = "multiple_choice"

    // Multiple choice / Grid selection
    var allowMultiple: Boolean? = null
    var minSelections: Int? = null
    var maxSelections: Int? = null
    var gridColumns: Int? = null
    var displayMode: String? = null  // "list" or "grid"

    // Rating
    var showLabels: Boolean? = null

    // Slider
    var sliderMin: Int? = null
    var sliderMax: Int? = null
    var sliderStep: Int? = null
    var sliderUnit: String? = null  // "minutes", "days", etc.

    // Text input
    var maxLines: Int? = null
    var maxCharacters: Int? = null
    var placeholder: String? = null

    // Profile group
    var fields: List<ProfileField>? = null

    // Information screen
    var content: String? = null
    var contentFr: String? = null
    var contentEn: String? = null
    var contentEs: String? = null
    var bulletPoints: List<String>? = null
    var bulletPointsFr: List<String>? = null
    var bulletPointsEn: List<String>? = null
    var bulletPointsEs: List<String>? = null
    var features: List<InformationScreenFeature>? = null
    var ctaText: String? = null
    var ctaTextFr: String? = null
    var ctaTextEn: String? = null
    var ctaTextEs: String? = null
    var backgroundColor: String? = null

    constructor()

    constructor(
        kind: String,
        allowMultiple: Boolean? = null,
        minSelections: Int? = null,
        maxSelections: Int? = null,
        gridColumns: Int? = null,
        displayMode: String? = null,
        showLabels: Boolean? = null,
        sliderMin: Int? = null,
        sliderMax: Int? = null,
        sliderStep: Int? = null,
        sliderUnit: String? = null,
        maxLines: Int? = null,
        maxCharacters: Int? = null,
        placeholder: String? = null
    ) {
        this.kind = kind
        this.allowMultiple = allowMultiple
        this.minSelections = minSelections
        this.maxSelections = maxSelections
        this.gridColumns = gridColumns
        this.displayMode = displayMode
        this.showLabels = showLabels
        this.sliderMin = sliderMin
        this.sliderMax = sliderMax
        this.sliderStep = sliderStep
        this.sliderUnit = sliderUnit
        this.maxLines = maxLines
        this.maxCharacters = maxCharacters
        this.placeholder = placeholder
    }

    @Exclude
    fun toKind(): QuestionTypeKind {
        return when (kind) {
            "multiple_choice" -> QuestionTypeKind.MULTIPLE_CHOICE
            "rating" -> QuestionTypeKind.RATING
            "time_selection" -> QuestionTypeKind.TIME_SELECTION
            "text_input" -> QuestionTypeKind.TEXT_INPUT
            "grid_selection" -> QuestionTypeKind.GRID_SELECTION
            "toggle_list" -> QuestionTypeKind.TOGGLE_LIST
            "slider" -> QuestionTypeKind.SLIDER
            "circular_picker" -> QuestionTypeKind.CIRCULAR_PICKER
            "image_card" -> QuestionTypeKind.IMAGE_CARD
            "information_screen" -> QuestionTypeKind.INFORMATION_SCREEN
            "profile_group" -> QuestionTypeKind.PROFILE_GROUP
            else -> QuestionTypeKind.MULTIPLE_CHOICE
        }
    }
}
