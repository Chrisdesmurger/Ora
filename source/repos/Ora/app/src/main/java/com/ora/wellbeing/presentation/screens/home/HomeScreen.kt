package com.ora.wellbeing.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.navigation.QuickSessionType
import com.ora.wellbeing.presentation.theme.*

@Composable
fun HomeScreen(
    onNavigateToContent: (String) -> Unit,
    onNavigateToProgram: (String) -> Unit,
    onNavigateToQuickSession: (QuickSessionType) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(HomeUiEvent.LoadHomeData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // FIX(build-debug-android): Message de bienvenue personnalisé restauré
        WelcomeSection(
            userName = uiState.userName,
            streakDays = uiState.streakDays
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // FIX(build-debug-android): Sessions rapides restaurées avec textes
            item {
                QuickSessionsSection(
                    onStartQuickSession = onNavigateToQuickSession
                )
            }

            // FIX(build-debug-android): Recommandations du jour restaurées avec textes
            item {
                RecommendationsSection(
                    recommendations = uiState.dailyRecommendations,
                    onContentClick = onNavigateToContent
                )
            }

            // FIX(build-debug-android): Progression des programmes actifs restaurée avec textes
            item {
                ActiveProgramsSection(
                    activePrograms = uiState.activePrograms,
                    onProgramClick = onNavigateToProgram
                )
            }

            // FIX(build-debug-android): Statistiques rapides restaurées avec textes
            item {
                QuickStatsSection(
                    totalMinutes = uiState.totalMinutesThisWeek,
                    sessionsCompleted = uiState.sessionsCompletedThisWeek,
                    favoriteCategory = uiState.favoriteCategory
                )
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    userName: String,
    streakDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FIX(build-debug-android): Texte de bienvenue restauré
            Text(
                text = if (userName.isNotBlank()) "Bonjour $userName" else "Bonjour",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (streakDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔥 $streakDays jours de suite!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun QuickSessionsSection(
    onStartQuickSession: (QuickSessionType) -> Unit
) {
    Column {
        // FIX(build-debug-android): Titre de section restauré
        Text(
            text = "Sessions rapides",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(QuickSessionType.values()) { sessionType ->
                QuickSessionCard(
                    sessionType = sessionType,
                    onClick = { onStartQuickSession(sessionType) }
                )
            }
        }
    }
}

@Composable
private fun QuickSessionCard(
    sessionType: QuickSessionType,
    onClick: () -> Unit
) {
    val (backgroundColor, iconColor, sessionName) = when (sessionType) {
        QuickSessionType.BREATHING -> Triple(CategoryBreathingBlue, CategoryBreathingBlue, "Respiration")
        QuickSessionType.YOGA_FLASH -> Triple(CategoryYogaGreen, MaterialTheme.colorScheme.tertiary, "Flash Yoga")
        QuickSessionType.MINI_MEDITATION -> Triple(CategoryMeditationLavender, CategoryMeditationLavender, "Méditation")
    }

    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FIX(build-debug-android): Nom de session restauré
            Text(
                text = sessionName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecommendationsSection(
    recommendations: List<HomeUiState.ContentRecommendation>,
    onContentClick: (String) -> Unit
) {
    if (recommendations.isEmpty()) return

    Column {
        // FIX(build-debug-android): Titre de section restauré
        Text(
            text = "Recommandations du jour",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendations) { content ->
                RecommendationCard(
                    content = content,
                    onClick = { onContentClick(content.id) }
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    content: HomeUiState.ContentRecommendation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                // FIX(build-debug-android): Durée restaurée
                Text(
                    text = content.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FIX(build-debug-android): Titre et catégorie restaurés
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = content.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActiveProgramsSection(
    activePrograms: List<HomeUiState.ActiveProgram>,
    onProgramClick: (String) -> Unit
) {
    if (activePrograms.isEmpty()) return

    Column {
        // FIX(build-debug-android): Titre de section restauré
        Text(
            text = "Programmes en cours",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        activePrograms.forEach { program ->
            ActiveProgramCard(
                program = program,
                onClick = { onProgramClick(program.id) }
            )

            if (program != activePrograms.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActiveProgramCard(
    program: HomeUiState.ActiveProgram,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FIX(build-debug-android): Titre du programme restauré
            Text(
                text = program.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FIX(build-debug-android): Progression textuelle restaurée
            Text(
                text = "Jour ${program.currentDay} sur ${program.totalDays}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Barre de progression avec pourcentage
            LinearProgressIndicator(
                progress = program.progressPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFFFFE4D9)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${program.progressPercentage}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun QuickStatsSection(
    totalMinutes: Int,
    sessionsCompleted: Int,
    favoriteCategory: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FIX(build-debug-android): Titre de section restauré
            Text(
                text = "Cette semaine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${totalMinutes}min",
                    label = "Temps total"
                )

                StatItem(
                    value = sessionsCompleted.toString(),
                    label = "Sessions"
                )

                StatItem(
                    value = favoriteCategory,
                    label = "Favori"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // FIX(build-debug-android): Valeur et label restaurés
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    OraTheme {
        HomeScreen(
            onNavigateToContent = {},
            onNavigateToProgram = {},
            onNavigateToQuickSession = {}
        )
    }
}