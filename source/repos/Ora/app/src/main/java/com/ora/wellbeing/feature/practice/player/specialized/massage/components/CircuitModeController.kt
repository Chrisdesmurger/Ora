package com.ora.wellbeing.feature.practice.player.specialized.massage.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.player.specialized.massage.BodyZone
import com.ora.wellbeing.feature.practice.player.specialized.massage.ZoneState

/**
 * Circuit Mode Controller for automatic zone transitions
 *
 * Features:
 * - Enable/disable circuit mode
 * - Configure pause duration between zones
 * - Visual circuit progress
 * - Zone reordering
 */
@Composable
fun CircuitModeController(
    isEnabled: Boolean,
    zones: List<BodyZone>,
    currentZoneIndex: Int,
    pauseDurationMs: Long,
    isPaused: Boolean,
    pauseTimeRemainingMs: Long,
    onToggleEnabled: (Boolean) -> Unit,
    onPauseDurationChanged: (Long) -> Unit,
    onZoneSelected: (Int) -> Unit,
    onSkipPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PlayerColors.Massage.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircuitModeIcon(isEnabled = isEnabled, isActive = isEnabled && !isPaused)

                    Column {
                        Text(
                            text = "Mode circuit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PlayerColors.Massage.onBackground
                        )
                        Text(
                            text = if (isEnabled)
                                "Enchainement automatique"
                            else
                                "Transitions manuelles",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlayerColors.Massage.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEnabled) {
                        IconButton(
                            onClick = { showSettings = !showSettings },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Parametres",
                                tint = PlayerColors.Massage.accent
                            )
                        }
                    }

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PlayerColors.Massage.accent,
                            checkedTrackColor = PlayerColors.Massage.accent.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // Pause indicator
            AnimatedVisibility(
                visible = isPaused && isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                PauseIndicator(
                    timeRemainingMs = pauseTimeRemainingMs,
                    onSkip = onSkipPause
                )
            }

            // Circuit progress visualization
            if (isEnabled) {
                CircuitProgressBar(
                    zones = zones,
                    currentZoneIndex = currentZoneIndex,
                    onZoneSelected = onZoneSelected
                )
            }

            // Settings panel
            AnimatedVisibility(
                visible = showSettings && isEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                CircuitSettings(
                    pauseDurationMs = pauseDurationMs,
                    onPauseDurationChanged = onPauseDurationChanged
                )
            }
        }
    }
}

@Composable
private fun CircuitModeIcon(
    isEnabled: Boolean,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circuit")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isEnabled)
                    PlayerColors.Massage.accent.copy(alpha = 0.2f)
                else
                    Color.Gray.copy(alpha = 0.1f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Loop,
            contentDescription = null,
            tint = if (isEnabled) PlayerColors.Massage.accent else Color.Gray,
            modifier = Modifier
                .size(24.dp)
                .then(
                    if (isActive)
                        Modifier.rotate(rotation)
                    else
                        Modifier
                )
        )
    }
}

@Composable
private fun PauseIndicator(
    timeRemainingMs: Long,
    onSkip: () -> Unit
) {
    val seconds = (timeRemainingMs / 1000).toInt()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Massage.accent.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Pause,
                    contentDescription = null,
                    tint = PlayerColors.Massage.accent,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = "Pause entre les zones",
                        style = MaterialTheme.typography.labelMedium,
                        color = PlayerColors.Massage.onBackground
                    )
                    Text(
                        text = "${seconds}s restantes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlayerColors.Massage.accent
                    )
                }
            }

            TextButton(onClick = onSkip) {
                Text(
                    text = "Passer",
                    color = PlayerColors.Massage.accent
                )
            }
        }
    }
}

@Composable
private fun CircuitProgressBar(
    zones: List<BodyZone>,
    currentZoneIndex: Int,
    onZoneSelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(zones) { index, zone ->
            CircuitZoneChip(
                zone = zone,
                index = index,
                isActive = index == currentZoneIndex,
                onClick = { onZoneSelected(index) }
            )

            // Arrow between zones
            if (index < zones.size - 1) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = PlayerColors.Massage.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CircuitZoneChip(
    zone: BodyZone,
    index: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when (zone.state) {
        ZoneState.COMPLETED -> PlayerColors.Massage.zoneCompleted
        ZoneState.ACTIVE -> PlayerColors.Massage.accent
        ZoneState.PENDING -> PlayerColors.Massage.zonePending
    }

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        tonalElevation = if (isActive) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zone number
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color.White.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else PlayerColors.Massage.onBackground
                )
            }

            // Zone icon and name
            Text(zone.icon)
            Text(
                text = zone.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color.White else PlayerColors.Massage.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Status icon
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
        }
    }
}

@Composable
private fun CircuitSettings(
    pauseDurationMs: Long,
    onPauseDurationChanged: (Long) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Massage.zonePending.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Duree de pause entre les zones",
                style = MaterialTheme.typography.labelMedium,
                color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
            )

            // Preset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0L to "Sans",
                    3000L to "3s",
                    5000L to "5s",
                    10000L to "10s",
                    15000L to "15s"
                ).forEach { (duration, label) ->
                    FilterChip(
                        selected = pauseDurationMs == duration,
                        onClick = { onPauseDurationChanged(duration) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PlayerColors.Massage.accent,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Custom slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Personnaliser",
                        style = MaterialTheme.typography.labelSmall,
                        color = PlayerColors.Massage.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${pauseDurationMs / 1000}s",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PlayerColors.Massage.accent
                    )
                }

                Slider(
                    value = pauseDurationMs.toFloat(),
                    onValueChange = { onPauseDurationChanged(it.toLong()) },
                    valueRange = 0f..30000f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = PlayerColors.Massage.accent,
                        activeTrackColor = PlayerColors.Massage.accent
                    )
                )
            }
        }
    }
}

/**
 * Compact circuit mode toggle
 */
@Composable
fun CircuitModeToggle(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled)
            PlayerColors.Massage.accent
        else
            PlayerColors.Massage.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Loop,
                contentDescription = null,
                tint = if (isEnabled) Color.White else PlayerColors.Massage.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Circuit",
                style = MaterialTheme.typography.labelMedium,
                color = if (isEnabled) Color.White else PlayerColors.Massage.onBackground
            )
        }
    }
}
