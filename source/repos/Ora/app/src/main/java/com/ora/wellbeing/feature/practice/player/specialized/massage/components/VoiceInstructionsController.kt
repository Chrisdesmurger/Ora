package com.ora.wellbeing.feature.practice.player.specialized.massage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

/**
 * Voice Instructions Controller for hands-free mode
 *
 * Features:
 * - Enable/disable voice instructions
 * - Speech rate adjustment
 * - Visual feedback when speaking
 * - Quick settings access
 */
@Composable
fun VoiceInstructionsController(
    isEnabled: Boolean,
    isSpeaking: Boolean,
    speechRate: Float,
    onToggleEnabled: (Boolean) -> Unit,
    onSpeechRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Main control row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice toggle button
            VoiceToggleButton(
                isEnabled = isEnabled,
                isSpeaking = isSpeaking,
                onToggle = { onToggleEnabled(!isEnabled) }
            )

            // Settings button
            if (isEnabled) {
                IconButton(
                    onClick = { showSettings = !showSettings },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            PlayerColors.Massage.surface,
                            CircleShape
                        )
                ) {
                    Icon(
                        if (showSettings) Icons.Default.ExpandLess else Icons.Default.Settings,
                        contentDescription = "Parametres vocaux",
                        tint = PlayerColors.Massage.accent
                    )
                }
            }
        }

        // Settings panel
        AnimatedVisibility(
            visible = showSettings && isEnabled,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VoiceSettingsPanel(
                speechRate = speechRate,
                onSpeechRateChanged = onSpeechRateChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun VoiceToggleButton(
    isEnabled: Boolean,
    isSpeaking: Boolean,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking")
    val speakingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_scale"
    )

    Surface(
        modifier = Modifier
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled)
            PlayerColors.Massage.accent
        else
            PlayerColors.Massage.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Microphone icon with speaking animation
            Box(
                modifier = Modifier
                    .size(if (isSpeaking) (32.dp * speakingScale) else 32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEnabled)
                            Color.White.copy(alpha = 0.2f)
                        else
                            PlayerColors.Massage.accent.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                    contentDescription = null,
                    tint = if (isEnabled) Color.White else PlayerColors.Massage.accent,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = if (isEnabled) "Mode mains libres" else "Instructions vocales",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isEnabled) Color.White else PlayerColors.Massage.onBackground
                )

                if (isEnabled && isSpeaking) {
                    Text(
                        text = "En cours de lecture...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else if (isEnabled) {
                    Text(
                        text = "Actif",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.3f),
                    uncheckedThumbColor = PlayerColors.Massage.accent,
                    uncheckedTrackColor = PlayerColors.Massage.accent.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun VoiceSettingsPanel(
    speechRate: Float,
    onSpeechRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PlayerColors.Massage.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Parametres vocaux",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PlayerColors.Massage.onBackground
            )

            // Speech rate slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vitesse de parole",
                        style = MaterialTheme.typography.labelMedium,
                        color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = getSpeechRateLabel(speechRate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PlayerColors.Massage.accent
                    )
                }

                Slider(
                    value = speechRate,
                    onValueChange = onSpeechRateChanged,
                    valueRange = 0.5f..2f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = PlayerColors.Massage.accent,
                        activeTrackColor = PlayerColors.Massage.accent
                    )
                )

                // Speed presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedPresetChip(
                        label = "Lent",
                        isSelected = speechRate <= 0.75f,
                        onClick = { onSpeechRateChanged(0.75f) }
                    )
                    SpeedPresetChip(
                        label = "Normal",
                        isSelected = speechRate in 0.9f..1.1f,
                        onClick = { onSpeechRateChanged(1f) }
                    )
                    SpeedPresetChip(
                        label = "Rapide",
                        isSelected = speechRate >= 1.25f,
                        onClick = { onSpeechRateChanged(1.5f) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            PlayerColors.Massage.accent
        else
            PlayerColors.Massage.zonePending
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else PlayerColors.Massage.onBackground,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun getSpeechRateLabel(rate: Float): String {
    return when {
        rate <= 0.6f -> "Tres lent"
        rate <= 0.85f -> "Lent"
        rate <= 1.15f -> "Normal"
        rate <= 1.5f -> "Rapide"
        else -> "Tres rapide"
    }
}

/**
 * Compact voice control for inline usage
 */
@Composable
fun VoiceControlCompact(
    isEnabled: Boolean,
    isSpeaking: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_compact")
    val speakingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_alpha"
    )

    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(48.dp)
            .background(
                if (isEnabled)
                    PlayerColors.Massage.accent.copy(
                        alpha = if (isSpeaking) speakingAlpha else 1f
                    )
                else
                    PlayerColors.Massage.surface,
                CircleShape
            )
    ) {
        Icon(
            if (isEnabled) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
            contentDescription = if (isEnabled) "Desactiver les instructions vocales" else "Activer les instructions vocales",
            tint = if (isEnabled) Color.White else PlayerColors.Massage.onBackground.copy(alpha = 0.6f)
        )
    }
}
