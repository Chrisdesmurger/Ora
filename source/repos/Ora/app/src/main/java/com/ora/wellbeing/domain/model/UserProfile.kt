package com.ora.wellbeing.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
// FIX(user-dynamic): Domain model pour le profil utilisateur
// Correspond au schéma Firestore users/{uid} défini dans user_data_contract.yaml
// IMPORTANT: Uses regular class (not data class) for Firestore compatibility
// IMPORTANT: Firestore uses snake_case, so we need @PropertyName annotations
/**
 * Profil utilisateur - entity du domaine
 * Source de vérité: Firestore collection "users"
 *
 * @property uid Firebase Auth UID (immutable, source de vérité)
 * @property firstName Prénom optionnel (PII minimal)
 * @property lastName Nom de famille optionnel
 * @property email Email de l'utilisateur
 * @property photoUrl URL de la photo de profil
 * @property motto Devise/citation personnelle
 * @property planTier Niveau d'abonnement ("free" | "premium")
 * @property createdAt Timestamp de création du compte (epoch ms)
 * @property updatedAt Timestamp de dernière mise à jour (epoch ms)
 * @property locale Locale préférée ("fr" | "en" | null = système)
 */
@IgnoreExtraProperties
class UserProfile {
    // uid is stored as both documentId AND field in Firestore
    var uid: String = ""
    // IMPORTANT: Firestore uses snake_case field names
    @get:PropertyName("first_name")
    @set:PropertyName("first_name")
    var firstName: String? = null
    @get:PropertyName("last_name")
    @set:PropertyName("last_name")
    var lastName: String? = null
    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String? = null
    @get:PropertyName("photo_url")
    @set:PropertyName("photo_url")
    var photoUrl: String? = null
    @get:PropertyName("motto")
    @set:PropertyName("motto")
    var motto: String? = null
    @get:PropertyName("plan_tier")
    @set:PropertyName("plan_tier")
    var planTier: String = "free"
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long? = null
    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Long? = null
    @get:PropertyName("locale")
    @set:PropertyName("locale")
    var locale: String? = null
    
    @get:PropertyName("has_completed_onboarding")
    @set:PropertyName("has_completed_onboarding")
    var hasCompletedOnboarding: Boolean = false
    // No-arg constructor required by Firestore
    constructor()
    // Constructor for easy creation
    constructor(
        uid: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        photoUrl: String? = null,
        motto: String? = null,
        planTier: String = "free",
        createdAt: Long? = null,
        updatedAt: Long? = null,
        locale: String? = null
    ) : this() {
        this.uid = uid
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.photoUrl = photoUrl
        this.motto = motto
        this.planTier = planTier
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.locale = locale
    }
    companion object {
        /**
         * Crée un profil par défaut pour un nouvel utilisateur
         */
        fun createDefault(uid: String, firstName: String? = null, email: String? = null): UserProfile {
            return UserProfile(
                uid = uid,
                firstName = firstName,
                email = email,
                planTier = "free",
                createdAt = System.currentTimeMillis(),
                locale = null
            )
        }
    }

    /**
     * Vérifie si l'utilisateur est premium
     */
    @get:Exclude
    val isPremium: Boolean
        get() = planTier == "premium"

    /**
     * Retourne le nom d'affichage (prénom uniquement ou vide si absent)
     */
    @Exclude
    fun displayName(): String {
        return when {
            !firstName.isNullOrBlank() -> firstName!!
            !lastName.isNullOrBlank() -> lastName!!
            else -> ""
        }
    }

    /**
     * Copy function for immutability pattern
     */
    fun copy(
        uid: String = this.uid,
        firstName: String? = this.firstName,
        lastName: String? = this.lastName,
        email: String? = this.email,
        photoUrl: String? = this.photoUrl,
        motto: String? = this.motto,
        planTier: String = this.planTier,
        createdAt: Long? = this.createdAt,
        updatedAt: Long? = this.updatedAt,
        locale: String? = this.locale,
        hasCompletedOnboarding: Boolean = this.hasCompletedOnboarding
    ): UserProfile {
        val newProfile = UserProfile(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            email = email,
            photoUrl = photoUrl,
            motto = motto,
            planTier = planTier,
            createdAt = createdAt,
            updatedAt = updatedAt,
            locale = locale
        )
        newProfile.hasCompletedOnboarding = hasCompletedOnboarding
        return newProfile
    }
}
