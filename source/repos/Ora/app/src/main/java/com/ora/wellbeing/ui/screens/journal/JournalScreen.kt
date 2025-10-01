package com.ora.wellbeing.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.ui.components.GratitudeCard
import com.ora.wellbeing.ui.components.OraLogo
import com.ora.wellbeing.ui.theme.OraColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JournalScreen(
    onNavigateToDetail: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OraColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header avec logo ORA et profil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OraLogo(size = 40)

            IconButton(
                onClick = { /* Navigation vers profil */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = OraColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section "3 gratitudes du jour"
        Text(
            text = "3 gratitudes du jour",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OraColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cards de gratitudes
        uiState.todayGratitudes.forEachIndexed { index, gratitude ->
            val cardColor = when (index) {
                0 -> OraColors.Gratitude1
                1 -> OraColors.Gratitude2
                else -> OraColors.Gratitude3
            }

            GratitudeCard(
                text = gratitude.text,
                color = cardColor,
                modifier = Modifier.fillMaxWidth()
            )

            if (index < uiState.todayGratitudes.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Zone de texte "Comment tu te sens"
        OutlinedTextField(
            value = uiState.todayFeeling,
            onValueChange = {
                viewModel.onEvent(JournalUiEvent.UpdateFeeling(it))
            },
            placeholder = {
                Text(
                    text = "Écris comment tu te sens aujourd'hui...",
                    color = OraColors.TextLight
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onNavigateToDetail() },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OraColors.Primary,
                unfocusedBorderColor = OraColors.TextLight.copy(alpha = 0.3f)
            ),
            enabled = false // On clique pour aller au détail
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Section "Mon habit tracker"
        Text(
            text = "Mon habit tracker",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OraColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calendrier de points colorés pour les habits
        HabitTrackerCalendar(
            habitTrackers = uiState.habitTrackers,
            currentMonth = uiState.currentMonth,
            onDateClick = { habitId, date ->
                viewModel.onEvent(JournalUiEvent.ToggleHabit(habitId, date))
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Gestion du loading
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OraColors.Primary)
        }
    }
}

@Composable
fun HabitTrackerCalendar(
    habitTrackers: List<com.ora.wellbeing.domain.model.HabitTracker>,
    currentMonth: LocalDate,
    onDateClick: (String, LocalDate) -> Unit
) {
    Column {
        // Légende des habits
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(habitTrackers) { habit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                Color(android.graphics.Color.parseColor(habit.color)),
                                CircleShape
                            )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = habit.name,
                        fontSize = 12.sp,
                        color = OraColors.TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grille du calendrier simplifié (derniers 21 jours)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            val startDate = currentMonth.minusDays(20)

            items(21) { dayIndex ->
                val date = startDate.plusDays(dayIndex.toLong())

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Numéro du jour
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 10.sp,
                        color = OraColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Points colorés pour chaque habit
                    habitTrackers.forEach { habit ->
                        val isCompleted = habit.completedDates.contains(date)

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isCompleted) {
                                        Color(android.graphics.Color.parseColor(habit.color))
                                    } else {
                                        Color.Gray.copy(alpha = 0.3f)
                                    },
                                    CircleShape
                                )
                                .clickable {
                                    onDateClick(habit.id, date)
                                }
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}