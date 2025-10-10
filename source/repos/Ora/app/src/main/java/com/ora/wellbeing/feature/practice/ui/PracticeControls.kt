package com.ora.wellbeing.feature.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ora.wellbeing.core.domain.ambient.AmbientType
import com.ora.wellbeing.feature.practice.player.PlayerState
import kotlin.math.roundToInt

@Composable
fun PracticeControls(
    playerState: PlayerState,
    onEvent: (PracticeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(16.dp)
    ) {
        // Timer & Progress
        TimerSection(
            currentPosition = playerState.currentPosition,
            duration = playerState.duration
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Play/Pause & Seek Controls
        PlaybackControls(
            isPlaying = playerState.isPlaying,
            onPlayPause = { onEvent(PracticeUiEvent.TogglePlayPause) },
            onSeekBackward = { onEvent(PracticeUiEvent.SeekBackward) },
            onSeekForward = { onEvent(PracticeUiEvent.SeekForward) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ambient Controls (collapsible)
        var showAmbient by remember { mutableStateOf(false) }
        AmbientSection(
            expanded = showAmbient,
            onToggle = { showAmbient = !showAmbient },
            onEvent = onEvent
        )
    }
}

@Composable
private fun TimerSection(
    currentPosition: Long,
    duration: Long
) {
    Column {
        // Progress bar
        val progress = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else 0f

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Seek backward -15s
        FilledTonalIconButton(
            onClick = onSeekBackward,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Replay,
                contentDescription = "Reculer 15s",
                modifier = Modifier.size(24.dp)
            )
        }

        // Play/Pause (large)
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }

        // Seek forward +15s
        FilledTonalIconButton(
            onClick = onSeekForward,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Forward30,
                contentDescription = "Avancer 15s",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmbientSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    onEvent: (PracticeUiEvent) -> Unit
) {
    Column {
        // Header
        Surface(
            onClick = onToggle,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ambiance sonore",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Réduire" else "Développer"
                )
            }
        }

        // Expanded content
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            // Ambient track selector
            AmbientTrackSelector(
                onTrackSelected = { type -> onEvent(PracticeUiEvent.SelectAmbientTrack(type)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Volume slider
            var volume by remember { mutableStateOf(0.3f) }
            Column {
                Text(
                    text = "Volume: ${(volume * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onEvent(PracticeUiEvent.SetAmbientVolume(it))
                    },
                    valueRange = 0f..1f
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Crossfade toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transition douce (crossfade)",
                    style = MaterialTheme.typography.bodySmall
                )
                var crossfade by remember { mutableStateOf(true) }
                Switch(
                    checked = crossfade,
                    onCheckedChange = {
                        crossfade = it
                        onEvent(PracticeUiEvent.ToggleCrossfade(it))
                    }
                )
            }
        }
    }
}

@Composable
private fun AmbientTrackSelector(
    onTrackSelected: (AmbientType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Choisir une ambiance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        val ambientTypes = listOf(
            AmbientType.OCEAN,
            AmbientType.RAIN,
            AmbientType.FOREST,
            AmbientType.BIRDS,
            AmbientType.FIREPLACE,
            AmbientType.NONE
        )

        // Grid of chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var selectedType by remember { mutableStateOf<AmbientType?>(null) }

            ambientTypes.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        selectedType = type
                        onTrackSelected(type)
                    },
                    label = { Text("${type.emoji} ${type.displayName}") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Formate le temps en MM:SS
 */
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}

@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple FlowRow implementation (Material3 has built-in but might need API level)
    Column(modifier = modifier) {
        content()
    }
}
