package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Onboarding Configuration Model
 * Represents a complete onboarding questionnaire
 * Fetched from Firebase onboarding_configs collection
 */

enum class OnboardingStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}

@IgnoreExtraProperties
class OnboardingConfig {
    var id: String = ""
    var title: String = ""
    var description: String = ""
    var status: String = "draft"
    var version: String = "1.0"
    var questions: List<OnboardingQuestion> = emptyList()
    var createdAt: Timestamp? = null
    var updatedAt: Timestamp? = null
    var createdBy: String = ""
    var publishedAt: Timestamp? = null
    var publishedBy: String? = null

    constructor()

    constructor(
        id: String,
        title: String,
        description: String,
        status: String,
        version: String,
        questions: List<OnboardingQuestion>,
        createdAt: Timestamp?,
        updatedAt: Timestamp?,
        createdBy: String,
        publishedAt: Timestamp? = null,
        publishedBy: String? = null
    ) {
        this.id = id
        this.title = title
        this.description = description
        this.status = status
        this.version = version
        this.questions = questions
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.createdBy = createdBy
        this.publishedAt = publishedAt
        this.publishedBy = publishedBy
    }

    /**
     * Get status enum
     */
    fun getStatusEnum(): OnboardingStatus {
        return when (status.lowercase()) {
            "active" -> OnboardingStatus.ACTIVE
            "draft" -> OnboardingStatus.DRAFT
            "archived" -> OnboardingStatus.ARCHIVED
            else -> OnboardingStatus.DRAFT
        }
    }

    /**
     * Check if config is active
     */
    fun isActive(): Boolean = getStatusEnum() == OnboardingStatus.ACTIVE
}
