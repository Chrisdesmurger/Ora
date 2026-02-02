package feature.journal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme
import core.design.components.*

@Composable
fun JournalScreen(
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }
    var journalText by remember { mutableStateOf("") }
    var gratitudeItems by remember {
        mutableStateOf(
            listOf(
                GratitudeItem("", false, GratitudeCardColor.PINK),
                GratitudeItem("", false, GratitudeCardColor.ORANGE),
                GratitudeItem("", false, GratitudeCardColor.GREEN)
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OraColors.Background),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Mon Journal",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Prends un moment pour toi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OraColors.OnSurfaceVariant
                )
            }
        }

        item {
            // Sélecteur d'humeur
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OraMoodSelector(
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it },
                    title = "Comment te sens-tu aujourd'hui ?"
                )
            }
        }

        item {
            // Zone de journal
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OraJournalTextField(
                    value = journalText,
                    onValueChange = { journalText = it },
                    title = "Mes pensées du jour",
                    placeholder = "Écris comment tu te sens aujourd'hui, ce qui t'a marqué, tes réflexions..."
                )
            }
        }

        item {
            // Section gratitude
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Mes gratitudes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Note 3 choses pour lesquelles tu es reconnaissant aujourd'hui",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OraColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Cards de gratitude
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gratitudeItems.forEachIndexed { index, item ->
                        OraGratitudeCard(
                            text = item.text,
                            onTextChange = { newText ->
                                gratitudeItems = gratitudeItems.toMutableList().apply {
                                    this[index] = item.copy(text = newText)
                                }
                            },
                            isChecked = item.isChecked,
                            onCheckedChange = { checked ->
                                gratitudeItems = gratitudeItems.toMutableList().apply {
                                    this[index] = item.copy(isChecked = checked)
                                }
                            },
                            cardColor = item.color,
                            placeholder = when (index) {
                                0 -> "Une personne qui compte pour toi..."
                                1 -> "Un moment spécial aujourd'hui..."
                                2 -> "Quelque chose de beau que tu as vu..."
                                else -> "Écris ce pour quoi tu es reconnaissant..."
                            }
                        )
                    }
                }
            }
        }

        item {
            // Historique des humeurs (si l'utilisateur a déjà saisi des données)
            if (selectedMood != null) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OraMoodHistory(
                        moodHistory = listOf(
                            "Aujourd'hui" to selectedMood!!,
                            "Hier" to MoodType.HAPPY,
                            "Avant-hier" to MoodType.NEUTRAL,
                            "Il y a 3 jours" to MoodType.HAPPY
                        )
                    )
                }
            }
        }

        item {
            // Bouton de sauvegarde
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = {
                        // Logique de sauvegarde
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OraColors.OraOrange
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Sauvegarder ma journée",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun JournalScreenPreview() {
    OraTheme {
        JournalScreen()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun JournalScreenMobilePreview() {
    OraTheme {
        JournalScreen()
    }
}