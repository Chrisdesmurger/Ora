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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.R
import com.ora.wellbeing.presentation.theme.OraTheme

/**
 * Carte de statistiques pour le profil.
 * Peut afficher soit les gratitudes, soit les objectifs avec checkboxes.
 *
 * @param title Titre de la carte (ex: "GRATITUDES", "OBJECTIFS")
 * @param backgroundColor Couleur de fond (beige/crème par défaut)
 * @param modifier Modifier optionnel
 * @param content Contenu de la carte
 */
@Composable
fun ProfileStatCard(
    title: String,
    backgroundColor: Color = Color(0xFFF5F0E8),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .semantics { contentDescription = title },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = Color(0xFF1C1B1F)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color(0xFF1C1B1F)
            )

            // Contenu personnalisé
            content()
        }
    }
}

/**
 * Variante pour les gratitudes avec l'indicateur "Aujourd'hui"
 */
@Composable
fun GratitudeStatCard(
    todayText: String = stringResource(R.string.profile_gratitude_today),
    backgroundColor: Color = Color(0xFFF5F0E8),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Text(
            text = todayText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F),
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.semantics {
                contentDescription = "GRATITUDES, $todayText"
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_gratitudes_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF1C1B1F)
                )
                cardContent()
            }
        }
    } else {
        ProfileStatCard(
            title = stringResource(R.string.profile_gratitudes_title),
            backgroundColor = backgroundColor,
            modifier = modifier,
            content = cardContent
        )
    }
}

/**
 * Item d'objectif avec checkbox
 */
@Composable
fun GoalItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = if (isChecked) "$text, coché" else "$text, non coché"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF1C1B1F),
                uncheckedColor = Color(0xFF1C1B1F),
                checkmarkColor = Color.White
            )
        )
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1C1B1F),
            lineHeight = 22.sp
        )
    }
}

/**
 * Variante pour les objectifs avec checkboxes
 */
@Composable
fun GoalsStatCard(
    goals: List<Pair<String, Boolean>>,
    onGoalCheckedChange: (Int, Boolean) -> Unit,
    backgroundColor: Color = Color(0xFFF5F0E8),
    modifier: Modifier = Modifier
) {
    ProfileStatCard(
        title = stringResource(R.string.profile_goals_title),
        backgroundColor = backgroundColor,
        modifier = modifier
    ) {
        goals.forEachIndexed { index, (goalText, isChecked) ->
            GoalItem(
                text = goalText,
                isChecked = isChecked,
                onCheckedChange = { checked -> onGoalCheckedChange(index, checked) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStatCardPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Carte Gratitudes
            GratitudeStatCard(
                modifier = Modifier.fillMaxWidth()
            )

            // Carte Objectifs
            GoalsStatCard(
                goals = listOf(
                    "Lire plus" to true,
                    "Arrêter l'alcool" to true,
                    "10 min de réseaux sociaux max" to false
                ),
                onGoalCheckedChange = { _, _ -> },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
