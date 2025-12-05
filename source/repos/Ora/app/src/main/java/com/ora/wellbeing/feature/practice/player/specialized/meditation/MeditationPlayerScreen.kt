package com.ora.wellbeing.feature.practice.player.specialized.meditation

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.ui.CustomSeekBar

/**
 * Écran lecteur spécialisé pour Méditation/Respiration
 * - Animation de respiration
 * - Sons ambiants superposables
 * - Mode nuit automatique
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeditationPlayerScreen(
    practiceId: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    viewModel: MeditationPlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    // Couleurs adaptées au mode nuit
    val backgroundColor = if (uiState.isNightMode) {
        PlayerColors.Meditation.nightBackground
    } else {
        PlayerColors.Meditation.background
    }

    val accentColor = if (uiState.isNightMode) {
        PlayerColors.Meditation.nightAccent
    } else {
        PlayerColors.Meditation.accent
    }

    val textColor = if (uiState.isNightMode) {
        PlayerColors.Meditation.nightOnBackground
    } else {
        PlayerColors.Meditation.onBackground
    }

    // Adapter la couleur de la barre d'état au fond du lecteur
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window

        // Application immédiate de la couleur
        SideEffect {
            window.statusBarColor = backgroundColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !uiState.isNightMode
        }

        // Restauration uniquement au démontage
        DisposableEffect(Unit) {
            onDispose {
                window.statusBarColor = Color(0xFFFFF5F0).toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        if (uiState.isNightMode) Color(0xFF0D0D1A) else PlayerColors.Meditation.backgroundGradientEnd
                    )
                )
            )
    ) {
        when {
            uiState.isLoading -> MeditationLoadingState(accentColor)
            uiState.error != null -> MeditationErrorState(
                error = uiState.error ?: "Erreur inconnue",
                onRetry = { viewModel.onEvent(MeditationPlayerEvent.Retry) },
                onBack = onBack
            )
            uiState.practice != null -> MeditationPlayerContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onBack = onBack,
                backgroundColor = backgroundColor,
                accentColor = accentColor,
                textColor = textColor
            )
        }
    }
}

@Composable
private fun MeditationLoadingState(accentColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = accentColor)
            Text(
                text = "Préparation de votre méditation...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MeditationErrorState(
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
private fun MeditationPlayerContent(
    uiState: MeditationPlayerState,
    onEvent: (MeditationPlayerEvent) -> Unit,
    onBack: () -> Unit,
    backgroundColor: Color,
    accentColor: Color,
    textColor: Color
) {
    val practice = uiState.practice ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Retour", tint = textColor)
            }

            IconButton(onClick = { onEvent(MeditationPlayerEvent.ToggleNightMode) }) {
                Icon(
                    if (uiState.isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    "Mode nuit",
                    tint = accentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Animation de respiration centrale
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            BreathingAnimation(
                phase = uiState.breathingPhase,
                progress = uiState.breathingCycleProgress,
                isPlaying = uiState.playerState.isPlaying,
                accentColor = accentColor,
                thumbnailUrl = practice.thumbnailUrl
            )
        }

        // Indicateur de phase
        if (uiState.breathingPhase != BreathingPhase.IDLE) {
            Text(
                text = uiState.breathingPhase.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = accentColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        // Indicateur de progression
        PhaseIndicator(
            currentPhase = uiState.currentPhaseIndex,
            totalPhases = uiState.totalPhases,
            phaseName = uiState.phaseName,
            accentColor = accentColor,
            textColor = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seek bar
        CustomSeekBar(
            currentPosition = uiState.playerState.currentPosition,
            duration = uiState.playerState.duration,
            onSeek = { onEvent(MeditationPlayerEvent.SeekTo(it)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contrôle principal
        FloatingActionButton(
            onClick = { onEvent(MeditationPlayerEvent.TogglePlayPause) },
            containerColor = accentColor,
            contentColor = Color.White,
            modifier = Modifier.size(72.dp)
        ) {
            Icon(
                if (uiState.playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (uiState.playerState.isPlaying) "Pause" else "Lecture",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sons ambiants
        AmbientSoundSelector(
            sounds = uiState.ambientSounds,
            activeSound = uiState.activeAmbientSound,
            onSoundSelected = { onEvent(MeditationPlayerEvent.SetAmbientSound(it)) },
            accentColor = accentColor,
            textColor = textColor
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BreathingAnimation(
    phase: BreathingPhase,
    progress: Float,
    isPlaying: Boolean,
    accentColor: Color,
    thumbnailUrl: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    // Animation de pulsation quand en lecture mais pas d'exercice de respiration
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Calcul de l'échelle selon la phase de respiration
    val breathingScale = when (phase) {
        BreathingPhase.IDLE -> if (isPlaying) pulseScale else 1f
        BreathingPhase.INHALE -> 1f + (0.3f * progress)
        BreathingPhase.HOLD_IN -> 1.3f
        BreathingPhase.EXHALE -> 1.3f - (0.3f * progress)
        BreathingPhase.HOLD_OUT -> 1f
    }

    Box(contentAlignment = Alignment.Center) {
        // Cercle extérieur (glow)
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(breathingScale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f))
        )

        // Cercle principal
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(breathingScale)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            // Image de thumbnail ou icône
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            )

            // Overlay avec icône
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseIndicator(
    currentPhase: Int,
    totalPhases: Int,
    phaseName: String,
    accentColor: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Points de progression
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalPhases) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPhase) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentPhase) accentColor
                            else accentColor.copy(alpha = 0.3f)
                        )
                )
            }
        }

        if (phaseName.isNotEmpty()) {
            Text(
                text = "Phase ${currentPhase + 1}/$totalPhases - $phaseName",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun AmbientSoundSelector(
    sounds: List<AmbientSound>,
    activeSound: AmbientSound?,
    onSoundSelected: (AmbientSound?) -> Unit,
    accentColor: Color,
    textColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Sons ambiants",
            style = MaterialTheme.typography.labelMedium,
            color = textColor.copy(alpha = 0.7f)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sounds) { sound ->
                val isActive = sound.id == activeSound?.id

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isActive) accentColor else accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.clickable {
                        onSoundSelected(if (isActive) null else sound)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sound.icon,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sound.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) Color.White else textColor
                        )
                    }
                }
            }
        }
    }
}

