package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Answer Option for Onboarding Questions
 * Supports multi-language labels (French/English)
 */
@IgnoreExtraProperties
class AnswerOption {
    var id: String = ""
    var label: String = ""
    var labelFr: String? = null
    var labelEn: String? = null
    var icon: String? = null // Emoji or icon name
    var color: String? = null // Hex color
    var imageUrl: String? = null
    var order: Int = 0

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String? = null,
        labelEn: String? = null,
        icon: String? = null,
        color: String? = null,
        imageUrl: String? = null,
        order: Int = 0
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.icon = icon
        this.color = color
        this.imageUrl = imageUrl
        this.order = order
    }

    /**
     * Get localized label based on user's locale
     */
    fun getLocalizedLabel(locale: String = "fr"): String {
        return when (locale.lowercase()) {
            "fr" -> labelFr ?: label
            "en" -> labelEn ?: label
            else -> label
        }
    }
}
