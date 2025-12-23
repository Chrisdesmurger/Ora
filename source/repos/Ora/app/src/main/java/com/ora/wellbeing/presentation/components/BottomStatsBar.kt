package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.R
import com.ora.wellbeing.presentation.theme.OraTheme

/**
 * Barre de statistiques en bas du profil.
 * Affiche 3 colonnes: jours d'affilée, temps total, dernière activité.
 *
 * @param streakDays Nombre de jours consécutifs
 * @param totalTime Temps total formaté (ex: "24h10")
 * @param lastActivity Description de la dernière activité
 * @param backgroundColor Couleur de fond (beige par défaut)
 * @param modifier Modifier optionnel
 */
@Composable
fun BottomStatsBar(
    streakDays: Int,
    totalTime: String,
    lastActivity: String,
    backgroundColor: Color = Color(0xFFF5F0E8),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$streakDays jours d'affilée, $totalTime en tout, Dernière activité: $lastActivity"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = Color(0xFF1C1B1F)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            // Colonne 1: Jours d'affilée
            StatColumn(
                value = streakDays.toString(),
                label = stringResource(R.string.profile_streak_label),
                modifier = Modifier.weight(1f)
            )

            // Séparateur vertical (optionnel)
            Spacer(modifier = Modifier.width(8.dp))

            // Colonne 2: Temps total
            StatColumn(
                value = totalTime,
                label = stringResource(R.string.profile_total_time_label),
                modifier = Modifier.weight(1f)
            )

            // Séparateur vertical (optionnel)
            Spacer(modifier = Modifier.width(8.dp))

            // Colonne 3: Dernière activité
            LastActivityColumn(
                activity = lastActivity,
                modifier = Modifier.weight(1.5f)
            )
        }
    }
}

/**
 * Colonne de statistique avec valeur et label
 */
@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

/**
 * Colonne pour la dernière activité
 */
@Composable
private fun LastActivityColumn(
    activity: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_last_activity),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center
        )
        Text(
            text = activity,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

/**
 * Version alternative avec icônes (optionnel)
 */
@Composable
fun BottomStatsBarWithIcons(
    streakDays: Int,
    totalTime: String,
    lastActivity: String,
    backgroundColor: Color = Color(0xFFF5F0E8),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$streakDays jours d'affilée, $totalTime en tout, Dernière activité: $lastActivity"
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = Color(0xFF1C1B1F)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak avec icône flamme
            StatWithIcon(
                icon = OraIcons.Streak,
                value = streakDays.toString(),
                label = stringResource(R.string.profile_streak_label),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Temps total avec icône horloge
            StatWithIcon(
                icon = OraIcons.TotalTime,
                value = totalTime,
                label = stringResource(R.string.profile_total_time_label),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Dernière activité
            Column(
                modifier = Modifier.weight(1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = OraIcons.LastActivity,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF1C1B1F)
                    )
                    Text(
                        text = stringResource(R.string.profile_last_activity),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1C1B1F)
                    )
                }
                Text(
                    text = lastActivity,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF1C1B1F)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F),
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomStatsBarPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Version sans icônes
            BottomStatsBar(
                streakDays = 5,
                totalTime = "24h10",
                lastActivity = "Yoga doux - 25 min"
            )

            // Version avec icônes
            BottomStatsBarWithIcons(
                streakDays = 5,
                totalTime = "24h10",
                lastActivity = "Yoga doux - 25 min"
            )
        }
    }
}
