package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.firestore.IgnoreExtraProperties
import com.ora.wellbeing.core.localization.LocalizationProvider

/**
 * Information Screen Feature
 * Represents a feature displayed in an information screen
 */
@IgnoreExtraProperties
class InformationScreenFeature {
    var icon: String? = null
    var title: String = ""
    var titleFr: String = ""
    var titleEn: String = ""
    var titleEs: String = ""
    var description: String = ""
    var descriptionFr: String = ""
    var descriptionEn: String = ""
    var descriptionEs: String = ""
    var order: Int = 0

    constructor()

    constructor(
        icon: String?,
        title: String,
        titleFr: String,
        titleEn: String,
        titleEs: String,
        description: String,
        descriptionFr: String,
        descriptionEn: String,
        descriptionEs: String,
        order: Int
    ) {
        this.icon = icon
        this.title = title
        this.titleFr = titleFr
        this.titleEn = titleEn
        this.titleEs = titleEs
        this.description = description
        this.descriptionFr = descriptionFr
        this.descriptionEn = descriptionEn
        this.descriptionEs = descriptionEs
        this.order = order
    }

    fun getLocalizedTitle(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> titleEn.ifBlank { title }
            "es" -> titleEs.ifBlank { titleEn.ifBlank { title } }
            else -> titleFr.ifBlank { title }
        }
    }

    fun getLocalizedDescription(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> descriptionEn.ifBlank { description }
            "es" -> descriptionEs.ifBlank { descriptionEn.ifBlank { description } }
            else -> descriptionFr.ifBlank { description }
        }
    }
}

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
    var titleEs: String = ""
    var subtitle: String = ""
    var subtitleFr: String = ""
    var subtitleEn: String = ""
    var subtitleEs: String = ""
    var content: String = ""
    var contentFr: String = ""
    var contentEn: String = ""
    var contentEs: String = ""
    var bulletPoints: List<String> = emptyList()
    var bulletPointsFr: List<String> = emptyList()
    var bulletPointsEn: List<String> = emptyList()
    var bulletPointsEs: List<String> = emptyList()
    var ctaText: String = ""
    var ctaTextFr: String = ""
    var ctaTextEn: String = ""
    var ctaTextEs: String = ""
    var backgroundColor: String = "#F5EFE6"
    var features: List<InformationScreenFeature> = emptyList()

    constructor()

    constructor(
        position: Int,
        order: Int,
        title: String,
        titleFr: String,
        titleEn: String,
        titleEs: String,
        subtitle: String,
        subtitleFr: String,
        subtitleEn: String,
        subtitleEs: String,
        content: String,
        contentFr: String,
        contentEn: String,
        contentEs: String,
        bulletPoints: List<String>,
        bulletPointsFr: List<String>,
        bulletPointsEn: List<String>,
        bulletPointsEs: List<String>,
        ctaText: String,
        ctaTextFr: String,
        ctaTextEn: String,
        ctaTextEs: String,
        backgroundColor: String
    ) {
        this.position = position
        this.order = order
        this.title = title
        this.titleFr = titleFr
        this.titleEn = titleEn
        this.titleEs = titleEs
        this.subtitle = subtitle
        this.subtitleFr = subtitleFr
        this.subtitleEn = subtitleEn
        this.subtitleEs = subtitleEs
        this.content = content
        this.contentFr = contentFr
        this.contentEn = contentEn
        this.contentEs = contentEs
        this.bulletPoints = bulletPoints
        this.bulletPointsFr = bulletPointsFr
        this.bulletPointsEn = bulletPointsEn
        this.bulletPointsEs = bulletPointsEs
        this.ctaText = ctaText
        this.ctaTextFr = ctaTextFr
        this.ctaTextEn = ctaTextEn
        this.ctaTextEs = ctaTextEs
        this.backgroundColor = backgroundColor
    }

    /**
     * Get localized title based on current locale
     */
    fun getLocalizedTitle(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> titleEn.ifBlank { title }
            "es" -> titleEs.ifBlank { titleEn.ifBlank { title } }
            else -> titleFr.ifBlank { title }
        }
    }

    /**
     * Get localized subtitle based on current locale
     */
    fun getLocalizedSubtitle(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> subtitleEn.ifBlank { subtitle }
            "es" -> subtitleEs.ifBlank { subtitleEn.ifBlank { subtitle } }
            else -> subtitleFr.ifBlank { subtitle }
        }
    }

    /**
     * Get localized content based on current locale
     */
    fun getLocalizedContent(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> contentEn.ifBlank { content }
            "es" -> contentEs.ifBlank { contentEn.ifBlank { content } }
            else -> contentFr.ifBlank { content }
        }
    }

    /**
     * Get localized bullet points based on current locale
     */
    fun getLocalizedBulletPoints(locale: String = LocalizationProvider.DEFAULT_LOCALE): List<String> {
        return when (locale.lowercase()) {
            "en" -> bulletPointsEn.ifEmpty { bulletPoints }
            "es" -> bulletPointsEs.ifEmpty { bulletPointsEn.ifEmpty { bulletPoints } }
            else -> bulletPointsFr.ifEmpty { bulletPoints }
        }
    }

    /**
     * Get localized CTA text based on current locale
     */
    fun getLocalizedCtaText(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale.lowercase()) {
            "en" -> ctaTextEn.ifBlank { ctaText }
            "es" -> ctaTextEs.ifBlank { ctaTextEn.ifBlank { ctaText } }
            else -> ctaTextFr.ifBlank { ctaText }
        }
    }
}
