package com.ora.wellbeing.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ora.wellbeing.R
import com.ora.wellbeing.domain.model.VideoContent

// FIX(build-debug-android): Création de l'objet OraColors pour remplacer ui.theme.OraColors manquant
object OraColors {
    val Primary = Color(0xFF6B73FF)          // Bleu violet zen
    val Secondary = Color(0xFF03DAC6)        // Teal apaisante
    val Tertiary = Color(0xFFFF6B9D)         // Rose doux
    val Error = Color(0xFFB00020)            // Rouge erreur
    val TextPrimary = Color(0xFF1C1B1F)      // Texte principal
    val TextSecondary = Color(0xFF6B6B6B)    // Texte secondaire
    val VideoOverlay = Color.Black.copy(alpha = 0.3f)  // Overlay sombre pour vidéos
}

@Composable
fun OraLogo(
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône soleil
        Box(
            modifier = Modifier
                .size(size.dp)
                .background(OraColors.Primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☀",
                fontSize = (size * 0.6).sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "ORA",
            fontSize = (size * 0.8).sp,
            fontWeight = FontWeight.Bold,
            color = OraColors.Primary
        )
    }
}

@Composable
fun VideoCard(
    video: VideoContent,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLarge) 200.dp else 120.dp),
                contentScale = ContentScale.Crop
            )

            // FIX(build-debug-android): Utilisation directe de OraColors.VideoOverlay au lieu d'une référence inexistante
            // Overlay sombre
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OraColors.VideoOverlay)
            )

            // Badge "NOUVEAU"
            if (video.isNew) {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(containerColor = OraColors.Error),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "NOUVEAU",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Bouton play au centre
            IconButton(
                onClick = onClick,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = OraColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Durée en bas à droite
            Text(
                text = video.duration,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Titre en dessous
        Text(
            text = video.title,
            fontSize = if (isLarge) 16.sp else 14.sp,
            fontWeight = FontWeight.Medium,
            color = OraColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun CategoryButton(
    title: String,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.2f),
            contentColor = if (isSelected) Color.White else color
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GratitudeCard(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = OraColors.TextPrimary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun MoodSelector(
    selectedMood: com.ora.wellbeing.domain.model.Mood?,
    onMoodSelected: (com.ora.wellbeing.domain.model.Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        com.ora.wellbeing.domain.model.Mood.values().forEach { mood ->
            val isSelected = selectedMood == mood

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onMoodSelected(mood) }
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (isSelected) OraColors.Primary.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mood.emoji,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mood.label,
                    fontSize = 12.sp,
                    color = if (isSelected) OraColors.Primary else OraColors.TextSecondary
                )
            }
        }
    }
}