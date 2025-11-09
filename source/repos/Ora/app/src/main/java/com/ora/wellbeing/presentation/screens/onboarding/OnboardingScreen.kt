package com.ora.wellbeing.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.data.model.onboarding.AnswerOption
import com.ora.wellbeing.data.model.onboarding.OnboardingQuestion
import com.ora.wellbeing.data.model.onboarding.QuestionTypeKind

/**
 * Main Onboarding Screen
 * Displays questions one by one with progress indicator
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate on completion
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    // Show errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
        }
    }

    // Auto-start when loaded
    LaunchedEffect(uiState.config) {
        if (uiState.config != null && !uiState.hasStarted) {
            viewModel.onEvent(OnboardingUiEvent.StartOnboarding)
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
            when {
                uiState.isLoading -> {
                    OnboardingLoadingScreen()
                }
                uiState.config == null -> {
                    OnboardingErrorScreen(
                        error = uiState.error ?: "Impossible de charger le questionnaire",
                        onRetry = { viewModel.onEvent(OnboardingUiEvent.RetryLoad) }
                    )
                }
                else -> {
                    OnboardingQuestionnaireContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent,
                        progress = viewModel.getProgress()
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Préparation de votre questionnaire...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun OnboardingErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun OnboardingQuestionnaireContent(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Progress text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Question ${uiState.currentQuestionIndex + 1} sur ${uiState.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "${uiState.progressPercentage}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Question content with animation
        AnimatedContent(
            targetState = uiState.currentQuestionIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "question_transition"
        ) { questionIndex ->
            uiState.currentQuestion?.let { question ->
                OnboardingQuestionCard(
                    question = question,
                    selectedOptions = uiState.currentAnswers[question.id] ?: emptyList(),
                    onAnswerChange = { selectedOptions, textAnswer ->
                        onEvent(OnboardingUiEvent.AnswerQuestion(selectedOptions, textAnswer))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        OnboardingNavigationButtons(
            uiState = uiState,
            onEvent = onEvent
        )
    }
}

@Composable
fun OnboardingQuestionCard(
    question: OnboardingQuestion,
    selectedOptions: List<String>,
    onAnswerChange: (List<String>, String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = getCategoryLabel(question.category),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Question title
        Text(
            text = question.getLocalizedTitle(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Subtitle
        question.getLocalizedSubtitle()?.let { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Options based on question type
        when (question.type.toKind()) {
            QuestionTypeKind.MULTIPLE_CHOICE -> {
                MultipleChoiceOptions(
                    options = question.options,
                    selectedOptions = selectedOptions,
                    allowMultiple = question.type.allowMultiple ?: false,
                    onSelectionChange = { newSelection ->
                        onAnswerChange(newSelection, null)
                    }
                )
            }
            QuestionTypeKind.TEXT_INPUT -> {
                var textInput by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        onAnswerChange(emptyList(), it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Votre réponse") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            QuestionTypeKind.RATING -> {
                RatingOptions(
                    options = question.options,
                    selectedOption = selectedOptions.firstOrNull(),
                    onSelectionChange = { optionId ->
                        onAnswerChange(listOf(optionId), null)
                    }
                )
            }
            QuestionTypeKind.TIME_SELECTION -> {
                MultipleChoiceOptions(
                    options = question.options,
                    selectedOptions = selectedOptions,
                    allowMultiple = question.type.allowMultiple ?: false,
                    onSelectionChange = { newSelection ->
                        onAnswerChange(newSelection, null)
                    }
                )
            }
        }

        // Required indicator
        if (question.required) {
            Text(
                text = "* Cette question est obligatoire",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MultipleChoiceOptions(
    options: List<AnswerOption>,
    selectedOptions: List<String>,
    allowMultiple: Boolean,
    onSelectionChange: (List<String>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.sortedBy { it.order }.forEach { option ->
            val isSelected = selectedOptions.contains(option.id)

            OptionCard(
                option = option,
                isSelected = isSelected,
                onClick = {
                    val newSelection = if (allowMultiple) {
                        if (isSelected) {
                            selectedOptions - option.id
                        } else {
                            selectedOptions + option.id
                        }
                    } else {
                        listOf(option.id)
                    }
                    onSelectionChange(newSelection)
                }
            )
        }
    }
}

@Composable
fun RatingOptions(
    options: List<AnswerOption>,
    selectedOption: String?,
    onSelectionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.sortedBy { it.order }.forEach { option ->
            val isSelected = selectedOption == option.id

            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onSelectionChange(option.id) },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                } else {
                    androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                },
                tonalElevation = 2.dp,
                shadowElevation = if (isSelected) 4.dp else 1.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.icon ?: option.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    option: AnswerOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        tonalElevation = 2.dp,
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            option.icon?.let { emoji ->
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Label
            Text(
                text = option.getLocalizedLabel(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Checkmark
            AnimatedVisibility(visible = isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OnboardingNavigationButtons(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Previous button
            if (!uiState.isFirstQuestion) {
                OutlinedButton(
                    onClick = { onEvent(OnboardingUiEvent.PreviousQuestion) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Précédent")
                }
            }

            // Next/Complete button
            Button(
                onClick = {
                    if (uiState.isLastQuestion) {
                        onEvent(OnboardingUiEvent.CompleteOnboarding)
                    } else {
                        onEvent(OnboardingUiEvent.NextQuestion)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = uiState.canProceed && !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isLastQuestion) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (uiState.isLastQuestion) "Terminer" else "Suivant"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (uiState.isLastQuestion) {
                            Icons.Default.Check
                        } else {
                            Icons.Filled.ArrowForward
                        },
                        contentDescription = if (uiState.isLastQuestion) "Complete" else "Next",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun getCategoryLabel(category: String): String {
    return when (category.lowercase()) {
        "goals" -> "🎯 Objectifs"
        "experience" -> "⭐ Expérience"
        "preferences" -> "❤️ Préférences"
        "personalization" -> "✨ Personnalisation"
        else -> category
    }
}
