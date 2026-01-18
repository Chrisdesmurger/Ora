package com.ora.wellbeing.ui.components.content

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ora.wellbeing.ui.theme.OraTheme
import com.ora.wellbeing.ui.theme.oraColors

/**
 * Types de contenu supportés
 */
enum class ContentType {
    Yoga,
    Meditation,
    Breathing,
    Pilates,
    AutoMassage;

    @Composable
    fun getColor(): Color {
        return when (this) {
            Yoga -> MaterialTheme.oraColors.yogaPrimary
            Meditation -> MaterialTheme.oraColors.meditationPrimary
            Breathing -> MaterialTheme.oraColors.breathingPrimary
            Pilates -> MaterialTheme.oraColors.pilatesPrimary
            AutoMassage -> MaterialTheme.colorScheme.tertiary
        }
    }

    fun getIcon(): ImageVector {
        return when (this) {
            Yoga -> Icons.Default.SelfImprovement
            Meditation -> Icons.Default.Spa
            Breathing -> Icons.Default.Air
            Pilates -> Icons.Default.FitnessCenter
            AutoMassage -> Icons.Default.Healing
        }
    }
}

/**
 * Niveaux de difficulté
 */
enum class DifficultyLevel {
    Beginner,
    Intermediate,
    Advanced;

    @Composable
    fun getDisplayText(): String {
        return when (this) {
            Beginner -> "Débutant"
            Intermediate -> "Intermédiaire"
            Advanced -> "Avancé"
        }
    }

    fun getStars(): Int {
        return when (this) {
            Beginner -> 1
            Intermediate -> 2
            Advanced -> 3
        }
    }
}

/**
 * Card de contenu principal pour la bibliothèque
 */
@Composable
fun ContentCard(
    title: String,
    duration: Int, // en minutes
    contentType: ContentType,
    difficulty: DifficultyLevel,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isFavorite: Boolean = false,
    isCompleted: Boolean = false,
    likes: Int? = null,
    onFavoriteClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val typeColor = contentType.getColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                    ?: run { this.contentDescription = "Contenu $title, durée $duration minutes, ${difficulty.getDisplayText()}" }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Thumbnail avec overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // Image thumbnail
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = contentType.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                // Gradient overlay pour lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Top overlays row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Type badge
                    Surface(
                        color = typeColor.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = contentType.getIcon(),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = contentType.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Actions row
                    Row {
                        // Favorite button
                        if (onFavoriteClick != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { onFavoriteClick() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isFavorite) Color.Red else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom overlays row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Duration badge
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${duration}min",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    // Completion badge
                    if (isCompleted) {
                        Surface(
                            color = MaterialTheme.oraColors.successGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complété",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Lire le contenu",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Content information
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Subtitle
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Difficulty + Likes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Difficulty stars
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = if (index < difficulty.getStars()) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (index < difficulty.getStars()) typeColor else MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = difficulty.getDisplayText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Likes count
                    if (likes != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = likes.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentCardPreview() {
    OraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContentCard(
                title = "Yoga Réveil Énergisant",
                subtitle = "Parfait pour bien commencer la journée",
                duration = 12,
                contentType = ContentType.Yoga,
                difficulty = DifficultyLevel.Beginner,
                thumbnailUrl = null,
                likes = 24,
                isFavorite = false,
                isCompleted = false,
                onClick = { },
                onFavoriteClick = { }
            )

            ContentCard(
                title = "Méditation Pleine Conscience",
                subtitle = "Retour au calme intérieur",
                duration = 15,
                contentType = ContentType.Meditation,
                difficulty = DifficultyLevel.Intermediate,
                thumbnailUrl = null,
                likes = 67,
                isFavorite = true,
                isCompleted = true,
                onClick = { },
                onFavoriteClick = { }
            )
        }
    }
}