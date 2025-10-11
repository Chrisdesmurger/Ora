package com.ora.wellbeing.feature.practice.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ora.wellbeing.core.domain.practice.MediaType
import com.ora.wellbeing.feature.practice.player.PlaybackSpeed
import com.ora.wellbeing.feature.practice.player.RepeatMode
import kotlin.math.roundToInt

/**
 * Écran de lecture plein écran avec tous les contrôles
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    practiceId: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                LoadingState()
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error ?: "Erreur inconnue",
                    onRetry = { viewModel.onEvent(PlayerUiEvent.Retry) },
                    onBack = onBack
                )
            }
            uiState.practice != null -> {
                PlayerContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onBack = onBack,
                    onMinimize = onMinimize
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Chargement...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Retour")
                }
                Button(onClick = onRetry) {
                    Text("Réessayer")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerContent(
    uiState: PlayerUiState,
    onEvent: (PlayerUiEvent) -> Unit,
    onBack: () -> Unit,
    onMinimize: () -> Unit
) {
    val practice = uiState.practice ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = practice.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = practice.instructor ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Retour")
                }
            },
            actions = {
                IconButton(onClick = onMinimize) {
                    Icon(Icons.Default.Minimize, "Minimiser")
                }
                IconButton(onClick = { onEvent(PlayerUiEvent.ToggleFullscreen) }) {
                    Icon(
                        if (uiState.isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        "Plein écran"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Media View
        MediaPlayer(
            practice = practice,
            playerState = uiState.playerState,
            isFullscreen = uiState.isFullscreen,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (uiState.isFullscreen) 1f else 0.5f)
        )

        // Controls and Info Section
        if (!uiState.isFullscreen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Controls
                PlayerControls(
                    playerState = uiState.playerState,
                    onEvent = onEvent
                )

                HorizontalDivider()

                // Advanced Controls
                AdvancedControls(
                    uiState = uiState,
                    onEvent = onEvent
                )

                HorizontalDivider()

                // Session Info
                SessionInfo(
                    practice = practice,
                    sessionDuration = uiState.sessionDuration
                )
            }
        } else {
            // Fullscreen overlay controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                PlayerControls(
                    playerState = uiState.playerState,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun MediaPlayer(
    practice: com.ora.wellbeing.core.domain.practice.Practice,
    playerState: com.ora.wellbeing.feature.practice.player.PlayerState,
    isFullscreen: Boolean,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(if (isFullscreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        when (practice.mediaType) {
            MediaType.VIDEO -> {
                // Video Player View
                val context = LocalContext.current
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = if (isFullscreen) {
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                            } else {
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                            }
                        }
                    },
                    update = { playerView ->
                        playerView.resizeMode = if (isFullscreen) {
                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        } else {
                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            MediaType.AUDIO -> {
                // Audio: show thumbnail with visualizer
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = practice.thumbnailUrl,
                        contentDescription = practice.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )

                    // Audio visualizer overlay (simplified)
                    if (playerState.isPlaying) {
                        AudioVisualizer()
                    }
                }
            }
        }

        // Buffering indicator
        if (playerState.buffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Network warning
        if (!playerState.isNetworkAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Pas de connexion réseau",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioVisualizer() {
    // Simplified audio visualizer
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.GraphicEq,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun AdvancedControls(
    uiState: PlayerUiState,
    onEvent: (PlayerUiEvent) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Contrôles avancés",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        // Speed Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vitesse de lecture",
                style = MaterialTheme.typography.bodyMedium
            )
            PlaybackSpeedSelector(
                currentSpeed = PlaybackSpeed.fromValue(uiState.playerState.playbackSpeed),
                onSpeedSelected = { onEvent(PlayerUiEvent.SetPlaybackSpeed(it)) }
            )
        }

        // Repeat Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mode répétition",
                style = MaterialTheme.typography.bodyMedium
            )
            RepeatModeSelector(
                currentMode = uiState.playerState.repeatMode,
                onModeSelected = { onEvent(PlayerUiEvent.SetRepeatMode(it)) }
            )
        }
    }
}

@Composable
private fun PlaybackSpeedSelector(
    currentSpeed: PlaybackSpeed,
    onSpeedSelected: (PlaybackSpeed) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(currentSpeed.label)
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PlaybackSpeed.values().forEach { speed ->
                DropdownMenuItem(
                    text = { Text(speed.label) },
                    onClick = {
                        onSpeedSelected(speed)
                        expanded = false
                    },
                    leadingIcon = {
                        if (speed == currentSpeed) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RepeatModeSelector(
    currentMode: RepeatMode,
    onModeSelected: (RepeatMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RepeatMode.values().forEach { mode ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                label = {
                    Text(
                        when (mode) {
                            RepeatMode.OFF -> "Désactivé"
                            RepeatMode.ONE -> "Une fois"
                            RepeatMode.ALL -> "Infini"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        when (mode) {
                            RepeatMode.OFF -> Icons.Default.Close
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            RepeatMode.ALL -> Icons.Default.Repeat
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun SessionInfo(
    practice: com.ora.wellbeing.core.domain.practice.Practice,
    sessionDuration: Long
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Informations de session",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        InfoRow(label = "Discipline", value = practice.discipline.displayName)
        InfoRow(label = "Niveau", value = practice.level.displayName)
        InfoRow(label = "Durée prévue", value = "${practice.durationMin} min")
        if (sessionDuration > 0) {
            InfoRow(
                label = "Temps écoulé",
                value = formatDuration(sessionDuration)
            )
        }

        if (practice.benefits.isNotEmpty()) {
            Text(
                text = "Bienfaits",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            practice.benefits.take(3).forEach { benefit ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        "${minutes}min ${secs}s"
    } else {
        "${secs}s"
    }
}

/**
 * Player Controls composable
 */
@Composable
private fun PlayerControls(
    playerState: com.ora.wellbeing.feature.practice.player.PlayerState,
    onEvent: (PlayerUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Seek Bar
        CustomSeekBar(
            currentPosition = playerState.currentPosition,
            duration = playerState.duration,
            onSeek = { pos -> onEvent(PlayerUiEvent.SeekTo(pos)) },
            modifier = Modifier.fillMaxWidth()
        )

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onEvent(PlayerUiEvent.SeekBackward) }) {
                Icon(Icons.Default.Replay10, "Reculer de 10s")
            }
            IconButton(onClick = { onEvent(PlayerUiEvent.TogglePlayPause) }) {
                Icon(
                    if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (playerState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(48.dp)
                )
            }
            IconButton(onClick = { onEvent(PlayerUiEvent.SeekForward) }) {
                Icon(Icons.Default.Forward10, "Avancer de 10s")
            }
        }
    }
}

/**
 * HorizontalDivider composable (Material 3 equivalent of Divider)
 */
@Composable
private fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
