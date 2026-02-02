package com.ora.wellbeing.feature.practice.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Custom SeekBar avec preview
 */
@Composable
fun CustomSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onSeekStart: (() -> Unit)? = null,
    onSeekEnd: (() -> Unit)? = null
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val progress = if (duration > 0) {
        if (isDragging) dragPosition else (currentPosition.toFloat() / duration.toFloat())
    } else 0f

    Column(modifier = modifier) {
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(if (isDragging) (duration * dragPosition).toLong() else currentPosition),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
                color = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Seek bar
        val density = LocalDensity.current
        val barHeight = 6.dp
        val thumbRadius = 8.dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(thumbRadius * 2)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            onSeekStart?.invoke()
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((duration * dragPosition).toLong())
                            onSeekEnd?.invoke()
                        },
                        onDrag = { change, _ ->
                            val width = size.width
                            dragPosition = (change.position.x / width).coerceIn(0f, 1f)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val newProgress = (offset.x / width).coerceIn(0f, 1f)
                        onSeek((duration * newProgress).toLong())
                    }
                }
        ) {
            val barHeightPx = barHeight.toPx()
            val thumbRadiusPx = thumbRadius.toPx()
            val centerY = size.height / 2

            // Background track
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = barHeightPx,
                cap = StrokeCap.Round
            )

            // Progress track
            val progressWidth = size.width * progress
            drawLine(
                color = Color(0xFFF18D5C), // Ora primary color
                start = Offset(0f, centerY),
                end = Offset(progressWidth, centerY),
                strokeWidth = barHeightPx,
                cap = StrokeCap.Round
            )

            // Thumb
            drawCircle(
                color = if (isDragging) Color(0xFFF18D5C) else Color.White,
                radius = if (isDragging) thumbRadiusPx * 1.2f else thumbRadiusPx,
                center = Offset(progressWidth, centerY)
            )

            // Thumb shadow
            if (isDragging) {
                drawCircle(
                    color = Color(0xFFF18D5C).copy(alpha = 0.3f),
                    radius = thumbRadiusPx * 2f,
                    center = Offset(progressWidth, centerY)
                )
            }
        }
    }
}

/**
 * SeekBar simple (Material 3 Slider wrapper)
 */
@Composable
fun SimpleSeekBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(modifier = modifier) {
        androidx.compose.material3.Slider(
            value = progress,
            onValueChange = { newProgress ->
                onSeek((duration * newProgress).toLong())
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

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

/**
 * Formate le temps en MM:SS
 */
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000).toInt()
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}

/**
 * SeekBar avec marqueurs de chapitre
 */
@Composable
fun ChapterSeekBar(
    currentPosition: Long,
    duration: Long,
    chapters: List<Chapter>,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) {
        (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(modifier = modifier) {
        // Current chapter indicator
        val currentChapter = chapters.findLast { it.startTime <= currentPosition }
        if (currentChapter != null) {
            Text(
                text = currentChapter.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Seek bar with chapter markers
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.Slider(
                value = progress,
                onValueChange = { newProgress ->
                    onSeek((duration * newProgress).toLong())
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Chapter markers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chapters.forEach { chapter ->
                    val chapterProgress = if (duration > 0) {
                        chapter.startTime.toFloat() / duration.toFloat()
                    } else 0f

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(12.dp)
                            .offset(x = (chapterProgress * 100).dp)
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.7f),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Représente un chapitre dans la vidéo/audio
 */
data class Chapter(
    val title: String,
    val startTime: Long // in milliseconds
)
