package com.ora.wellbeing.presentation.screens.auth

/**
 * FIX(auth): État UI pour l'écran d'authentification
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val isSignUpMode: Boolean = false
)

/**
 * FIX(auth): Événements UI pour l'écran d'authentification
 */
sealed class AuthUiEvent {
    data class EmailChanged(val email: String) : AuthUiEvent()
    data class PasswordChanged(val password: String) : AuthUiEvent()
    object TogglePasswordVisibility : AuthUiEvent()
    object ToggleSignUpMode : AuthUiEvent()
    object SignInWithEmail : AuthUiEvent()
    object SignUpWithEmail : AuthUiEvent()
    object SignInWithGoogle : AuthUiEvent()
    object DismissError : AuthUiEvent()
}

/**
 * FIX(auth): Résultats d'authentification
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Idle : AuthResult()
}
