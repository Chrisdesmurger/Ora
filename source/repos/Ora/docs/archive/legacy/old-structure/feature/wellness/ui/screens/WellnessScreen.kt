package feature.wellness.ui.screens

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
import kotlin.random.Random

@Composable
fun WellnessScreen(
    modifier: Modifier = Modifier
) {
    // Données d'exemple pour les habitudes
    var habitData by remember {
        mutableStateOf(
            listOf(
                "Méditation quotidienne" to generateSampleHabitDays(28),
                "Session de yoga" to generateSampleHabitDays(28),
                "Exercices de respiration" to generateSampleHabitDays(21),
                "Temps de gratitude" to generateSampleHabitDays(14)
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
                    text = "Mon Bien-être",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Suis tes progrès et tes habitudes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OraColors.OnSurfaceVariant
                )
            }
        }

        item {
            // Résumé de la semaine
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = OraColors.OraOrange.copy(alpha = 0.1f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Cette semaine",
                        style = MaterialTheme.typography.titleLarge,
                        color = OraColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WellnessStatCard(
                            title = "Sessions",
                            value = "12",
                            subtitle = "cette semaine",
                            color = OraColors.YogaGreen
                        )

                        WellnessStatCard(
                            title = "Minutes",
                            value = "240",
                            subtitle = "de pratique",
                            color = OraColors.MeditationPurple
                        )

                        WellnessStatCard(
                            title = "Série",
                            value = "7",
                            subtitle = "jours consécutifs",
                            color = OraColors.PilatesOrange
                        )
                    }
                }
            }
        }

        // Trackers d'habitudes
        items(habitData.size) { index ->
            val (habitName, habitDays) = habitData[index]

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OraHabitTracker(
                    habitName = habitName,
                    habitDays = habitDays,
                    onDayClick = { dayIndex ->
                        // Toggle completion status
                        val updatedDays = habitDays.toMutableList().apply {
                            this[dayIndex] = this[dayIndex].copy(
                                isCompleted = !this[dayIndex].isCompleted
                            )
                        }
                        habitData = habitData.toMutableList().apply {
                            this[index] = habitName to updatedDays
                        }
                    }
                )
            }
        }

        item {
            // Conseils bien-être
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Conseil du jour",
                        style = MaterialTheme.typography.titleLarge,
                        color = OraColors.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Prends 5 minutes ce matin pour respirer profondément. Cela peut transformer ta journée entière.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OraColors.OnSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Navigate to breathing exercise */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OraColors.BreathingBlue
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Essayer maintenant")
                    }
                }
            }
        }
    }
}

@Composable
private fun WellnessStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = OraColors.OnSurface,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = OraColors.OnSurfaceVariant
        )
    }
}

private fun generateSampleHabitDays(count: Int): List<HabitDay> {
    val colors = listOf(
        OraColors.YogaGreen,
        OraColors.PilatesOrange,
        OraColors.MeditationPurple,
        OraColors.BreathingBlue
    )

    return (1..count).map { dayNumber ->
        HabitDay(
            dayOfWeek = when (dayNumber % 7) {
                1 -> "L"
                2 -> "M"
                3 -> "M"
                4 -> "J"
                5 -> "V"
                6 -> "S"
                0 -> "D"
                else -> "L"
            },
            dayNumber = dayNumber,
            isCompleted = Random.nextFloat() > 0.3f, // 70% de chance d'être complété
            completionColor = colors.random()
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun WellnessScreenPreview() {
    OraTheme {
        WellnessScreen()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun WellnessScreenMobilePreview() {
    OraTheme {
        WellnessScreen()
    }
}