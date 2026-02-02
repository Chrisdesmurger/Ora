package com.ora.wellbeing.data.model.onboarding

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Onboarding Configuration Model
 * Represents a complete onboarding questionnaire
 * Fetched from Firebase onboarding_configs collection
 * IMPORTANT: Firestore uses snake_case field names
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
    var informationScreens: List<InformationScreen> = emptyList()

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp? = null

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Timestamp? = null

    @get:PropertyName("created_by")
    @set:PropertyName("created_by")
    var createdBy: String = ""

    @get:PropertyName("published_at")
    @set:PropertyName("published_at")
    var publishedAt: Timestamp? = null

    @get:PropertyName("published_by")
    @set:PropertyName("published_by")
    var publishedBy: String? = null

    constructor()

    constructor(
        id: String,
        title: String,
        description: String,
        status: String,
        version: String,
        questions: List<OnboardingQuestion>,
        informationScreens: List<InformationScreen>,
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
        this.informationScreens = informationScreens
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.createdBy = createdBy
        this.publishedAt = publishedAt
        this.publishedBy = publishedBy
    }

    fun getStatusEnum(): OnboardingStatus {
        return when (status.lowercase()) {
            "active" -> OnboardingStatus.ACTIVE
            "draft" -> OnboardingStatus.DRAFT
            "archived" -> OnboardingStatus.ARCHIVED
            else -> OnboardingStatus.DRAFT
        }
    }

    fun isActive(): Boolean = getStatusEnum() == OnboardingStatus.ACTIVE
}
