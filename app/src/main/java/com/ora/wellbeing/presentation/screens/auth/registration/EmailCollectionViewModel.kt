package com.ora.wellbeing.presentation.screens.auth.registration

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ora.wellbeing.data.repository.AuthRepository
import com.ora.wellbeing.data.service.EmailNotificationService
import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel pour l'ecran de collecte d'email et creation de compte
 * Gere Firebase Auth (Email/Password + Google) + creation document Firestore users/{uid}
 */
@HiltViewModel
class EmailCollectionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestoreUserProfileRepository: FirestoreUserProfileRepository,
    private val emailNotificationService: EmailNotificationService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val credentialManager = CredentialManager.create(context)

    private val _uiState = MutableStateFlow(EmailCollectionUiState())
    val uiState: StateFlow<EmailCollectionUiState> = _uiState.asStateFlow()

    fun onEvent(event: EmailCollectionUiEvent) {
        when (event) {
            is EmailCollectionUiEvent.EmailChanged -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    emailError = null
                )
            }
            is EmailCollectionUiEvent.PasswordChanged -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    passwordError = null
                )
            }
            EmailCollectionUiEvent.TogglePasswordVisibility -> {
                _uiState.value = _uiState.value.copy(
                    isPasswordVisible = !_uiState.value.isPasswordVisible
                )
            }
            EmailCollectionUiEvent.CreateAccount -> {
                createAccount()
            }
            EmailCollectionUiEvent.SignUpWithGoogle -> {
                signUpWithGoogle()
            }
            EmailCollectionUiEvent.DismissError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    /**
     * Valide l'email (format basique)
     */
    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Valide le mot de passe (minimum 6 caracteres)
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Cree le compte Firebase Auth + document Firestore users/{uid}
     * FIX(build-debug-android): Use Date() instead of System.currentTimeMillis() for timestamps
     */
    private fun createAccount() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Validation
        if (!isEmailValid(email)) {
            _uiState.value = _uiState.value.copy(
                emailError = "Email invalide"
            )
            return
        }

        if (!isPasswordValid(password)) {
            _uiState.value = _uiState.value.copy(
                passwordError = "Le mot de passe doit contenir au moins 6 caracteres"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                Timber.d("EmailCollectionViewModel: Creating account for $email")

                // 1. Creer le compte Firebase Auth
                authRepository.signUpWithEmail(email, password)
                    .onSuccess { localUser ->
                        val uid = localUser.id
                        Timber.d("EmailCollectionViewModel: Firebase Auth created, uid=$uid")

                        // 2. Creer le document Firestore users/{uid}
                        // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
                        val userProfile = UserProfile(
                            uid = uid,
                            email = email,
                            firstName = null, // Sera rempli plus tard (optionnel)
                            lastName = null,
                            planTier = "free", // Par defaut
                            createdAt = Date(), // FIX(build-debug-android): Changed from Long to Date
                            updatedAt = Date()  // FIX(build-debug-android): Changed from Long to Date
                        )

                        firestoreUserProfileRepository.createUserProfile(userProfile)
                            .onSuccess {
                                Timber.i("EmailCollectionViewModel: Firestore profile created successfully")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    accountCreated = true
                                )

                                // Send welcome email asynchronously (fire-and-forget)
                                // Uses separate coroutine to not block the registration flow
                                viewModelScope.launch(Dispatchers.IO) {
                                    try {
                                        emailNotificationService.sendWelcomeEmail(
                                            uid = uid,
                                            email = email,
                                            firstName = null
                                        )
                                    } catch (e: Exception) {
                                        // Silent failure - email sending should not affect registration
                                        Timber.e(e, "EmailCollectionViewModel: Failed to send welcome email, continuing silently")
                                    }
                                }
                            }
                            .onFailure { firestoreError ->
                                // Firestore a echoue mais Auth a reussi
                                // On continue quand meme (le profil sera cree par SyncManager)
                                Timber.e(firestoreError, "EmailCollectionViewModel: Firestore profile creation failed, continuing anyway")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    accountCreated = true
                                )

                                // Send welcome email even if Firestore failed
                                viewModelScope.launch(Dispatchers.IO) {
                                    try {
                                        emailNotificationService.sendWelcomeEmail(
                                            uid = uid,
                                            email = email,
                                            firstName = null
                                        )
                                    } catch (e: Exception) {
                                        Timber.e(e, "EmailCollectionViewModel: Failed to send welcome email, continuing silently")
                                    }
                                }
                            }
                    }
                    .onFailure { authError ->
                        Timber.e(authError, "EmailCollectionViewModel: Firebase Auth failed")

                        val errorMessage = when {
                            authError.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                                "Cet email est deja utilise"
                            authError.message?.contains("invalid-email", ignoreCase = true) == true ->
                                "Email invalide"
                            authError.message?.contains("weak-password", ignoreCase = true) == true ->
                                "Mot de passe trop faible"
                            else -> "Erreur lors de la creation du compte"
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "EmailCollectionViewModel: Unexpected error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Une erreur inattendue s'est produite"
                )
            }
        }
    }

    /**
     * Sends a welcome email asynchronously.
     * This is a fire-and-forget operation that will not block the registration flow.
     * Failures are logged but do not affect the user experience.
     *
     * @param uid Firebase user ID
     * @param email User's email address (nullable for Google sign-in)
     * @param firstName User's first name (nullable)
     */
    private fun sendWelcomeEmailAsync(uid: String, email: String?, firstName: String?) {
        if (email.isNullOrBlank()) {
            Timber.w("EmailCollectionViewModel: Cannot send welcome email - email is null or blank")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                emailNotificationService.sendWelcomeEmail(
                    uid = uid,
                    email = email,
                    firstName = firstName
                )
                Timber.d("EmailCollectionViewModel: Welcome email triggered for $email")
            } catch (e: Exception) {
                // Silent failure - email sending should not affect registration
                Timber.e(e, "EmailCollectionViewModel: Failed to send welcome email, continuing silently")
            }
        }
    }

    /**
     * Inscription avec Google via Credential Manager
     * FIX(build-debug-android): Use Date() instead of System.currentTimeMillis() for timestamps
     */
    private fun signUpWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Timber.d("EmailCollectionViewModel: Starting Google sign up")

            try {
                // Web Client ID extrait de google-services.json
                val webClientId = "859432337771-4uvfmnga435mtjvtl5itfru63mrtagkt.apps.googleusercontent.com"

                // Creer la requete Google ID
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
                Timber.d("EmailCollectionViewModel: Credential type: ${credential.type}")

                when {
                    credential is GoogleIdTokenCredential -> {
                        val idToken = credential.idToken
                        Timber.d("EmailCollectionViewModel: Got Google ID token")

                        // Authentifier avec Firebase
                        authRepository.signInWithGoogle(idToken)
                            .onSuccess { localUser ->
                                Timber.d("EmailCollectionViewModel: Google sign up successful, uid=${localUser.id}")

                                // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
                                // Creer le profil Firestore
                                val userProfile = UserProfile(
                                    uid = localUser.id,
                                    email = localUser.email,
                                    firstName = null,
                                    lastName = null,
                                    planTier = "free",
                                    createdAt = Date(), // FIX(build-debug-android): Changed from Long to Date
                                    updatedAt = Date()  // FIX(build-debug-android): Changed from Long to Date
                                )

                                firestoreUserProfileRepository.createUserProfile(userProfile)
                                    .onSuccess {
                                        Timber.i("EmailCollectionViewModel: Firestore profile created (Google)")
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            accountCreated = true
                                        )

                                        // Send welcome email asynchronously (fire-and-forget)
                                        sendWelcomeEmailAsync(
                                            uid = localUser.id,
                                            email = localUser.email,
                                            firstName = null // Google doesn't always provide firstName
                                        )
                                    }
                                    .onFailure {
                                        // Continue meme si Firestore echoue
                                        Timber.e(it, "EmailCollectionViewModel: Firestore failed (Google), continuing")
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            accountCreated = true
                                        )

                                        // Send welcome email even if Firestore failed
                                        sendWelcomeEmailAsync(
                                            uid = localUser.id,
                                            email = localUser.email,
                                            firstName = null
                                        )
                                    }
                            }
                            .onFailure { exception ->
                                Timber.e(exception, "EmailCollectionViewModel: Firebase auth with Google failed")
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Echec de l'authentification Google"
                                )
                            }
                    }
                    credential.type == "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL" -> {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken

                        authRepository.signInWithGoogle(idToken)
                            .onSuccess { localUser ->
                                // FIX(build-debug-android): Use Date() instead of System.currentTimeMillis()
                                val userProfile = UserProfile(
                                    uid = localUser.id,
                                    email = localUser.email,
                                    firstName = null,
                                    lastName = null,
                                    planTier = "free",
                                    createdAt = Date(), // FIX(build-debug-android): Changed from Long to Date
                                    updatedAt = Date()  // FIX(build-debug-android): Changed from Long to Date
                                )

                                firestoreUserProfileRepository.createUserProfile(userProfile)
                                    .onSuccess {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            accountCreated = true
                                        )

                                        // Send welcome email asynchronously
                                        sendWelcomeEmailAsync(
                                            uid = localUser.id,
                                            email = localUser.email,
                                            firstName = null
                                        )
                                    }
                                    .onFailure {
                                        _uiState.value = _uiState.value.copy(
                                            isLoading = false,
                                            accountCreated = true
                                        )

                                        // Send welcome email even if Firestore failed
                                        sendWelcomeEmailAsync(
                                            uid = localUser.id,
                                            email = localUser.email,
                                            firstName = null
                                        )
                                    }
                            }
                            .onFailure {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Echec de l'authentification Google"
                                )
                            }
                    }
                    else -> {
                        throw Exception("Type de credential inattendu: ${credential.type}")
                    }
                }

            } catch (e: GetCredentialException) {
                Timber.e(e, "EmailCollectionViewModel: Credential Manager error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Connexion Google annulee"
                )
            } catch (e: Exception) {
                Timber.e(e, "EmailCollectionViewModel: Google sign up error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur Google: ${e.message}"
                )
            }
        }
    }
}

/**
 * UI State pour l'ecran de collecte email
 */
data class EmailCollectionUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val accountCreated: Boolean = false
) {
    val isFormValid: Boolean
        get() = email.isNotBlank() && password.length >= 6
}

/**
 * UI Events pour l'ecran de collecte email
 */
sealed class EmailCollectionUiEvent {
    data class EmailChanged(val email: String) : EmailCollectionUiEvent()
    data class PasswordChanged(val password: String) : EmailCollectionUiEvent()
    object TogglePasswordVisibility : EmailCollectionUiEvent()
    object CreateAccount : EmailCollectionUiEvent()
    object SignUpWithGoogle : EmailCollectionUiEvent()
    object DismissError : EmailCollectionUiEvent()
}
