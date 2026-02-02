package com.ora.wellbeing.presentation.screens.profile

import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.presentation.screens.profile.validation.ProfileField

/**
 * UI State for Profile Edit Screen
 */
data class ProfileEditUiState(
    // Current profile data
    val profile: UserProfile? = null,

    // Form state
    val firstName: String = "",
    val lastName: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val gender: String? = null,
    val language: String = "fr", // Default to French
    val notificationsEnabled: Boolean = true,
    val eveningReminderEnabled: Boolean = true,

    // Loading states
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,

    // Upload progress (0-100)
    val uploadProgress: Int = 0,

    // Validation errors (field -> error message)
    val validationErrors: Map<ProfileField, String> = emptyMap(),

    // Track unsaved changes
    val hasUnsavedChanges: Boolean = false,

    // Success/Error messages
    val errorMessage: String? = null,
    val successMessage: String? = null
)
