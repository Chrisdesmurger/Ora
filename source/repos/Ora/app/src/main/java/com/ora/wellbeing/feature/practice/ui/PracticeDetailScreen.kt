package com.ora.wellbeing.feature.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ora.wellbeing.R
import com.ora.wellbeing.core.domain.practice.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeDetailScreen(
    practiceId: String,
    onBack: () -> Unit,
    viewModel: PracticeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Erreur",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadPractice(practiceId) }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            uiState.practice != null -> {
                PracticeDetailContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // Notes Dialog
    if (uiState.showNotesDialog) {
        NotesDialog(
            currentNotes = uiState.currentNotes,
            onDismiss = { viewModel.onEvent(PracticeUiEvent.DismissNotesDialog) },
            onSave = { notes -> viewModel.onEvent(PracticeUiEvent.SaveNotes(notes)) }
        )
    }
}

@Composable
private fun PracticeDetailContent(
    uiState: PracticeUiState,
    onEvent: (PracticeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val practice = uiState.practice ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Hero Media Section
        MediaSection(
            practice = practice,
            playerState = uiState.playerState,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            // Title
            Text(
                text = practice.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chips: Duration & Level
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("${practice.durationMin} min") }
                )
                AssistChip(
                    onClick = {},
                    label = { Text(practice.level.displayName) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Instructor
            practice.instructor?.let {
                Text(
                    text = "Avec $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Description
            Text(
                text = practice.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Benefits
            if (practice.benefits.isNotEmpty()) {
                Text(
                    text = "Bienfaits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                practice.benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Primary CTA: Start Session
            Button(
                onClick = { onEvent(PracticeUiEvent.StartSession) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = if (uiState.playerState.isPlaying) "En cours..." else "Commencer la séance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add to Program
                OutlinedButton(
                    onClick = { onEvent(PracticeUiEvent.AddToProgram) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter")
                }

                // Personal Notes
                OutlinedButton(
                    onClick = { onEvent(PracticeUiEvent.OpenNotes) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notes")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Download button
            if (practice.downloadable) {
                OutlinedButton(
                    onClick = { onEvent(PracticeUiEvent.ToggleDownload) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            uiState.downloadState?.progress ?: 0f > 0 -> "Téléchargement ${(uiState.downloadState?.progress?.times(100))?.toInt()}%"
                            uiState.isDownloaded -> "Téléchargé ✓"
                            else -> "Télécharger pour hors-ligne"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Similar practices
            if (uiState.similarPractices.isNotEmpty()) {
                Text(
                    text = "Séances similaires",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                SimilarRow(
                    practices = uiState.similarPractices,
                    onPracticeClick = { /* TODO: Navigate */ }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MediaSection(
    practice: com.ora.wellbeing.core.domain.practice.Practice,
    playerState: com.ora.wellbeing.feature.practice.player.PlayerState,
    onEvent: (PracticeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        when (practice.mediaType) {
            MediaType.VIDEO -> {
                // Video Player View
                val context = LocalContext.current
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.AUDIO -> {
                // Audio: show thumbnail
                AsyncImage(
                    model = practice.thumbnailUrl,
                    contentDescription = practice.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Overlay controls if playing
        if (playerState.isPlaying || playerState.currentPosition > 0) {
            PracticeControls(
                playerState = playerState,
                onEvent = onEvent,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun NotesDialog(
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notes personnelles") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Écrivez vos notes ici...") },
                minLines = 5,
                maxLines = 10
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes) }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
