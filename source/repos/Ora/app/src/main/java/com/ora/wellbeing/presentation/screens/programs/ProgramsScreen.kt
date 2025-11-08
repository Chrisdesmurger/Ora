package com.ora.wellbeing.presentation.screens.programs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.ora.wellbeing.presentation.theme.*

@Composable
fun ProgramsScreen(
    onNavigateToProgram: (String) -> Unit,
    onNavigateToActiveProgram: (String) -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProgramsUiEvent.LoadProgramsData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()  // Respecter la zone de la barre de statut
            .padding(horizontal = 12.dp)  // Réduire le padding horizontal pour maximiser l'espace
    ) {
        // Header
        ProgramsHeader()

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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Programmes actifs
                if (uiState.activePrograms.isNotEmpty()) {
                    item {
                        ActiveProgramsSection(
                            activePrograms = uiState.activePrograms,
                            onContinueProgram = onNavigateToActiveProgram
                        )
                    }
                }

                // Programmes recommandés
                item {
                    RecommendedProgramsSection(
                        recommendedPrograms = uiState.recommendedPrograms,
                        onProgramClick = onNavigateToProgram
                    )
                }

                // Défis populaires
                item {
                    PopularChallengesSection(
                        popularChallenges = uiState.popularChallenges,
                        onChallengeClick = onNavigateToProgram
                    )
                }

                // Tous les programmes par catégorie
                item {
                    AllProgramsSection(
                        programsByCategory = uiState.programsByCategory,
                        onProgramClick = onNavigateToProgram
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgramsHeader() {
    Column {
        Text(
            text = "Programmes & Défis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Suivez des programmes structurés pour développer vos habitudes de bien-être",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActiveProgramsSection(
    activePrograms: List<ProgramsUiState.ActiveProgram>,
    onContinueProgram: (String) -> Unit
) {
    Column {
        Text(
            text = "Vos programmes en cours",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        activePrograms.forEach { program ->
            ActiveProgramCard(
                program = program,
                onContinueClick = { onContinueProgram(program.id) }
            )

            if (program != activePrograms.last()) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveProgramCard(
    program: ProgramsUiState.ActiveProgram,
    onContinueClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = program.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = program.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${program.progressPercentage}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barre de progression
            LinearProgressIndicator(
                progress = program.progressPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jour ${program.currentDay}/${program.totalDays}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continuer", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun RecommendedProgramsSection(
    recommendedPrograms: List<ProgramsUiState.Program>,
    onProgramClick: (String) -> Unit
) {
    if (recommendedPrograms.isEmpty()) return

    Column {
        Text(
            text = "Recommandés pour vous",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendedPrograms) { program ->
                ProgramCard(
                    program = program,
                    onClick = { onProgramClick(program.id) },
                    modifier = Modifier.width(280.dp)
                )
            }
        }
    }
}

@Composable
private fun PopularChallengesSection(
    popularChallenges: List<ProgramsUiState.Program>,
    onChallengeClick: (String) -> Unit
) {
    if (popularChallenges.isEmpty()) return

    Column {
        Text(
            text = "Défis populaires",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(popularChallenges) { challenge ->
                ChallengeCard(
                    challenge = challenge,
                    onClick = { onChallengeClick(challenge.id) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Composable
private fun AllProgramsSection(
    programsByCategory: Map<String, List<ProgramsUiState.Program>>,
    onProgramClick: (String) -> Unit
) {
    programsByCategory.forEach { (category, programs) ->
        Column {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            programs.chunked(2).forEach { rowPrograms ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPrograms.forEach { program ->
                        ProgramCard(
                            program = program,
                            onClick = { onProgramClick(program.id) },
                            modifier = Modifier.weight(1f),
                            isCompact = true
                        )
                    }

                    // Remplir l'espace si la ligne n'est pas complète
                    if (rowPrograms.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                if (rowPrograms != programs.chunked(2).last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgramCard(
    program: ProgramsUiState.Program,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Badge de durée et niveau
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${program.duration} jours",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!isCompact) {
                    Surface(
                        color = CategoryMeditationLavender.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = program.level,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = CategoryMeditationLavender,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = program.title,
                style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = program.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isCompact) 2 else 3
            )

            if (!isCompact) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${program.participantCount} participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChallengeCard(
    challenge: ProgramsUiState.Program,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CategoryMeditationLavender
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = CategoryMeditationLavender
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${challenge.duration} jours",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = CategoryMeditationLavender
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${challenge.participantCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFF5F0)
@Composable
fun ProgramsScreenPreview() {
    OraTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProgramsScreen(
                onNavigateToProgram = {},
                onNavigateToActiveProgram = {}
            )
        }
    }
}