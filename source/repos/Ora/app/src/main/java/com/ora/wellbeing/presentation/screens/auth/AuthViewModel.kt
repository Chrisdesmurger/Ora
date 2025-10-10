package com.ora.wellbeing.presentation.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ora.wellbeing.BuildConfig
import com.ora.wellbeing.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * FIX(auth): ViewModel pour gérer l'authentification
 * Utilise Credential Manager pour Google Sign-In
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResult: StateFlow<AuthResult> = _authResult.asStateFlow()

    private val credentialManager = CredentialManager.create(context)

    /**
     * Gère les événements de l'UI
     */
    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> {
                _uiState.update { it.copy(email = event.email) }
            }
            is AuthUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.password) }
            }
            AuthUiEvent.TogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            AuthUiEvent.ToggleSignUpMode -> {
                _uiState.update {
                    it.copy(
                        isSignUpMode = !it.isSignUpMode,
                        errorMessage = null
                    )
                }
            }
            AuthUiEvent.SignInWithEmail -> signInWithEmail()
            AuthUiEvent.SignUpWithEmail -> signUpWithEmail()
            AuthUiEvent.SignInWithGoogle -> signInWithGoogle()
            AuthUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    /**
     * Connexion avec email/password
     */
    private fun signInWithEmail() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (!validateEmailPassword(email, password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Timber.d("AuthViewModel: Starting email sign in")

            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    Timber.d("AuthViewModel: Sign in successful")
                    _authResult.value = AuthResult.Success
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    Timber.e(exception, "AuthViewModel: Sign in failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                    _authResult.value = AuthResult.Error(getErrorMessage(exception))
                }
        }
    }

    /**
     * Inscription avec email/password
     */
    private fun signUpWithEmail() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (!validateEmailPassword(email, password)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Timber.d("AuthViewModel: Starting email sign up")

            authRepository.signUpWithEmail(email, password)
                .onSuccess {
                    Timber.d("AuthViewModel: Sign up successful")
                    _authResult.value = AuthResult.Success
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { exception ->
                    Timber.e(exception, "AuthViewModel: Sign up failed")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(exception)
                        )
                    }
                    _authResult.value = AuthResult.Error(getErrorMessage(exception))
                }
        }
    }

    /**
     * Connexion avec Google via Credential Manager
     */
    private fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            Timber.d("AuthViewModel: Starting Google sign in")

            try {
                // Web Client ID extrait de google-services.json (client_type: 3)
                val webClientId = "859432337771-4uvfmnga435mtjvtl5itfru63mrtagkt.apps.googleusercontent.com"

                // Créer la requête Google ID
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Obtenir les credentials via Credential Manager
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                // Extraire le Google ID Token
                val credential = result.credential
                Timber.d("AuthViewModel: Credential type: ${credential.type}, class: ${credential.javaClass.name}")

                when {
                    credential is GoogleIdTokenCredential -> {
                        val idToken = credential.idToken
                        Timber.d("AuthViewModel: Got Google ID token")

                        // Authentifier avec Firebase
                        authRepository.signInWithGoogle(idToken)
                            .onSuccess {
                                Timber.d("AuthViewModel: Google sign in successful")
                                _authResult.value = AuthResult.Success
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            .onFailure { exception ->
                                Timber.e(exception, "AuthViewModel: Firebase auth with Google failed")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Échec de l'authentification Google: ${exception.message}"
                                    )
                                }
                            }
                    }
                    credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL" -> {
                        // Fallback pour les cas où le cast ne fonctionne pas
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Timber.d("AuthViewModel: Got Google ID token via createFrom")

                        authRepository.signInWithGoogle(idToken)
                            .onSuccess {
                                Timber.d("AuthViewModel: Google sign in successful")
                                _authResult.value = AuthResult.Success
                                _uiState.update { it.copy(isLoading = false) }
                            }
                            .onFailure { exception ->
                                Timber.e(exception, "AuthViewModel: Firebase auth with Google failed")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Échec de l'authentification Google: ${exception.message}"
                                    )
                                }
                            }
                    }
                    else -> {
                        throw Exception("Type de credential inattendu: ${credential.type} (${credential.javaClass.name})")
                    }
                }

            } catch (e: GetCredentialException) {
                val errorMsg = "Credential Manager error: ${e.javaClass.simpleName} - ${e.message}"
                Timber.e(e, "AuthViewModel: $errorMsg")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Connexion Google échouée: ${e.javaClass.simpleName}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "AuthViewModel: Google sign in error: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur Google: ${e.message ?: e.javaClass.simpleName}"
                    )
                }
            }
        }
    }

    /**
     * Valide l'email et le mot de passe
     */
    private fun validateEmailPassword(email: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "L'email est requis") }
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.update { it.copy(errorMessage = "Email invalide") }
                return false
            }
            password.isEmpty() -> {
                _uiState.update { it.copy(errorMessage = "Le mot de passe est requis") }
                return false
            }
            password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Le mot de passe doit contenir au moins 6 caractères") }
                return false
            }
        }
        return true
    }

    /**
     * Convertit les exceptions en messages d'erreur lisibles
     */
    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("The email address is already in use") == true ->
                "Cette adresse email est déjà utilisée"
            exception.message?.contains("The password is invalid") == true ->
                "Mot de passe incorrect"
            exception.message?.contains("There is no user record") == true ->
                "Aucun compte trouvé avec cet email"
            exception.message?.contains("The email address is badly formatted") == true ->
                "Format d'email invalide"
            exception.message?.contains("A network error") == true ->
                "Erreur réseau. Vérifiez votre connexion"
            else -> exception.message ?: "Une erreur est survenue"
        }
    }
}
