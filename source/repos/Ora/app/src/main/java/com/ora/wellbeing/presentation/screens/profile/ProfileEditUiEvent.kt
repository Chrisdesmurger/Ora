package com.ora.wellbeing.presentation.screens.profile

import android.net.Uri

/**
 * UI Events for Profile Edit Screen
 */
sealed class ProfileEditUiEvent {
    data class UpdateFirstName(val firstName: String) : ProfileEditUiEvent()
    data class UpdateLastName(val lastName: String) : ProfileEditUiEvent()
    data class UpdateBio(val bio: String) : ProfileEditUiEvent()
    data class UpdateGender(val gender: String?) : ProfileEditUiEvent()
    data class ChangeLanguage(val language: String) : ProfileEditUiEvent()
    data class ToggleNotifications(val enabled: Boolean) : ProfileEditUiEvent()
    data class ToggleEveningReminder(val enabled: Boolean) : ProfileEditUiEvent()
    data class UploadPhoto(val uri: Uri) : ProfileEditUiEvent()
    object RemovePhoto : ProfileEditUiEvent()
    object Save : ProfileEditUiEvent()
    object SignOut : ProfileEditUiEvent()
    object NavigateBack : ProfileEditUiEvent()
    object DismissError : ProfileEditUiEvent()
    object DismissSuccess : ProfileEditUiEvent()
}
