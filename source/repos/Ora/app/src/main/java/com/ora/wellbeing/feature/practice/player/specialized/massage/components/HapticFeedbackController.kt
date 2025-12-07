package com.ora.wellbeing.feature.practice.player.specialized.massage.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.player.specialized.massage.PressureLevel

/**
 * Haptic Feedback Controller component
 *
 * Features:
 * - Enable/disable haptic feedback
 * - Intensity adjustment
 * - Visual pressure level guide
 * - Real-time haptic preview
 */
@Composable
fun HapticFeedbackController(
    isEnabled: Boolean,
    intensity: Float,
    currentPressureLevel: PressureLevel,
    onToggleEnabled: (Boolean) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    onTestHaptic: (PressureLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            Icons.Default.Vibration,
                            contentDescription = null,
                            tint = if (isEnabled)
                                PlayerColors.Massage.accent
                            else
                                Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Retour haptique",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PlayerColors.Massage.onBackground
                        )
                        Text(
                            text = if (isEnabled) "Actif - ${getIntensityLabel(intensity)}" else "Desactive",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlayerColors.Massage.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PlayerColors.Massage.accent,
                            checkedTrackColor = PlayerColors.Massage.accent.copy(alpha = 0.3f)
                        )
                    )

                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = PlayerColors.Massage.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            // Expanded settings
            if (expanded && isEnabled) {
                Divider(color = PlayerColors.Massage.onBackground.copy(alpha = 0.1f))

                // Intensity slider
                Column {
                    Text(
                        text = "Intensite des vibrations",
                        style = MaterialTheme.typography.labelMedium,
                        color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
                    )

                    Slider(
                        value = intensity,
                        onValueChange = onIntensityChanged,
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = PlayerColors.Massage.accent,
                            activeTrackColor = PlayerColors.Massage.accent
                        )
                    )

                    // Intensity indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Leger",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlayerColors.Massage.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Fort",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlayerColors.Massage.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pressure level test buttons
                Text(
                    text = "Tester les niveaux de pression",
                    style = MaterialTheme.typography.labelMedium,
                    color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PressureLevel.values().forEach { level ->
                        PressureTestButton(
                            level = level,
                            isCurrentLevel = level == currentPressureLevel,
                            onClick = { onTestHaptic(level) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PressureTestButton(
    level: PressureLevel,
    isCurrentLevel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        PressureLevel.LOW -> PlayerColors.Massage.pressureLow
        PressureLevel.MEDIUM -> PlayerColors.Massage.pressureMedium
        PressureLevel.HIGH -> PlayerColors.Massage.pressureHigh
    }

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isCurrentLevel) color else color.copy(alpha = 0.3f),
        tonalElevation = if (isCurrentLevel) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Pressure level indicator bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val barCount = when (level) {
                    PressureLevel.LOW -> 1
                    PressureLevel.MEDIUM -> 2
                    PressureLevel.HIGH -> 3
                }

                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(if (index < barCount) 16.dp else 8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index < barCount)
                                    if (isCurrentLevel) Color.White else color
                                else
                                    Color.Gray.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Text(
                text = level.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrentLevel) Color.White else PlayerColors.Massage.onBackground
            )
        }
    }
}

private fun getIntensityLabel(intensity: Float): String {
    return when {
        intensity <= 0.3f -> "Leger"
        intensity <= 0.6f -> "Moyen"
        else -> "Fort"
    }
}

/**
 * Compact haptic toggle for inline usage
 */
@Composable
fun HapticToggleCompact(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(48.dp)
            .background(
                if (isEnabled)
                    PlayerColors.Massage.accent
                else
                    PlayerColors.Massage.surface,
                CircleShape
            )
    ) {
        Icon(
            Icons.Default.Vibration,
            contentDescription = if (isEnabled) "Desactiver vibrations" else "Activer vibrations",
            tint = if (isEnabled) Color.White else PlayerColors.Massage.onBackground.copy(alpha = 0.6f)
        )
    }
}

/**
 * Pressure level indicator with haptic feedback trigger
 */
@Composable
fun PressureLevelIndicator(
    currentLevel: PressureLevel,
    recommendedLevel: PressureLevel,
    hapticEnabled: Boolean,
    onLevelChanged: (PressureLevel) -> Unit,
    onHapticTriggered: (PressureLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

            if (hapticEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Vibration,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = PlayerColors.Massage.accent
                    )
                    Text(
                        text = "Recommandee: ${recommendedLevel.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PlayerColors.Massage.accent
                    )
                }
            } else {
                Text(
                    text = "Recommandee: ${recommendedLevel.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = PlayerColors.Massage.accent
                )
            }
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
                        .clickable {
                            onLevelChanged(level)
                            if (hapticEnabled) {
                                onHapticTriggered(level)
                            }
                        }
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
