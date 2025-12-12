package com.ora.wellbeing.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.ora.wellbeing.R
import com.ora.wellbeing.presentation.components.OraIcons
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
            .statusBarsPadding()  // Respecter la zone de la barre de statut
            .padding(horizontal = 12.dp)  // Reduire le padding horizontal pour maximiser l'espace
    ) {
        // FIX(build-debug-android): Message de bienvenue personnalise restaure
        WelcomeSection(
            userName = uiState.userName,
            streakDays = uiState.streakDays
        )

        Spacer(modifier = Modifier.height(0.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // FIX(build-debug-android): Sessions rapides restaurees avec textes
            item {
                QuickSessionsSection(
                    onStartQuickSession = onNavigateToQuickSession
                )
            }

            // FIX(build-debug-android): Recommandations du jour restaurees avec textes
            item {
                RecommendationsSection(
                    recommendations = uiState.dailyRecommendations,
                    onContentClick = onNavigateToContent
                )
            }

            // NEW: Section "Pense pour toi" - Personalized recommendations from Cloud Functions
            item {
                PersonalizedRecommendationsSection(
                    recommendations = uiState.personalizedRecommendations,
                    showSection = uiState.showPersonalizedSection,
                    hasCompletedOnboarding = uiState.hasCompletedOnboarding,
                    onContentClick = onNavigateToContent
                )
            }

            // FIX(build-debug-android): Progression des programmes actifs restauree avec textes
            item {
                ActiveProgramsSection(
                    activePrograms = uiState.activePrograms,
                    onProgramClick = onNavigateToProgram
                )
            }

            // FIX(build-debug-android): Statistiques rapides restaurees avec textes
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texte de bienvenue a gauche
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (userName.isNotBlank()) "Bonjour $userName" else "Bonjour",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (streakDays > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$streakDays jours de suite!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Logo Ora a droite
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_ora),
            contentDescription = "Logo Ora",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun QuickSessionsSection(
    onStartQuickSession: (QuickSessionType) -> Unit
) {
    Column {
        // FIX(build-debug-android): Titre de section restaure
        Text(
            text = "Sessions rapides",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TitleOrangeDark
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
    val (backgroundColor, icon, sessionName) = when (sessionType) {
        QuickSessionType.BREATHING -> Triple(CategoryBreathingBlue, OraIcons.Waves, "Respiration")
        QuickSessionType.YOGA_FLASH -> Triple(CategoryYogaGreen, OraIcons.YogaPerson, "Flash Yoga")
        QuickSessionType.MINI_MEDITATION -> Triple(CategoryMeditationLavender, OraIcons.MindHead, "Meditation")
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .height(120.dp), // Hauteur fixe pour un meilleur centrage
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Suppression de l'ombre
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp), // Icone agrandie
                tint = MaterialTheme.colorScheme.background // Meme couleur que le fond de l'app
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = sessionName,
                style = MaterialTheme.typography.titleMedium, // Titre plus grand
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TitleGreenSage // Vert sage pale fonce
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

    // Show only the first recommendation
    val dailyRecommendation = recommendations.firstOrNull() ?: return

    Column {
        Text(
            text = "Decouverte du jour",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TitleOrangeDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        DailyRecommendationCard(
            content = dailyRecommendation,
            onClick = { onContentClick(dailyRecommendation.id) }
        )
    }
}

/**
 * NEW: Personalized Recommendations Section
 * Displays AI-powered recommendations based on user onboarding responses
 * Only shown when user has completed onboarding and recommendations are available
 */
@Composable
private fun PersonalizedRecommendationsSection(
    recommendations: List<HomeUiState.ContentRecommendation>,
    showSection: Boolean,
    hasCompletedOnboarding: Boolean,
    onContentClick: (String) -> Unit
) {
    // Show empty state if onboarding not completed
    if (!hasCompletedOnboarding) {
        PersonalizedEmptyState()
        return
    }

    // Don't show section if no recommendations available yet
    if (!showSection || recommendations.isEmpty()) {
        return
    }

    Column {
        // Section header with AI indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Pense pour toi",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Base sur tes objectifs et preferences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal scroll of 5 recommendation cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(recommendations) { recommendation ->
                PersonalizedRecommendationCard(
                    content = recommendation,
                    onClick = { onContentClick(recommendation.id) }
                )
            }
        }
    }
}

/**
 * Empty state shown when user hasn't completed onboarding
 */
@Composable
private fun PersonalizedEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pense pour toi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Complete ton profil pour recevoir des recommandations personnalisees",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card for personalized recommendation
 * Design identique a ContentCard (ContentCategoryDetailScreen) pour coherence visuelle
 * Format 3:4 portrait avec image de fond, play icon, et titre en overlay
 */
@Composable
private fun PersonalizedRecommendationCard(
    content: HomeUiState.ContentRecommendation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .aspectRatio(3f / 4f), // Format 3:4 portrait
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            val imageUrl = content.previewImageUrl ?: content.thumbnailUrl
            if (imageUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder color if no thumbnail
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }

            // Dark gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Duration badge (top left)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = content.duration,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Play icon (center)
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.9f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Title (bottom)
            Text(
                text = content.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun DailyRecommendationCard(
    content: HomeUiState.ContentRecommendation,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image (preview or thumbnail)
            val imageUrl = content.previewImageUrl ?: content.thumbnailUrl

            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        contentScale = ContentScale.Crop
                    ),
                    contentDescription = content.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback gradient background if no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                )
                            )
                        )
                )
            }

            // Dark overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Play button in center
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Lire",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .padding(12.dp),
                tint = Color.White
            )

            // Content info at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = content.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = content.duration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
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

                // FIX(build-debug-android): Duree restauree
                Text(
                    text = content.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FIX(build-debug-android): Titre et categorie restaures
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
        // FIX(build-debug-android): Titre de section restaure
        Text(
            text = "Programmes en cours",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TitleOrangeDark
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
            containerColor = Color(0xFFFEEFE0) // Beige legerement plus fonce que le fond
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Suppression de l'ombre
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FIX(build-debug-android): Titre du programme restaure
            Text(
                text = program.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FIX(build-debug-android): Progression textuelle restauree
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
            containerColor = Color(0xFFFFF6F0) // Beige/peche tres clair et doux
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Suppression de l'ombre
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // FIX(build-debug-android): Titre de section restaure
            Text(
                text = "Cette semaine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark
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
        // FIX(build-debug-android): Valeur et label restaures
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
