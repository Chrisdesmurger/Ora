package com.ora.wellbeing.presentation.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Écran d'authentification avec Email/Password et Google Sign-In
 * Design Ora: Utilise les couleurs zen (violet, teal, rose)
 */
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authResult by viewModel.authResult.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Redirection après succès d'authentification
    LaunchedEffect(authResult) {
        if (authResult is AuthResult.Success) {
            onAuthSuccess()
        }
    }

    // Affichage Snackbar pour les erreurs
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(AuthUiEvent.DismissError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo / Titre de l'app avec couleurs Ora
                Text(
                    text = "🧘‍♀️ Ora",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary // Bleu violet zen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Votre compagnon bien-être",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary, // Teal apaisante
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Carte arrondie pour le formulaire (style zen)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Titre du formulaire
                        Text(
                            text = if (uiState.isSignUpMode) "Créer un compte" else "Se connecter",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Champ Email avec couleurs Ora
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
                            label = { Text("Email") },
                            placeholder = { Text("vous@exemple.com") },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Champ Password avec couleurs Ora
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onEvent(AuthUiEvent.PasswordChanged(it)) },
                            label = { Text("Mot de passe") },
                            placeholder = { Text("Minimum 6 caractères") },
                            singleLine = true,
                            enabled = !uiState.isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            visualTransformation = if (uiState.isPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (uiState.isSignUpMode) {
                                        viewModel.onEvent(AuthUiEvent.SignUpWithEmail)
                                    } else {
                                        viewModel.onEvent(AuthUiEvent.SignInWithEmail)
                                    }
                                }
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.onEvent(AuthUiEvent.TogglePasswordVisibility) }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (uiState.isPasswordVisible)
                                            "Masquer le mot de passe"
                                        else
                                            "Afficher le mot de passe",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bouton principal (Sign In / Sign Up) - Couleur primaire Ora
                        Button(
                            onClick = {
                                if (uiState.isSignUpMode) {
                                    viewModel.onEvent(AuthUiEvent.SignUpWithEmail)
                                } else {
                                    viewModel.onEvent(AuthUiEvent.SignInWithEmail)
                                }
                            },
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    text = if (uiState.isSignUpMode) "Créer mon compte" else "Se connecter",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bouton pour changer de mode - Couleur secondaire
                        TextButton(
                            onClick = { viewModel.onEvent(AuthUiEvent.ToggleSignUpMode) },
                            enabled = !uiState.isLoading
                        ) {
                            Text(
                                text = if (uiState.isSignUpMode)
                                    "Déjà un compte ? Se connecter"
                                else
                                    "Pas encore de compte ? S'inscrire",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Divider avec "OU" - Couleur tertiaire douce
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "OU",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bouton Google Sign-In - Bordure secondaire
                OutlinedButton(
                    onClick = { viewModel.onEvent(AuthUiEvent.SignInWithGoogle) },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "🔐 Continuer avec Google",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Message d'information - Couleur tertiaire
                AnimatedVisibility(visible = uiState.isSignUpMode) {
                    Text(
                        text = "En créant un compte, vous acceptez nos conditions d'utilisation et notre politique de confidentialité.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
