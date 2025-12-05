package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Information Screen Model
 * Represents an informational screen shown during onboarding at specific positions
 * Fetched from Firebase onboarding_configs collection
 */
@IgnoreExtraProperties
class InformationScreen {
    var position: Int = 0
    var order: Int = 0
    var title: String = ""
    var titleFr: String = ""
    var titleEn: String = ""
    var subtitle: String = ""
    var subtitleFr: String = ""
    var subtitleEn: String = ""
    var content: String = ""
    var contentFr: String = ""
    var contentEn: String = ""
    var bulletPoints: List<String> = emptyList()
    var bulletPointsFr: List<String> = emptyList()
    var bulletPointsEn: List<String> = emptyList()
    var ctaText: String = ""
    var ctaTextFr: String = ""
    var ctaTextEn: String = ""
    var backgroundColor: String = "#F5EFE6"

    constructor()

    constructor(
        position: Int,
        order: Int,
        title: String,
        titleFr: String,
        titleEn: String,
        subtitle: String,
        subtitleFr: String,
        subtitleEn: String,
        content: String,
        contentFr: String,
        contentEn: String,
        bulletPoints: List<String>,
        bulletPointsFr: List<String>,
        bulletPointsEn: List<String>,
        ctaText: String,
        ctaTextFr: String,
        ctaTextEn: String,
        backgroundColor: String
    ) {
        this.position = position
        this.order = order
        this.title = title
        this.titleFr = titleFr
        this.titleEn = titleEn
        this.subtitle = subtitle
        this.subtitleFr = subtitleFr
        this.subtitleEn = subtitleEn
        this.content = content
        this.contentFr = contentFr
        this.contentEn = contentEn
        this.bulletPoints = bulletPoints
        this.bulletPointsFr = bulletPointsFr
        this.bulletPointsEn = bulletPointsEn
        this.ctaText = ctaText
        this.ctaTextFr = ctaTextFr
        this.ctaTextEn = ctaTextEn
        this.backgroundColor = backgroundColor
    }

    /**
     * Get localized title based on current locale
     */
    fun getLocalizedTitle(locale: String = "fr"): String {
        return when (locale) {
            "en" -> titleEn.ifBlank { title }
            else -> titleFr.ifBlank { title }
        }
    }

    /**
     * Get localized subtitle based on current locale
     */
    fun getLocalizedSubtitle(locale: String = "fr"): String {
        return when (locale) {
            "en" -> subtitleEn.ifBlank { subtitle }
            else -> subtitleFr.ifBlank { subtitle }
        }
    }

    /**
     * Get localized content based on current locale
     */
    fun getLocalizedContent(locale: String = "fr"): String {
        return when (locale) {
            "en" -> contentEn.ifBlank { content }
            else -> contentFr.ifBlank { content }
        }
    }

    /**
     * Get localized bullet points based on current locale
     */
    fun getLocalizedBulletPoints(locale: String = "fr"): List<String> {
        return when (locale) {
            "en" -> bulletPointsEn.ifEmpty { bulletPoints }
            else -> bulletPointsFr.ifEmpty { bulletPoints }
        }
    }

    /**
     * Get localized CTA text based on current locale
     */
    fun getLocalizedCtaText(locale: String = "fr"): String {
        return when (locale) {
            "en" -> ctaTextEn.ifBlank { ctaText }
            else -> ctaTextFr.ifBlank { ctaText }
        }
    }
}
