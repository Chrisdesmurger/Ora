package com.ora.wellbeing.feature.practice.player.specialized.massage.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors

/**
 * Overlay component showing acupressure points with descriptions
 *
 * Features:
 * - List of acupressure points
 * - Tap to see description
 * - Visual indicators on body map
 * - Pulse animation for active points
 */
@Composable
fun AcupressurePointsOverlay(
    points: List<AcupressurePoint>,
    selectedPointId: String?,
    onPointSelected: (String?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPoint = points.find { it.id == selectedPointId }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Points d'acupression",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Points list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(points) { point ->
                    AcupressurePointCard(
                        point = point,
                        isSelected = point.id == selectedPointId,
                        onClick = {
                            onPointSelected(if (point.id == selectedPointId) null else point.id)
                        }
                    )
                }
            }

            // Selected point detail
            AnimatedVisibility(
                visible = selectedPoint != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                selectedPoint?.let { point ->
                    AcupressurePointDetail(
                        point = point,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AcupressurePointCard(
    point: AcupressurePoint,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            PlayerColors.Massage.accent.copy(alpha = 0.3f)
        else
            PlayerColors.Massage.surface.copy(alpha = 0.9f),
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated point indicator
            Box(
                modifier = Modifier
                    .size(if (isSelected) (40.dp * pulse) else 40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            PlayerColors.Massage.accent
                        else
                            PlayerColors.Massage.accent.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) PlayerColors.Massage.accent else PlayerColors.Massage.onBackground
                )
                Text(
                    text = point.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PlayerColors.Massage.onBackground.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Details",
                    tint = PlayerColors.Massage.accent
                )
            }
        }
    }
}

@Composable
private fun AcupressurePointDetail(
    point: AcupressurePoint,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PlayerColors.Massage.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Point icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PlayerColors.Massage.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = point.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PlayerColors.Massage.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = point.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PlayerColors.Massage.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Technique instruction
            TechniqueInstruction(point = point)
        }
    }
}

@Composable
private fun TechniqueInstruction(point: AcupressurePoint) {
    val technique = getPointTechnique(point.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PlayerColors.Massage.zonePending.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Technique",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = PlayerColors.Massage.accent
        )

        Spacer(modifier = Modifier.height(8.dp))

        technique.steps.forEachIndexed { index, step ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PlayerColors.Massage.accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PlayerColors.Massage.accent
                    )
                }

                Text(
                    text = step,
                    style = MaterialTheme.typography.bodySmall,
                    color = PlayerColors.Massage.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Duree: ${technique.durationSeconds} secondes",
            style = MaterialTheme.typography.labelSmall,
            color = PlayerColors.Massage.onBackground.copy(alpha = 0.6f)
        )
    }
}

/**
 * Technique data class
 */
private data class PointTechnique(
    val steps: List<String>,
    val durationSeconds: Int
)

/**
 * Get technique for a specific point
 */
private fun getPointTechnique(pointId: String): PointTechnique {
    return when (pointId) {
        "gb20" -> PointTechnique(
            steps = listOf(
                "Placez vos pouces a la base du crane",
                "Appuyez fermement pendant 30 secondes",
                "Effectuez de petits mouvements circulaires",
                "Relacher progressivement"
            ),
            durationSeconds = 60
        )
        "gb21", "gb21_r" -> PointTechnique(
            steps = listOf(
                "Localisez le point au milieu de l'epaule",
                "Appuyez avec le pouce ou deux doigts",
                "Maintenez une pression ferme mais confortable",
                "Respirez profondement pendant la pression"
            ),
            durationSeconds = 45
        )
        "li4", "li4_r" -> PointTechnique(
            steps = listOf(
                "Pincez entre le pouce et l'index",
                "Appuyez vers l'os du metacarpe",
                "Maintenez pendant 30-60 secondes",
                "Alternez les deux mains"
            ),
            durationSeconds = 60
        )
        else -> PointTechnique(
            steps = listOf(
                "Localisez le point",
                "Appuyez fermement",
                "Maintenez 30 secondes",
                "Relacher doucement"
            ),
            durationSeconds = 30
        )
    }
}

/**
 * Compact acupressure points toggle button
 */
@Composable
fun AcupressureToggleButton(
    isVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color = if (isVisible)
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
                Icons.Default.TouchApp,
                contentDescription = null,
                tint = if (isVisible) Color.White else PlayerColors.Massage.accent,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Points",
                style = MaterialTheme.typography.labelMedium,
                color = if (isVisible) Color.White else PlayerColors.Massage.onBackground
            )
        }
    }
}
