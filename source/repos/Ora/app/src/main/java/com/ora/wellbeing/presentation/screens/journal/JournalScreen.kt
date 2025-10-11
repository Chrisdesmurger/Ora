package com.ora.wellbeing.presentation.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateToEntry: (String?) -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(JournalUiEvent.LoadJournalData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header avec titre et actions
        JournalHeader(
            onNewEntryClick = { onNavigateToEntry(null) },
            onHistoryClick = onNavigateToHistory,
            streak = uiState.gratitudeStreak
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Entrée du jour
                item {
                    TodayEntrySection(
                        todayEntry = uiState.todayEntry,
                        onAddTodayEntry = { onNavigateToEntry(null) },
                        onEditTodayEntry = { onNavigateToEntry(uiState.todayEntry?.date) }
                    )
                }

                // Rappel de gratitude
                item {
                    GratitudeReminderCard(
                        hasCompletedToday = uiState.todayEntry != null,
                        onReminderClick = { onNavigateToEntry(null) }
                    )
                }

                // Entrées récentes
                if (uiState.recentEntries.isNotEmpty()) {
                    item {
                        Text(
                            text = "Entrées récentes",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(uiState.recentEntries) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            onClick = { onNavigateToEntry(entry.date) }
                        )
                    }
                }

                // Stats de gratitude
                item {
                    GratitudeStatsCard(
                        totalEntries = uiState.totalEntries,
                        streak = uiState.gratitudeStreak,
                        thisMonthCount = uiState.thisMonthEntries
                    )
                }
            }
        }
    }
}

@Composable
private fun JournalHeader(
    onNewEntryClick: () -> Unit,
    onHistoryClick: () -> Unit,
    streak: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = "Journal de gratitudes",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (streak > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔥 Série de $streak jour${if (streak > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Historique",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // FIX: Use icon-only button for narrow screens to prevent text wrapping
            FilledTonalIconButton(
                onClick = onNewEntryClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nouvelle entrée",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayEntrySection(
    todayEntry: JournalUiState.JournalEntry?,
    onAddTodayEntry: () -> Unit,
    onEditTodayEntry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aujourd'hui",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (todayEntry != null) {
                    TextButton(onClick = onEditTodayEntry) {
                        Text("Modifier", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (todayEntry == null) {
                // Pas d'entrée aujourd'hui
                Column {
                    Text(
                        text = "Prenez un moment pour noter vos gratitudes du jour ✨",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = onAddTodayEntry,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Commencer")
                    }
                }
            } else {
                // Entrée d'aujourd'hui existe - 3 gratitudes avec couleurs différentes
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gratitude 1 - Rose
                    if (todayEntry.gratitudes.isNotEmpty()) {
                        GratitudeItem(
                            text = todayEntry.gratitudes[0],
                            backgroundColor = GratitudePink,
                            index = 1
                        )
                    }

                    // Gratitude 2 - Orange
                    if (todayEntry.gratitudes.size > 1) {
                        GratitudeItem(
                            text = todayEntry.gratitudes[1],
                            backgroundColor = GratitudePeach,
                            index = 2
                        )
                    }

                    // Gratitude 3 - Vert
                    if (todayEntry.gratitudes.size > 2) {
                        GratitudeItem(
                            text = todayEntry.gratitudes[2],
                            backgroundColor = GratitudeMint,
                            index = 3
                        )
                    }

                    if (todayEntry.mood.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Humeur: ${todayEntry.mood}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GratitudeItem(
    text: String,
    backgroundColor: Color,
    index: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icône check dans cercle
        Surface(
            modifier = Modifier.size(28.dp),
            shape = RoundedCornerShape(14.dp),
            color = when (index) {
                1 -> Color(0xFFF4A69E) // Rose plus foncé
                2 -> Color(0xFFF4B183) // Orange plus foncé
                else -> Color(0xFF9BC4A8) // Vert plus foncé
            }
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GratitudeReminderCard(
    hasCompletedToday: Boolean,
    onReminderClick: () -> Unit
) {
    if (hasCompletedToday) return

    Card(
        onClick = onReminderClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Moment de gratitude",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Notez 3 choses pour lesquelles vous êtes reconnaissant(e)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Aller",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalEntryCard(
    entry: JournalUiState.JournalEntry,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (entry.mood.isNotEmpty()) {
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            entry.gratitudes.take(2).forEachIndexed { index, gratitude ->
                Text(
                    text = "• $gratitude",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entry.gratitudes.size > 2) {
                Text(
                    text = "et ${entry.gratitudes.size - 2} autre${if (entry.gratitudes.size > 3) "s" else ""}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GratitudeStatsCard(
    totalEntries: Int,
    streak: Int,
    thisMonthCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Vos statistiques",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalEntries.toString(),
                    label = "Total d'entrées",
                    icon = Icons.Default.BookmarkBorder
                )

                StatItem(
                    value = streak.toString(),
                    label = "Série actuelle",
                    icon = Icons.Default.Whatshot
                )

                StatItem(
                    value = thisMonthCount.toString(),
                    label = "Ce mois-ci",
                    icon = Icons.Default.CalendarMonth
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun JournalScreenPreview() {
    OraTheme {
        JournalScreen(
            onNavigateToEntry = {},
            onNavigateToHistory = {}
        )
    }
}