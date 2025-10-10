package com.ora.wellbeing.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * FIX(user-dynamic): Modèle Firestore pour le profil utilisateur
 * Stocké dans collection "users"
 *
 * IMPORTANT: Firestore nécessite:
 * - Constructeur sans paramètres (vide)
 * - Propriétés var déclarées HORS du constructeur
 * - @PropertyName sur les getter/setter pour snake_case
 */
@IgnoreExtraProperties
class UserProfile {
    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = ""

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
    var planTier: String = "FREE"

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    @ServerTimestamp
    var createdAt: Date? = null

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    @ServerTimestamp
    var updatedAt: Date? = null
    /**
     * Retourne le nom complet ou "Invité" si vide
     */
    @Exclude
    fun getDisplayName(): String {
        return when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName!!
            !lastName.isNullOrBlank() -> lastName!!
            else -> "Invité"
        }
    }

    /**
     * Retourne le prénom ou "Invité"
     */
    @Exclude
    fun getFirstNameOrGuest(): String {
        return firstName?.takeIf { it.isNotBlank() } ?: "Invité"
    }

    /**
     * Vérifie si le profil est premium
     */
    @Exclude
    fun isPremium(): Boolean {
        return planTier == "PREMIUM" || planTier == "LIFETIME"
    }
}

/**
 * Niveaux d'abonnement
 */
enum class PlanTier {
    FREE,
    PREMIUM,
    LIFETIME;

    fun getDisplayName(): String = when (this) {
        FREE -> "Gratuit"
        PREMIUM -> "Premium"
        LIFETIME -> "Lifetime"
    }
}
