package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.presentation.theme.OraTheme

/**
 * Démonstration complète de l'écran Profil utilisant tous les composants UI créés.
 * Cette page reproduit le mockup image9.png
 */
@Composable
fun ProfileScreenDemo(
    userName: String = "Clara",
    userMotto: String = "Je prends soin de moi chaque jour",
    modifier: Modifier = Modifier
) {
    var goals by remember {
        mutableStateOf(
            listOf(
                "Lire plus" to true,
                "Arrêter l'alcool" to true,
                "10 min de réseaux sociaux max" to false
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 80.dp), // Bottom padding pour la bottom bar
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header avec logo Ora et icône edit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ORA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PracticeColors.YogaPrimary
            )
            IconButton(onClick = { /* Editer profil */ }) {
                Icon(
                    imageVector = OraIcons.Edit,
                    contentDescription = "Modifier le profil",
                    tint = PracticeColors.YogaPrimary
                )
            }
        }

        // Photo de profil
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder pour la photo de profil
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF799C8E),
                shape = CircleShape
            ) {
                // Image utilisateur ici
            }
        }

        // Nom de l'utilisateur
        Text(
            text = userName,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
        )

        // Motto/Citation
        Text(
            text = userMotto,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F)
        )

        // Section: Mon temps par pratique
        Text(
            text = "MON TEMPS PAR PRATIQUE",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.padding(top = 16.dp)
        )

        // Cartes de pratiques
        PracticeCard(
            practiceName = "Yoga",
            icon = OraIcons.Yoga,
            timeString = "3h45 ce mois-ci",
            backgroundColor = PracticeColors.YogaPrimary,
            onClick = { /* Voir détails Yoga */ }
        )

        PracticeCard(
            practiceName = "Pilates",
            icon = OraIcons.Pilates,
            timeString = "2h15 ce mois-ci",
            backgroundColor = PracticeColors.PilatesLight,
            onClick = { /* Voir détails Pilates */ }
        )

        PracticeCard(
            practiceName = "Méditation",
            icon = OraIcons.Meditation,
            timeString = "4h30 ce mois-ci",
            backgroundColor = PracticeColors.MeditationDark,
            onClick = { /* Voir détails Méditation */ }
        )

        PracticeCard(
            practiceName = "Respiration",
            icon = OraIcons.Respiration,
            timeString = "1h20 ce mois-ci",
            backgroundColor = PracticeColors.RespirationLight,
            contentColor = Color(0xFF2D4A3E),
            onClick = { /* Voir détails Respiration */ }
        )

        // Row pour Gratitudes et Objectifs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Carte Gratitudes
            GratitudeStatCard(
                todayText = "Aujourd'hui",
                modifier = Modifier.weight(1f),
                onClick = { /* Ouvrir journal */ }
            )

            // Carte Objectifs
            GoalsStatCard(
                goals = goals,
                onGoalCheckedChange = { index, checked ->
                    goals = goals.toMutableList().apply {
                        this[index] = this[index].first to checked
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Barre de stats en bas
        BottomStatsBar(
            streakDays = 5,
            totalTime = "24h10",
            lastActivity = "Yoga doux - 25 min",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Preview complet de l'écran profil
 */
@Preview(
    showBackground = true,
    showSystemUi = true,
    backgroundColor = 0xFFFAF7F2
)
@Composable
private fun ProfileScreenDemoPreview() {
    OraTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFFAF7F2) // Couleur beige du mockup
        ) {
            ProfileScreenDemo()
        }
    }
}

/**
 * Preview avec variante d'icônes
 */
@Preview(
    name = "Avec icônes",
    showBackground = true,
    backgroundColor = 0xFFFAF7F2
)
@Composable
private fun ProfileScreenWithIconsPreview() {
    OraTheme(dynamicColor = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Variante avec icônes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            BottomStatsBarWithIcons(
                streakDays = 5,
                totalTime = "24h10",
                lastActivity = "Yoga doux - 25 min"
            )
        }
    }
}