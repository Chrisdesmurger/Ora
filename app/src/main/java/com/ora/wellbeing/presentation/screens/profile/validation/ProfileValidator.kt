package com.ora.wellbeing.presentation.screens.profile.validation

/**
 * Validator for the entire profile editing form
 */
class ProfileValidator {

    /**
     * Validates all form fields and returns a map of field -> error message
     * Only includes entries for fields with errors
     */
    fun validateProfile(
        firstName: String?,
        lastName: String?,
        bio: String?
    ): Map<ProfileField, String> {
        val errors = mutableMapOf<ProfileField, String>()

        // Validate first name
        val firstNameResult = ValidationRules.validateFirstName(firstName)
        if (!firstNameResult.isValid && firstNameResult.errorMessage != null) {
            errors[ProfileField.FIRST_NAME] = firstNameResult.errorMessage
        }

        // Validate last name
        val lastNameResult = ValidationRules.validateLastName(lastName)
        if (!lastNameResult.isValid && lastNameResult.errorMessage != null) {
            errors[ProfileField.LAST_NAME] = lastNameResult.errorMessage
        }

        // Validate bio
        val bioResult = ValidationRules.validateBio(bio)
        if (!bioResult.isValid && bioResult.errorMessage != null) {
            errors[ProfileField.BIO] = bioResult.errorMessage
        }

        return errors
    }

    /**
     * Checks if the form is valid (no errors)
     */
    fun isValid(
        firstName: String?,
        lastName: String?,
        bio: String?
    ): Boolean {
        return validateProfile(firstName, lastName, bio).isEmpty()
    }
}

/**
 * Enum representing profile form fields
 */
enum class ProfileField {
    FIRST_NAME,
    LAST_NAME,
    BIO,
    PHOTO
}
