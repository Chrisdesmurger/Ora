package com.ora.wellbeing.presentation.screens.profile.validation

/**
 * Validation rules for profile editing form fields
 */
object ValidationRules {

    private const val MIN_NAME_LENGTH = 2
    private const val MAX_NAME_LENGTH = 50
    private const val MAX_BIO_LENGTH = 200

    // Regex for names: letters, spaces, hyphens, and accented characters
    private val NAME_REGEX = Regex("^[a-zA-ZÀ-ÿ\\s-]+$")

    /**
     * Validates first name
     * Rules: Required, 2-50 chars, letters/spaces/hyphens only
     */
    fun validateFirstName(firstName: String?): ValidationResult {
        return when {
            firstName.isNullOrBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Le prénom est obligatoire"
            )
            firstName.length < MIN_NAME_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Le prénom doit contenir au moins $MIN_NAME_LENGTH caractères"
            )
            firstName.length > MAX_NAME_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Le prénom ne peut pas dépasser $MAX_NAME_LENGTH caractères"
            )
            !NAME_REGEX.matches(firstName) -> ValidationResult(
                isValid = false,
                errorMessage = "Le prénom ne peut contenir que des lettres, espaces et tirets"
            )
            else -> ValidationResult(isValid = true, errorMessage = null)
        }
    }

    /**
     * Validates last name
     * Rules: Required, 2-50 chars, letters/spaces/hyphens only
     */
    fun validateLastName(lastName: String?): ValidationResult {
        return when {
            lastName.isNullOrBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Le nom est obligatoire"
            )
            lastName.length < MIN_NAME_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Le nom doit contenir au moins $MIN_NAME_LENGTH caractères"
            )
            lastName.length > MAX_NAME_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Le nom ne peut pas dépasser $MAX_NAME_LENGTH caractères"
            )
            !NAME_REGEX.matches(lastName) -> ValidationResult(
                isValid = false,
                errorMessage = "Le nom ne peut contenir que des lettres, espaces et tirets"
            )
            else -> ValidationResult(isValid = true, errorMessage = null)
        }
    }

    /**
     * Validates bio/motto
     * Rules: Optional, max 200 chars
     */
    fun validateBio(bio: String?): ValidationResult {
        return when {
            bio == null -> ValidationResult(isValid = true, errorMessage = null)
            bio.length > MAX_BIO_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "La devise ne peut pas dépasser $MAX_BIO_LENGTH caractères"
            )
            else -> ValidationResult(isValid = true, errorMessage = null)
        }
    }
}

/**
 * Result of a validation check
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String?
)
