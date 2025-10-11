package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.screens.profile.components.DropdownOption
import com.ora.wellbeing.presentation.screens.profile.components.ProfileDropdown
import com.ora.wellbeing.presentation.screens.profile.components.ProfilePhotoEditor
import com.ora.wellbeing.presentation.screens.profile.components.ProfileTextField
import com.ora.wellbeing.presentation.screens.profile.validation.ProfileField

/**
 * Profile Edit Screen
 * Full-screen Material 3 UI for editing user profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    // Show success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ProfileEditUiEvent.DismissSuccess)
            // Navigate back on success
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ProfileEditUiEvent.DismissError)
        }
    }

    // Unsaved changes dialog
    if (showUnsavedDialog) {
        UnsavedChangesDialog(
            onSave = {
                showUnsavedDialog = false
                viewModel.onEvent(ProfileEditUiEvent.Save)
            },
            onDiscard = {
                showUnsavedDialog = false
                onNavigateBack()
            },
            onCancel = {
                showUnsavedDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modifier le profil") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.hasUnsavedChanges) {
                                showUnsavedDialog = true
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(
                        onClick = { viewModel.onEvent(ProfileEditUiEvent.Save) },
                        enabled = !uiState.isSaving && uiState.hasUnsavedChanges
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(12.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Enregistrer"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            // Loading state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chargement du profil...")
            }
        } else {
            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Photo
                ProfilePhotoEditor(
                    photoUrl = uiState.photoUrl,
                    onPhotoSelected = { uri ->
                        viewModel.onEvent(ProfileEditUiEvent.UploadPhoto(uri))
                    },
                    isUploading = uiState.isUploadingPhoto,
                    uploadProgress = uiState.uploadProgress
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Personal Information Section
                SectionHeader("Informations personnelles")

                Spacer(modifier = Modifier.height(16.dp))

                // First Name
                ProfileTextField(
                    value = uiState.firstName,
                    onValueChange = {
                        viewModel.onEvent(ProfileEditUiEvent.UpdateFirstName(it))
                    },
                    label = "Prénom *",
                    placeholder = "Votre prénom",
                    errorMessage = uiState.validationErrors[ProfileField.FIRST_NAME],
                    maxCharacters = 50,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Last Name
                ProfileTextField(
                    value = uiState.lastName,
                    onValueChange = {
                        viewModel.onEvent(ProfileEditUiEvent.UpdateLastName(it))
                    },
                    label = "Nom *",
                    placeholder = "Votre nom",
                    errorMessage = uiState.validationErrors[ProfileField.LAST_NAME],
                    maxCharacters = 50,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email (Read-only)
                ProfileTextField(
                    value = uiState.profile?.email ?: "",
                    onValueChange = {},
                    label = "Email",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bio/Motto
                ProfileTextField(
                    value = uiState.bio,
                    onValueChange = {
                        viewModel.onEvent(ProfileEditUiEvent.UpdateBio(it))
                    },
                    label = "Devise",
                    placeholder = "Votre devise personnelle...",
                    errorMessage = uiState.validationErrors[ProfileField.BIO],
                    maxCharacters = 200,
                    singleLine = false,
                    maxLines = 3,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender Dropdown
                ProfileDropdown(
                    value = uiState.gender,
                    onValueChange = {
                        viewModel.onEvent(ProfileEditUiEvent.UpdateGender(it))
                    },
                    label = "Genre",
                    placeholder = "Sélectionner",
                    options = listOf(
                        DropdownOption(null, "Préfère ne pas dire"),
                        DropdownOption("female", "Femme"),
                        DropdownOption("male", "Homme"),
                        DropdownOption("other", "Autre")
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Preferences Section
                SectionHeader("Préférences")

                Spacer(modifier = Modifier.height(16.dp))

                // Language Toggle (FR/EN)
                SettingRow(
                    title = "Langue",
                    description = if (uiState.language == "fr") "Français" else "English",
                    isEnabled = uiState.language == "fr",
                    onToggle = { enabled ->
                        viewModel.onEvent(
                            ProfileEditUiEvent.ChangeLanguage(if (enabled) "fr" else "en")
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Toggle
                SettingRow(
                    title = "Notifications",
                    description = "Recevoir des rappels quotidiens",
                    isEnabled = uiState.notificationsEnabled,
                    onToggle = {
                        viewModel.onEvent(ProfileEditUiEvent.ToggleNotifications(it))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Evening Reminder Toggle
                SettingRow(
                    title = "Rappel du soir",
                    description = "Rappel pour le journal de gratitude",
                    isEnabled = uiState.eveningReminderEnabled,
                    onToggle = {
                        viewModel.onEvent(ProfileEditUiEvent.ToggleEveningReminder(it))
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Required fields note
                Text(
                    text = "* Champs obligatoires",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun UnsavedChangesDialog(
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Modifications non enregistrées") },
        text = { Text("Voulez-vous enregistrer vos modifications avant de quitter ?") },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscard) {
                    Text("Ignorer")
                }
                TextButton(onClick = onCancel) {
                    Text("Annuler")
                }
            }
        }
    )
}
