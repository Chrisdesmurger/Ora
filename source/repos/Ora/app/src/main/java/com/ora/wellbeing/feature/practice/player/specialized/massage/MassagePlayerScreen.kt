package com.ora.wellbeing.feature.practice.player.specialized.massage

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.ui.CustomSeekBar

/**
 * Écran lecteur spécialisé pour Auto-massage/Bien-être
 * - Vue split : vidéo + carte corporelle
 * - Timer par zone
 * - Indicateur de pression
 * - Checklist des zones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MassagePlayerScreen(
    practiceId: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    viewModel: MassagePlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    // Adapter la couleur de la barre d'état au fond du lecteur
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PlayerColors.Massage.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerColors.Massage.background)
    ) {
        when {
            uiState.isLoading -> MassageLoadingState()
            uiState.error != null -> MassageErrorState(
                error = uiState.error ?: "Erreur inconnue",
                onRetry = { viewModel.onEvent(MassagePlayerEvent.Retry) },
                onBack = onBack
            )
            uiState.practice != null -> MassagePlayerContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBack = onBack
            )
        }
    }
}

@Composable
private fun MassageLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PlayerColors.Massage.accent)
            Text(
                text = "Préparation de votre séance...",
                style = MaterialTheme.typography.bodyLarge,
                color = PlayerColors.Massage.onBackground
            )
        }
    }
}

@Composable
private fun MassageErrorState(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Retour") }
                Button(onClick = onRetry) { Text("Réessayer") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MassagePlayerContent(
    uiState: MassagePlayerState,
    onEvent: (MassagePlayerEvent) -> Unit,
    onBack: () -> Unit
) {
    val practice = uiState.practice ?: return
    val currentZone = uiState.bodyZones.getOrNull(uiState.currentZoneIndex)

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = practice.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Retour")
                }
            },
            actions = {
                IconButton(onClick = { onEvent(MassagePlayerEvent.ToggleBodyMap) }) {
                    Icon(
                        Icons.Default.Person,
                        "Carte corporelle",
                        tint = if (uiState.showBodyMap) PlayerColors.Massage.accent else Color.Gray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PlayerColors.Massage.background
            )
        )

        // Vue split : Vidéo + Carte corporelle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.45f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vidéo
            Box(
                modifier = Modifier
                    .weight(if (uiState.showBodyMap) 0.55f else 1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                val context = LocalContext.current
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.playerState.buffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }

            // Carte corporelle
            AnimatedVisibility(
                visible = uiState.showBodyMap,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                BodyMapView(
                    zones = uiState.bodyZones,
                    currentZoneIndex = uiState.currentZoneIndex,
                    onZoneSelected = { onEvent(MassagePlayerEvent.SelectZone(it)) },
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                )
            }
        }

        // Informations de la zone actuelle
        currentZone?.let { zone ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Timer de zone
                ZoneTimerCard(
                    zoneName = zone.name,
                    zoneIcon = zone.icon,
                    timeRemaining = uiState.zoneTimer,
                    repetitionsRemaining = uiState.zoneRepetitions,
                    targetRepetitions = uiState.targetRepetitions
                )

                // Indicateur de pression
                PressureIndicator(
                    currentLevel = uiState.pressureLevel,
                    recommendedLevel = zone.pressureRecommended,
                    onLevelChanged = { onEvent(MassagePlayerEvent.SetPressureLevel(it)) }
                )

                // Instructions
                InstructionCard(instruction = uiState.currentInstruction)
            }
        }

        // Seek bar
        CustomSeekBar(
            currentPosition = uiState.playerState.currentPosition,
            duration = uiState.playerState.duration,
            onSeek = { onEvent(MassagePlayerEvent.SeekTo(it)) },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Checklist des zones
        ZoneChecklist(
            zones = uiState.bodyZones,
            currentIndex = uiState.currentZoneIndex,
            onZoneSelected = { onEvent(MassagePlayerEvent.SelectZone(it)) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Contrôles principaux
        MassageMainControls(
            isPlaying = uiState.playerState.isPlaying,
            onPlayPause = { onEvent(MassagePlayerEvent.TogglePlayPause) },
            onPrevious = { onEvent(MassagePlayerEvent.PreviousZone) },
            onNext = { onEvent(MassagePlayerEvent.NextZone) },
            onRepeat = { onEvent(MassagePlayerEvent.RepeatCurrentZone) },
            onComplete = { onEvent(MassagePlayerEvent.CompleteCurrentZone) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BodyMapView(
    zones: List<BodyZone>,
    currentZoneIndex: Int,
    onZoneSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PlayerColors.Massage.surface,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Silhouette simplifiée avec zones cliquables
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Tête (placeholder)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                // Zones du corps
                zones.forEachIndexed { index, zone ->
                    val isActive = index == currentZoneIndex
                    val backgroundColor = when (zone.state) {
                        ZoneState.COMPLETED -> PlayerColors.Massage.zoneCompleted
                        ZoneState.ACTIVE -> PlayerColors.Massage.zoneActive
                        ZoneState.PENDING -> PlayerColors.Massage.zonePending
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = backgroundColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onZoneSelected(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(zone.icon)
                            Text(
                                text = zone.name,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                            if (zone.state == ZoneState.COMPLETED) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = PlayerColors.Massage.accentDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZoneTimerCard(
    zoneName: String,
    zoneIcon: String,
    timeRemaining: Long,
    repetitionsRemaining: Int,
    targetRepetitions: Int
) {
    val minutes = (timeRemaining / 60000).toInt()
    val seconds = ((timeRemaining % 60000) / 1000).toInt()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PlayerColors.Massage.zoneActive.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(zoneIcon, fontSize = 28.sp)
                Column {
                    Text(
                        text = zoneName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Répétition ${targetRepetitions - repetitionsRemaining + 1}/$targetRepetitions",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = PlayerColors.Massage.accent
            ) {
                Text(
                    text = "%d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PressureIndicator(
    currentLevel: PressureLevel,
    recommendedLevel: PressureLevel,
    onLevelChanged: (PressureLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pression",
                style = MaterialTheme.typography.labelMedium,
                color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
            )
            Text(
                text = "Recommandée : ${recommendedLevel.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = PlayerColors.Massage.accent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PressureLevel.values().forEach { level ->
                val isSelected = level == currentLevel
                val color = when (level) {
                    PressureLevel.LOW -> PlayerColors.Massage.pressureLow
                    PressureLevel.MEDIUM -> PlayerColors.Massage.pressureMedium
                    PressureLevel.HIGH -> PlayerColors.Massage.pressureHigh
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) color else color.copy(alpha = 0.3f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLevelChanged(level) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else PlayerColors.Massage.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionCard(instruction: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Massage.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = PlayerColors.Massage.accent
            )
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = PlayerColors.Massage.onBackground
            )
        }
    }
}

@Composable
private fun ZoneChecklist(
    zones: List<BodyZone>,
    currentIndex: Int,
    onZoneSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = zones.count { it.state == ZoneState.COMPLETED }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Zones terminées : $completedCount/${zones.size}",
            style = MaterialTheme.typography.labelMedium,
            color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(zones) { index, zone ->
                val isActive = index == currentIndex

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (zone.state) {
                        ZoneState.COMPLETED -> PlayerColors.Massage.zoneCompleted
                        ZoneState.ACTIVE -> PlayerColors.Massage.accent
                        ZoneState.PENDING -> PlayerColors.Massage.zonePending
                    },
                    modifier = Modifier.clickable { onZoneSelected(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        when (zone.state) {
                            ZoneState.COMPLETED -> Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PlayerColors.Massage.accentDark
                            )
                            ZoneState.ACTIVE -> Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            else -> {}
                        }
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = when (zone.state) {
                                ZoneState.ACTIVE -> Color.White
                                else -> PlayerColors.Massage.onBackground
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MassageMainControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, "Zone précédente", modifier = Modifier.size(28.dp))
        }
        IconButton(onClick = onRepeat) {
            Icon(Icons.Default.Replay, "Répéter", modifier = Modifier.size(24.dp))
        }
        FloatingActionButton(
            onClick = onPlayPause,
            containerColor = PlayerColors.Massage.accent,
            contentColor = Color.White
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lecture",
                modifier = Modifier.size(32.dp)
            )
        }
        IconButton(onClick = onComplete) {
            Icon(Icons.Default.Check, "Terminer zone", modifier = Modifier.size(24.dp))
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.SkipNext, "Zone suivante", modifier = Modifier.size(28.dp))
        }
    }
}
