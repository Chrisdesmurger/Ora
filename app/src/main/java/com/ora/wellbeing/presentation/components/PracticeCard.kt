package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.presentation.theme.OraTheme

/**
 * Carte de pratique pill-shaped pour afficher le temps passé dans chaque pratique.
 *
 * @param practiceName Nom de la pratique (ex: "Yoga", "Pilates")
 * @param icon Icône de la pratique
 * @param timeString Temps passé formaté (ex: "3h45 ce mois-ci")
 * @param backgroundColor Couleur de fond de la carte
 * @param contentColor Couleur du texte et de l'icône
 * @param onClick Action au clic (optionnel)
 * @param modifier Modifier optionnel
 */
@Composable
fun PracticeCard(
    practiceName: String,
    icon: ImageVector,
    timeString: String,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .semantics {
            contentDescription = "$practiceName, $timeString"
        }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = RoundedCornerShape(50), // Pill-shaped
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            PracticeCardContent(
                practiceName = practiceName,
                icon = icon,
                timeString = timeString,
                contentColor = contentColor
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(50), // Pill-shaped
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            PracticeCardContent(
                practiceName = practiceName,
                icon = icon,
                timeString = timeString,
                contentColor = contentColor
            )
        }
    }
}

@Composable
private fun PracticeCardContent(
    practiceName: String,
    icon: ImageVector,
    timeString: String,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Icône + Nom de la pratique
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = practiceName,
                color = contentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
        }

        // Temps passé
        Text(
            text = timeString,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp
        )
    }
}

// Couleurs des pratiques selon le mockup
object PracticeColors {
    val YogaPrimary = Color(0xFFF4845F)      // Orange vif
    val PilatesLight = Color(0xFFF5B299)     // Orange clair
    val MeditationDark = Color(0xFF799C8E)   // Vert foncé/sage
    val RespirationLight = Color(0xFFA8C4B7) // Vert clair
}

@Preview(showBackground = true)
@Composable
private fun PracticeCardPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PracticeCard(
                practiceName = "Yoga",
                icon = OraIcons.Yoga,
                timeString = "3h45 ce mois-ci",
                backgroundColor = PracticeColors.YogaPrimary
            )

            PracticeCard(
                practiceName = "Pilates",
                icon = OraIcons.Pilates,
                timeString = "2h15 ce mois-ci",
                backgroundColor = PracticeColors.PilatesLight
            )

            PracticeCard(
                practiceName = "Méditation",
                icon = OraIcons.Meditation,
                timeString = "4h30 ce mois-ci",
                backgroundColor = PracticeColors.MeditationDark
            )

            PracticeCard(
                practiceName = "Respiration",
                icon = OraIcons.Respiration,
                timeString = "1h20 ce mois-ci",
                backgroundColor = PracticeColors.RespirationLight,
                contentColor = Color(0xFF2D4A3E) // Texte plus foncé pour le contraste
            )
        }
    }
}