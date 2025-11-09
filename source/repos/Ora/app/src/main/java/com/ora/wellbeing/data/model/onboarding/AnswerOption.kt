package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Answer Option for Onboarding Questions
 * Supports multi-language labels (French/English)
 * IMPORTANT: Firestore uses snake_case field names
 */
@IgnoreExtraProperties
class AnswerOption {
    var id: String = ""
    var label: String = ""

    @get:PropertyName("label_fr")
    @set:PropertyName("label_fr")
    var labelFr: String? = null

    @get:PropertyName("label_en")
    @set:PropertyName("label_en")
    var labelEn: String? = null

    var icon: String? = null // Emoji or icon name
    var color: String? = null // Hex color

    @get:PropertyName("image_url")
    @set:PropertyName("image_url")
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
