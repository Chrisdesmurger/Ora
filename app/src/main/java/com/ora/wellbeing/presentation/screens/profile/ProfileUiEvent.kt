package com.ora.wellbeing.presentation.screens.profile

/**
 * Événements de l'interface utilisateur pour l'écran Profile
 * FIX(user-dynamic): Ajout d'événements pour modifier profil/photo
 */
sealed interface ProfileUiEvent {
    data object LoadProfileData : ProfileUiEvent
    data object NavigateToEditProfile : ProfileUiEvent
    data class NavigateToPracticeStats(val practiceId: String) : ProfileUiEvent
    data class ToggleGoal(val goalId: String) : ProfileUiEvent
    data object NavigateToGratitudes : ProfileUiEvent

    // FIX(user-dynamic): Nouveaux événements pour mise à jour profil
    data class UpdateMotto(val motto: String) : ProfileUiEvent
    data class UpdatePhotoUrl(val photoUrl: String?) : ProfileUiEvent
}
