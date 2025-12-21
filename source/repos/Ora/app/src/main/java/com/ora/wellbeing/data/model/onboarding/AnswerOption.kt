package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.ora.wellbeing.core.localization.LocalizationProvider

/**
 * Answer Option for Onboarding Questions
 * Supports multi-language labels (FR/EN/ES)
 * IMPORTANT: Firestore uses camelCase field names (consistent with ContentItem)
 */
@IgnoreExtraProperties
class AnswerOption {
    var id: String = ""
    var label: String = ""

    // i18n fields - camelCase (consistent with ContentItem)
    var labelFr: String? = null
    var labelEn: String? = null
    var labelEs: String? = null

    var icon: String? = null // Emoji or icon name
    var color: String? = null // Hex color
    var imageUrl: String? = null

    var order: Int = 0

    // For slider and circular picker types
    var minValue: Int? = null
    var maxValue: Int? = null
    var step: Int? = null
    var unit: String? = null  // "minutes", "days", "hours", etc.
    var value: Int? = null     // Numeric value for this option

    constructor()

    constructor(
        id: String,
        label: String,
        labelFr: String? = null,
        labelEn: String? = null,
        labelEs: String? = null,
        icon: String? = null,
        color: String? = null,
        imageUrl: String? = null,
        order: Int = 0,
        minValue: Int? = null,
        maxValue: Int? = null,
        step: Int? = null,
        unit: String? = null,
        value: Int? = null
    ) {
        this.id = id
        this.label = label
        this.labelFr = labelFr
        this.labelEn = labelEn
        this.labelEs = labelEs
        this.icon = icon
        this.color = color
        this.imageUrl = imageUrl
        this.order = order
        this.minValue = minValue
        this.maxValue = maxValue
        this.step = step
        this.unit = unit
        this.value = value
    }

    /**
     * Get localized label based on user's locale
     */
    fun getLocalizedLabel(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "fr" -> labelFr ?: label
            "en" -> labelEn ?: label
            "es" -> labelEs ?: labelEn ?: label
            else -> labelEn ?: labelFr ?: label
        }
    }
}
