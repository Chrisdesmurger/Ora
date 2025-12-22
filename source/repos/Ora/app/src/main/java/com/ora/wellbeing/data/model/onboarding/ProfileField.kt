package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Profile Field for PROFILE_GROUP question type
 */

enum class ProfileFieldInputType {
    TEXT,
    DATE,
    RADIO
}

@IgnoreExtraProperties
class ProfileField {
    var id: String = ""
    var label: String = ""
    var labelFr: String = ""
    var labelEn: String = ""
    var labelEs: String = ""
    var inputType: String = "text"
    var placeholder: String? = null
    var placeholderFr: String? = null
    var placeholderEn: String? = null
    var placeholderEs: String? = null
    var maxLength: Int? = null
    var required: Boolean = false
    var order: Int = 0
    var options: List<ProfileFieldOption>? = null

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String,
        labelEn: String,
        labelEs: String,
        inputType: String,
        placeholder: String? = null,
        placeholderFr: String? = null,
        placeholderEn: String? = null,
        placeholderEs: String? = null,
        maxLength: Int? = null,
        required: Boolean = false,
        order: Int = 0,
        options: List<ProfileFieldOption>? = null
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.labelEs = labelEs
        this.inputType = inputType
        this.placeholder = placeholder
        this.placeholderFr = placeholderFr
        this.placeholderEn = placeholderEn
        this.placeholderEs = placeholderEs
        this.maxLength = maxLength
        this.required = required
        this.order = order
        this.options = options
    }

    fun getInputTypeEnum(): ProfileFieldInputType {
        return when (inputType) {
            "text" -> ProfileFieldInputType.TEXT
            "date" -> ProfileFieldInputType.DATE
            "radio" -> ProfileFieldInputType.RADIO
            else -> ProfileFieldInputType.TEXT
        }
    }

    fun getLocalizedLabel(locale: String = "fr"): String {
        return when (locale.lowercase()) {
            "en" -> labelEn.takeIf { it.isNotBlank() } ?: label
            "es" -> labelEs.takeIf { it.isNotBlank() } ?: labelEn.takeIf { it.isNotBlank() } ?: label
            else -> labelFr.takeIf { it.isNotBlank() } ?: label
        }
    }

    fun getLocalizedPlaceholder(locale: String = "fr"): String? {
        return when (locale.lowercase()) {
            "en" -> placeholderEn?.takeIf { it.isNotBlank() } ?: placeholder
            "es" -> placeholderEs?.takeIf { it.isNotBlank() } ?: placeholderEn?.takeIf { it.isNotBlank() } ?: placeholder
            else -> placeholderFr?.takeIf { it.isNotBlank() } ?: placeholder
        }
    }
}

@IgnoreExtraProperties
class ProfileFieldOption {
    var id: String = ""
    var label: String = ""
    var labelFr: String = ""
    var labelEn: String = ""
    var labelEs: String = ""
    var icon: String? = null
    var order: Int = 0

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String,
        labelEn: String,
        labelEs: String,
        icon: String? = null,
        order: Int = 0
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.labelEs = labelEs
        this.icon = icon
        this.order = order
    }

    fun getLocalizedLabel(locale: String = "fr"): String {
        return when (locale.lowercase()) {
            "en" -> labelEn.takeIf { it.isNotBlank() } ?: label
            "es" -> labelEs.takeIf { it.isNotBlank() } ?: labelEn.takeIf { it.isNotBlank() } ?: label
            else -> labelFr.takeIf { it.isNotBlank() } ?: label
        }
    }
}
