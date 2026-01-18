package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.design.OraColors
import core.design.OraTheme
import kotlin.random.Random

data class HabitDay(
    val dayOfWeek: String,
    val dayNumber: Int,
    val isCompleted: Boolean,
    val completionColor: Color = OraColors.YogaGreen
)

@Composable
fun OraHabitTracker(
    habitName: String,
    habitDays: List<HabitDay>,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
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
                .padding(20.dp)
        ) {
            // Titre de l'habitude
            Text(
                text = habitName,
                style = MaterialTheme.typography.headlineSmall,
                color = OraColors.OnSurface,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // En-têtes des jours de la semaine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach { dayLetter ->
                    Text(
                        text = dayLetter,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = OraColors.OnSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grille des jours (4 semaines)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habitDays.size) { index ->
                    val day = habitDays[index]
                    OraHabitDayButton(
                        day = day,
                        onClick = { onDayClick(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistiques
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val completedDays = habitDays.count { it.isCompleted }
                val totalDays = habitDays.size
                val percentage = if (totalDays > 0) (completedDays * 100) / totalDays else 0

                Text(
                    text = "$completedDays/$totalDays jours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OraColors.OnSurfaceVariant
                )

                Text(
                    text = "$percentage% complété",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OraColors.YogaGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun OraHabitDayButton(
    day: HabitDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(
                if (day.isCompleted) {
                    day.completionColor
                } else {
                    Color.Gray.copy(alpha = 0.2f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (day.isCompleted) {
            // Point coloré pour les jours complétés
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        } else {
            // Numéro du jour pour les jours non complétés
            Text(
                text = day.dayNumber.toString(),
                fontSize = 12.sp,
                color = OraColors.OnSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun OraMultipleHabitsTracker(
    habits: List<Pair<String, List<HabitDay>>>,
    onDayClick: (Int, Int) -> Unit, // habitIndex, dayIndex
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(habits.size) { habitIndex ->
            val (habitName, habitDays) = habits[habitIndex]
            OraHabitTracker(
                habitName = habitName,
                habitDays = habitDays,
                onDayClick = { dayIndex -> onDayClick(habitIndex, dayIndex) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraHabitTrackerPreview() {
    OraTheme {
        val sampleHabitDays = remember {
            (1..28).map { dayNumber ->
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
                    isCompleted = Random.nextBoolean(),
                    completionColor = listOf(
                        OraColors.YogaGreen,
                        OraColors.PilatesOrange,
                        OraColors.MeditationPurple,
                        OraColors.BreathingBlue
                    ).random()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OraHabitTracker(
                habitName = "Méditation quotidienne",
                habitDays = sampleHabitDays,
                onDayClick = { }
            )

            OraHabitTracker(
                habitName = "Session de yoga",
                habitDays = sampleHabitDays.take(21),
                onDayClick = { }
            )
        }
    }
}