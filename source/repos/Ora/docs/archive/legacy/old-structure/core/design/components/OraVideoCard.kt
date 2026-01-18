package core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

@Composable
fun OraVideoCard(
    title: String,
    duration: String,
    imageUrl: String? = null,
    isNew: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image de fond (placeholder pour l'exemple)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                OraColors.YogaGreen.copy(alpha = 0.7f),
                                OraColors.YogaGreen.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Badge "NOUVEAU" en haut à gauche
            if (isNew) {
                Card(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    colors = CardDefaults.cardColors(
                        containerColor = OraColors.OraOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "NOUVEAU",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Durée en bas à droite
            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.BottomEnd),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = duration,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }

            // Bouton play central
            Card(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(32.dp),
                        tint = OraColors.OnSurface
                    )
                }
            }

            // Titre en overlay en bas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun OraSuggestionCard(
    title: String = "Your suggestion of the day",
    subtitle: String,
    duration: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .height(250.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fond dégradé
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                OraColors.MeditationPurple.copy(alpha = 0.8f),
                                OraColors.MeditationPurple
                            )
                        )
                    )
            )

            // Contenu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Titre en haut
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Bouton play central
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            modifier = Modifier.size(40.dp),
                            tint = OraColors.MeditationPurple
                        )
                    }
                }

                // Informations en bas
                Column {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraVideoCardPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OraVideoCard(
                title = "Morning Yoga Flow",
                duration = "12:22",
                isNew = true,
                onClick = {}
            )

            OraVideoCard(
                title = "Relaxing Meditation",
                duration = "08:45",
                isNew = false,
                onClick = {}
            )

            OraSuggestionCard(
                subtitle = "Peaceful Morning Meditation",
                duration = "15 minutes",
                onClick = {}
            )
        }
    }
}