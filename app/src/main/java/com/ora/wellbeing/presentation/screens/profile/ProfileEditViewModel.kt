package com.ora.wellbeing.presentation.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ora.wellbeing.domain.model.UserProfile
import com.ora.wellbeing.domain.repository.FirestoreUserProfileRepository
import com.ora.wellbeing.data.sync.SyncManager
import com.ora.wellbeing.presentation.screens.profile.validation.ProfileField
import com.ora.wellbeing.presentation.screens.profile.validation.ProfileValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * ViewModel for Profile Edit Screen
 * Handles form state, validation, photo upload, and Firestore updates
 */
@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val userProfileRepository: FirestoreUserProfileRepository,
    private val syncManager: SyncManager,
    private val authRepository: com.ora.wellbeing.data.repository.AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileEditUiState())
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val validator = ProfileValidator()
    private var initialProfile: UserProfile? = null

    init {
        loadProfile()
    }

    /**
     * Load current user profile from SyncManager
     */
    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                syncManager.userProfile.collect { profile ->
                    if (profile != null) {
                        initialProfile = profile
                        _uiState.update {
                            it.copy(
                                profile = profile,
                                firstName = profile.firstName ?: "",
                                lastName = profile.lastName ?: "",
                                bio = profile.motto ?: "",
                                photoUrl = profile.photoUrl,
                                language = "fr", // TODO: Get from profile when locale field is added
                                isLoading = false,
                                hasUnsavedChanges = false
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Impossible de charger le profil"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading profile")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erreur: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Handle UI events
     */
    fun onEvent(event: ProfileEditUiEvent) {
        when (event) {
            is ProfileEditUiEvent.UpdateFirstName -> updateFirstName(event.firstName)
            is ProfileEditUiEvent.UpdateLastName -> updateLastName(event.lastName)
            is ProfileEditUiEvent.UpdateBio -> updateBio(event.bio)
            is ProfileEditUiEvent.UpdateGender -> updateGender(event.gender)
            is ProfileEditUiEvent.ChangeLanguage -> changeLanguage(event.language)
            is ProfileEditUiEvent.ToggleNotifications -> toggleNotifications(event.enabled)
            is ProfileEditUiEvent.ToggleEveningReminder -> toggleEveningReminder(event.enabled)
            is ProfileEditUiEvent.UploadPhoto -> uploadPhoto(event.uri)
            is ProfileEditUiEvent.RemovePhoto -> removePhoto()
            is ProfileEditUiEvent.Save -> saveProfile()
            is ProfileEditUiEvent.SignOut -> signOut()
            is ProfileEditUiEvent.NavigateBack -> {} // Handled by UI
            is ProfileEditUiEvent.DismissError -> dismissError()
            is ProfileEditUiEvent.DismissSuccess -> dismissSuccess()
        }
    }

    private fun updateFirstName(firstName: String) {
        _uiState.update {
            it.copy(
                firstName = firstName,
                hasUnsavedChanges = true,
                validationErrors = it.validationErrors - ProfileField.FIRST_NAME
            )
        }
    }

    private fun updateLastName(lastName: String) {
        _uiState.update {
            it.copy(
                lastName = lastName,
                hasUnsavedChanges = true,
                validationErrors = it.validationErrors - ProfileField.LAST_NAME
            )
        }
    }

    private fun updateBio(bio: String) {
        _uiState.update {
            it.copy(
                bio = bio,
                hasUnsavedChanges = true,
                validationErrors = it.validationErrors - ProfileField.BIO
            )
        }
    }

    private fun updateGender(gender: String?) {
        _uiState.update {
            it.copy(
                gender = gender,
                hasUnsavedChanges = true
            )
        }
    }

    private fun changeLanguage(language: String) {
        _uiState.update {
            it.copy(
                language = language,
                hasUnsavedChanges = true
            )
        }
    }

    private fun toggleNotifications(enabled: Boolean) {
        _uiState.update {
            it.copy(
                notificationsEnabled = enabled,
                hasUnsavedChanges = true
            )
        }
    }

    private fun toggleEveningReminder(enabled: Boolean) {
        _uiState.update {
            it.copy(
                eveningReminderEnabled = enabled,
                hasUnsavedChanges = true
            )
        }
    }

    private fun removePhoto() {
        _uiState.update {
            it.copy(
                photoUrl = null,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Upload photo to Firebase Storage
     * Compresses image to max 1024x1024, JPEG 85% quality
     */
    private fun uploadPhoto(uri: Uri) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = true,
                        uploadProgress = 0,
                        errorMessage = null
                    )
                }

                // Compress image
                val compressedData = compressImage(uri)

                // Upload to Firebase Storage: /users/{uid}/profile.jpg
                val storageRef = firebaseStorage.reference
                    .child("users")
                    .child(uid)
                    .child("profile.jpg")

                val uploadTask = storageRef.putBytes(compressedData)

                // Monitor upload progress
                uploadTask.addOnProgressListener { snapshot ->
                    val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                    _uiState.update { it.copy(uploadProgress = progress) }
                }

                // Wait for upload to complete
                uploadTask.await()

                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                Timber.d("Photo uploaded successfully: $downloadUrl")

                _uiState.update {
                    it.copy(
                        photoUrl = downloadUrl,
                        isUploadingPhoto = false,
                        uploadProgress = 0,
                        hasUnsavedChanges = true
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error uploading photo")
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        uploadProgress = 0,
                        errorMessage = "Erreur lors du téléchargement de la photo: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Compress image to max 1024x1024, JPEG 85% quality
     */
    private fun compressImage(uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open image")

        // Decode image
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Calculate scaling
        val maxSize = 1024
        val ratio = Math.min(
            maxSize.toFloat() / bitmap.width,
            maxSize.toFloat() / bitmap.height
        )

        val width = (bitmap.width * ratio).toInt()
        val height = (bitmap.height * ratio).toInt()

        // Resize bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

        // Compress to JPEG 85%
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

        return outputStream.toByteArray()
    }

    /**
     * Validate and save profile to Firestore
     */
    private fun saveProfile() {
        val state = _uiState.value

        // Validate form
        val errors = validator.validateProfile(
            firstName = state.firstName,
            lastName = state.lastName,
            bio = state.bio.ifBlank { null }
        )

        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    validationErrors = errors,
                    errorMessage = "Veuillez corriger les erreurs avant de sauvegarder"
                )
            }
            return
        }

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            _uiState.update {
                it.copy(errorMessage = "Utilisateur non connecté")
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, errorMessage = null) }

                // Build updated profile
                val updatedProfile = initialProfile?.copy(
                    firstName = state.firstName.ifBlank { null },
                    lastName = state.lastName.ifBlank { null },
                    motto = state.bio.ifBlank { null },
                    photoUrl = state.photoUrl
                ) ?: return@launch

                // Update Firestore
                val result = userProfileRepository.updateUserProfile(updatedProfile)

                if (result.isSuccess) {
                    Timber.d("Profile updated successfully")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            hasUnsavedChanges = false,
                            successMessage = "Profil mis à jour avec succès",
                            validationErrors = emptyMap()
                        )
                    }
                } else {
                    throw result.exceptionOrNull() ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving profile")
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Erreur lors de la sauvegarde: ${e.message}"
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * Sign out the current user
     */
    private fun signOut() {
        viewModelScope.launch {
            try {
                Timber.d("ProfileEditViewModel: Signing out user")
                val result = authRepository.signOut()

                if (result.isSuccess) {
                    Timber.d("ProfileEditViewModel: Sign out successful")
                    // Stop sync manager
                    syncManager.stopSync()

                    _uiState.update {
                        it.copy(successMessage = "Déconnexion réussie")
                    }
                } else {
                    throw result.exceptionOrNull() ?: Exception("Sign out failed")
                }
            } catch (e: Exception) {
                Timber.e(e, "ProfileEditViewModel: Sign out error")
                _uiState.update {
                    it.copy(errorMessage = "Erreur lors de la déconnexion: ${e.message}")
                }
            }
        }
    }
}
