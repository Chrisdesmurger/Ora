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
    var inputType: String = "text"
    var placeholder: String? = null
    var maxLength: Int? = null
    var required: Boolean = false
    var options: List<ProfileFieldOption>? = null

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String,
        labelEn: String,
        inputType: String,
        placeholder: String? = null,
        maxLength: Int? = null,
        required: Boolean = false,
        options: List<ProfileFieldOption>? = null
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.inputType = inputType
        this.placeholder = placeholder
        this.maxLength = maxLength
        this.required = required
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
}

@IgnoreExtraProperties
class ProfileFieldOption {
    var id: String = ""
    var label: String = ""
    var labelFr: String = ""
    var labelEn: String = ""
    var icon: String? = null
    var order: Int = 0

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String,
        labelEn: String,
        icon: String? = null,
        order: Int = 0
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.icon = icon
        this.order = order
    }
}
