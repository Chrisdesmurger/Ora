package com.ora.wellbeing.feature.practice.player.specialized.massage.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ora.wellbeing.feature.practice.player.specialized.PlayerColors
import com.ora.wellbeing.feature.practice.player.specialized.massage.BodyZone
import com.ora.wellbeing.feature.practice.player.specialized.massage.ZoneState
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Interactive Body Map component
 *
 * Features:
 * - Rotatable body model (simulated 3D with Canvas)
 * - Pinch-to-zoom
 * - Tap to select zones
 * - Animated massage movements
 * - Acupressure point annotations
 */
@Composable
fun BodyMap3D(
    zones: List<BodyZone>,
    currentZoneIndex: Int,
    acupressurePoints: List<AcupressurePoint> = defaultAcupressurePoints(),
    showAcupressurePoints: Boolean = true,
    onZoneSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Animation for massage movements
    val infiniteTransition = rememberInfiniteTransition(label = "massage_animation")
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Massage direction animation
    val massageOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "massage_direction"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = PlayerColors.Massage.surface,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-200f, 200f),
                                y = (offset.y + pan.y).coerceIn(-200f, 200f)
                            )
                        }
                    }
                    .pointerInput(zones) {
                        detectTapGestures { tapOffset ->
                            // Calculate which zone was tapped
                            val zoneIndex = getZoneAtPosition(tapOffset, size.width.toFloat(), size.height.toFloat(), zones)
                            if (zoneIndex >= 0) {
                                onZoneSelected(zoneIndex)
                            }
                        }
                    }
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                translate(offset.x + centerX, offset.y + centerY) {
                    scale(scale) {
                        rotate(rotation) {
                            // Draw body silhouette
                            drawBodySilhouette(
                                zones = zones,
                                currentZoneIndex = currentZoneIndex,
                                pulseScale = pulseAnimation,
                                massageOffset = massageOffset,
                                centerX = 0f,
                                centerY = 0f
                            )

                            // Draw acupressure points
                            if (showAcupressurePoints) {
                                drawAcupressurePoints(
                                    points = acupressurePoints,
                                    centerX = 0f,
                                    centerY = 0f
                                )
                            }
                        }
                    }
                }
            }

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { scale = (scale * 1.2f).coerceAtMost(3f) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            PlayerColors.Massage.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Zoom in",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { scale = (scale / 1.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            PlayerColors.Massage.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.ZoomOut,
                        contentDescription = "Zoom out",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                        rotation = 0f
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            PlayerColors.Massage.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset view",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Rotation slider
            Slider(
                value = rotation,
                onValueChange = { rotation = it },
                valueRange = -45f..45f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PlayerColors.Massage.accent,
                    activeTrackColor = PlayerColors.Massage.accent
                )
            )
        }
    }
}

/**
 * Draw the body silhouette with zones
 */
private fun DrawScope.drawBodySilhouette(
    zones: List<BodyZone>,
    currentZoneIndex: Int,
    pulseScale: Float,
    massageOffset: Float,
    centerX: Float,
    centerY: Float
) {
    val bodyWidth = 120f
    val bodyHeight = 300f

    // Head
    val headRadius = 35f
    val headY = centerY - bodyHeight / 2 + headRadius
    drawCircle(
        color = Color.LightGray.copy(alpha = 0.6f),
        radius = headRadius,
        center = Offset(centerX, headY)
    )

    // Zone definitions relative to center
    val zoneRegions = mapOf(
        "neck" to ZoneRegion(
            x = centerX,
            y = headY + headRadius + 15f,
            width = 40f,
            height = 30f
        ),
        "shoulders" to ZoneRegion(
            x = centerX,
            y = headY + headRadius + 50f,
            width = bodyWidth + 40f,
            height = 35f
        ),
        "back" to ZoneRegion(
            x = centerX,
            y = headY + headRadius + 100f,
            width = bodyWidth,
            height = 80f
        ),
        "arms" to ZoneRegion(
            x = centerX,
            y = headY + headRadius + 80f,
            width = bodyWidth + 80f,
            height = 100f
        ),
        "hands" to ZoneRegion(
            x = centerX,
            y = headY + headRadius + 180f,
            width = bodyWidth + 100f,
            height = 40f
        )
    )

    // Draw zones
    zones.forEachIndexed { index, zone ->
        val region = zoneRegions[zone.id] ?: return@forEachIndexed
        val isActive = index == currentZoneIndex

        val color = when (zone.state) {
            ZoneState.COMPLETED -> PlayerColors.Massage.zoneCompleted
            ZoneState.ACTIVE -> PlayerColors.Massage.zoneActive
            ZoneState.PENDING -> PlayerColors.Massage.zonePending
        }

        val scale = if (isActive) pulseScale else 1f
        val offsetX = if (isActive) massageOffset else 0f

        // Draw zone area
        drawRoundRect(
            color = color.copy(alpha = 0.7f),
            topLeft = Offset(
                region.x - (region.width * scale) / 2 + offsetX,
                region.y - (region.height * scale) / 2
            ),
            size = Size(region.width * scale, region.height * scale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )

        // Draw zone border for active zone
        if (isActive) {
            drawRoundRect(
                color = PlayerColors.Massage.accent,
                topLeft = Offset(
                    region.x - (region.width * scale) / 2 + offsetX,
                    region.y - (region.height * scale) / 2
                ),
                size = Size(region.width * scale, region.height * scale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                style = Stroke(width = 3f)
            )

            // Draw massage direction arrows
            drawMassageArrows(
                centerX = region.x + offsetX,
                centerY = region.y,
                width = region.width * scale,
                height = region.height * scale
            )
        }
    }

    // Draw body outline
    val bodyPath = Path().apply {
        // Neck
        moveTo(centerX - 20f, headY + headRadius)
        lineTo(centerX + 20f, headY + headRadius)

        // Right shoulder
        lineTo(centerX + 60f, headY + headRadius + 40f)
        lineTo(centerX + 100f, headY + headRadius + 60f)

        // Right arm
        lineTo(centerX + 110f, headY + headRadius + 160f)
        lineTo(centerX + 100f, headRadius + 180f)
        lineTo(centerX + 60f, headY + headRadius + 140f)

        // Right side
        lineTo(centerX + 60f, headY + headRadius + 200f)

        // Bottom
        lineTo(centerX - 60f, headY + headRadius + 200f)

        // Left side
        lineTo(centerX - 60f, headY + headRadius + 140f)
        lineTo(centerX - 100f, headY + headRadius + 180f)
        lineTo(centerX - 110f, headY + headRadius + 160f)

        // Left arm
        lineTo(centerX - 100f, headY + headRadius + 60f)
        lineTo(centerX - 60f, headY + headRadius + 40f)

        // Left shoulder
        lineTo(centerX - 20f, headY + headRadius)

        close()
    }

    drawPath(
        path = bodyPath,
        color = Color.Gray.copy(alpha = 0.3f),
        style = Stroke(width = 2f)
    )
}

/**
 * Draw massage direction arrows
 */
private fun DrawScope.drawMassageArrows(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float
) {
    val arrowPath = Path().apply {
        // Up arrow
        moveTo(centerX, centerY - height / 4)
        lineTo(centerX - 8f, centerY - height / 4 + 8f)
        moveTo(centerX, centerY - height / 4)
        lineTo(centerX + 8f, centerY - height / 4 + 8f)

        // Down arrow
        moveTo(centerX, centerY + height / 4)
        lineTo(centerX - 8f, centerY + height / 4 - 8f)
        moveTo(centerX, centerY + height / 4)
        lineTo(centerX + 8f, centerY + height / 4 - 8f)
    }

    drawPath(
        path = arrowPath,
        color = PlayerColors.Massage.accent,
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )
}

/**
 * Draw acupressure points
 */
private fun DrawScope.drawAcupressurePoints(
    points: List<AcupressurePoint>,
    centerX: Float,
    centerY: Float
) {
    points.forEach { point ->
        // Outer glow
        drawCircle(
            color = PlayerColors.Massage.accent.copy(alpha = 0.3f),
            radius = 12f,
            center = Offset(centerX + point.x, centerY + point.y)
        )

        // Inner point
        drawCircle(
            color = PlayerColors.Massage.accent,
            radius = 6f,
            center = Offset(centerX + point.x, centerY + point.y)
        )
    }
}

/**
 * Get zone at tap position
 */
private fun getZoneAtPosition(
    tapOffset: Offset,
    canvasWidth: Float,
    canvasHeight: Float,
    zones: List<BodyZone>
): Int {
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2
    val headRadius = 35f
    val headY = centerY - 150f + headRadius

    val zoneRegions = mapOf(
        "neck" to ZoneRegion(centerX, headY + headRadius + 15f, 40f, 30f),
        "shoulders" to ZoneRegion(centerX, headY + headRadius + 50f, 160f, 35f),
        "back" to ZoneRegion(centerX, headY + headRadius + 100f, 120f, 80f),
        "arms" to ZoneRegion(centerX, headY + headRadius + 80f, 200f, 100f),
        "hands" to ZoneRegion(centerX, headY + headRadius + 180f, 220f, 40f)
    )

    zones.forEachIndexed { index, zone ->
        val region = zoneRegions[zone.id] ?: return@forEachIndexed
        val left = region.x - region.width / 2
        val right = region.x + region.width / 2
        val top = region.y - region.height / 2
        val bottom = region.y + region.height / 2

        if (tapOffset.x in left..right && tapOffset.y in top..bottom) {
            return index
        }
    }

    return -1
}

/**
 * Zone region data class
 */
private data class ZoneRegion(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * Acupressure point data class
 */
data class AcupressurePoint(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val description: String
)

/**
 * Default acupressure points
 */
fun defaultAcupressurePoints(): List<AcupressurePoint> = listOf(
    AcupressurePoint(
        id = "gb20",
        name = "Feng Chi (GB20)",
        x = -30f,
        y = -100f,
        description = "Point de relaxation a la base du crane"
    ),
    AcupressurePoint(
        id = "gb21",
        name = "Jian Jing (GB21)",
        x = -50f,
        y = -60f,
        description = "Point au milieu de l'epaule pour le stress"
    ),
    AcupressurePoint(
        id = "gb21_r",
        name = "Jian Jing (GB21)",
        x = 50f,
        y = -60f,
        description = "Point au milieu de l'epaule pour le stress"
    ),
    AcupressurePoint(
        id = "li4",
        name = "He Gu (LI4)",
        x = -95f,
        y = 80f,
        description = "Point entre pouce et index pour maux de tete"
    ),
    AcupressurePoint(
        id = "li4_r",
        name = "He Gu (LI4)",
        x = 95f,
        y = 80f,
        description = "Point entre pouce et index pour maux de tete"
    )
)
