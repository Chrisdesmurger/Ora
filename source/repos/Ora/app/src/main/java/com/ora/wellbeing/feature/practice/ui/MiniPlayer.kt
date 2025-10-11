package com.ora.wellbeing.feature.practice.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ora.wellbeing.feature.practice.player.PlayerState

/**
 * Mini player affiché en bas de l'écran quand le player est minimisé
 */
@Composable
fun MiniPlayer(
    practice: com.ora.wellbeing.core.domain.practice.Practice?,
    playerState: PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipForward: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (practice == null) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onExpand),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress bar at top
            val progress = if (playerState.duration > 0) {
                (playerState.currentPosition.toFloat() / playerState.duration.toFloat()).coerceIn(0f, 1f)
            } else 0f

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )

            // Content row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail + Info
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    AsyncImage(
                        model = practice.thumbnailUrl,
                        contentDescription = practice.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Text info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = practice.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = practice.instructor ?: practice.discipline.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip forward
                    IconButton(
                        onClick = onSkipForward,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Forward30,
                            contentDescription = "Avancer 15s",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play/Pause
                    FilledIconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Lecture",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Close
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mini player avec animation d'entrée/sortie
 */
@Composable
fun AnimatedMiniPlayer(
    visible: Boolean,
    practice: com.ora.wellbeing.core.domain.practice.Practice?,
    playerState: PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipForward: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && practice != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        MiniPlayer(
            practice = practice,
            playerState = playerState,
            onExpand = onExpand,
            onPlayPause = onPlayPause,
            onSkipForward = onSkipForward,
            onClose = onClose
        )
    }
}

/**
 * Format du temps pour le mini player
 */
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
