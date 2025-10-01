package com.ora.wellbeing.presentation.screens.profile

/**
 * Événements de l'interface utilisateur pour l'écran Profile
 */
sealed interface ProfileUiEvent {
    data object LoadProfileData : ProfileUiEvent
    data object NavigateToEditProfile : ProfileUiEvent
    data class NavigateToPracticeStats(val practiceId: String) : ProfileUiEvent
    data class ToggleGoal(val goalId: String) : ProfileUiEvent
    data object NavigateToGratitudes : ProfileUiEvent
}