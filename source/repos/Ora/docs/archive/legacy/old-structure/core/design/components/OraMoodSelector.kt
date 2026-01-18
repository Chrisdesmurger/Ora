package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.design.OraColors
import core.design.OraTheme

enum class MoodType(
    val emoji: String,
    val label: String,
    val color: Color
) {
    HAPPY("😊", "Heureux", OraColors.MoodHappy),
    NEUTRAL("😐", "Neutre", OraColors.MoodNeutral),
    SAD("😔", "Triste", OraColors.MoodSad),
    ANGRY("😠", "En colère", OraColors.MoodAngry)
}

@Composable
fun OraMoodSelector(
    selectedMood: MoodType?,
    onMoodSelected: (MoodType) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Comment te sens-tu aujourd'hui ?"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = OraColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sélecteur d'humeurs
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoodType.values().forEach { mood ->
                    OraMoodButton(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = { onMoodSelected(mood) }
                    )
                }
            }

            // Affichage de l'humeur sélectionnée
            if (selectedMood != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = selectedMood.color.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tu te sens ${selectedMood.label.lowercase()}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = selectedMood.color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OraMoodButton(
    mood: MoodType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emoji avec fond coloré si sélectionné
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) mood.color.copy(alpha = 0.2f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = mood.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) mood.color else OraColors.OnSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun OraMoodHistory(
    moodHistory: List<Pair<String, MoodType>>, // Date et humeur
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Historique de tes humeurs",
                style = MaterialTheme.typography.titleMedium,
                color = OraColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moodHistory.size) { index ->
                    val (date, mood) = moodHistory[index]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OraColors.OnSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = mood.emoji,
                                fontSize = 20.sp
                            )
                            Text(
                                text = mood.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = mood.color,
                                fontWeight = FontWeight.Medium
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
fun OraMoodSelectorPreview() {
    OraTheme {
        var selectedMood by remember { mutableStateOf<MoodType?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OraMoodSelector(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it }
            )

            OraMoodHistory(
                moodHistory = listOf(
                    "Aujourd'hui" to MoodType.HAPPY,
                    "Hier" to MoodType.NEUTRAL,
                    "Avant-hier" to MoodType.SAD
                )
            )
        }
    }
}