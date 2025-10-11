package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.theme.*
import com.ora.wellbeing.presentation.components.UserAvatar

// FIX(user-dynamic): ProfileScreen mis à jour pour afficher les données Firestore
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToPracticeStats: (String) -> Unit = {},
    onNavigateToGratitudes: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // FIX(user-dynamic): Afficher les erreurs dans un Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // FIX(user-dynamic): Afficher un loader pendant le chargement initial
        if (uiState.isLoading && uiState.userProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chargement du profil...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            ProfileContent(
                uiState = uiState,
                onEditProfileClick = {
                    viewModel.onEvent(ProfileUiEvent.NavigateToEditProfile)
                    onNavigateToEditProfile()
                },
                onPracticeClick = { practiceId ->
                    viewModel.onEvent(ProfileUiEvent.NavigateToPracticeStats(practiceId))
                    onNavigateToPracticeStats(practiceId)
                },
                onGoalToggle = { goalId ->
                    viewModel.onEvent(ProfileUiEvent.ToggleGoal(goalId))
                },
                onGratitudesClick = {
                    viewModel.onEvent(ProfileUiEvent.NavigateToGratitudes)
                    onNavigateToGratitudes()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onEditProfileClick: () -> Unit,
    onPracticeClick: (String) -> Unit,
    onGoalToggle: (String) -> Unit,
    onGratitudesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header avec logo et icône edit
        item {
            ProfileHeader(
                isPremium = uiState.userProfile?.isPremium ?: false,
                onEditClick = onEditProfileClick
            )
        }

        // Photo de profil et nom
        item {
            uiState.userProfile?.let { profile ->
                ProfileUserInfo(profile = profile)
            }
        }

        // Section "MON TEMPS PAR PRATIQUE"
        item {
            PracticeTimeSection(
                practiceTimes = uiState.practiceTimes,
                onPracticeClick = onPracticeClick
            )
        }

        // Section GRATITUDES et OBJECTIFS côte à côte
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // GRATITUDES (gauche)
                GratitudesCard(
                    modifier = Modifier.weight(1f),
                    hasGratitudeToday = uiState.hasGratitudeToday,
                    onClick = onGratitudesClick
                )

                // OBJECTIFS (droite)
                GoalsCard(
                    modifier = Modifier.weight(1f),
                    goals = uiState.goals,
                    onGoalToggle = onGoalToggle
                )
            }
        }

        // Barre de stats en bas
        item {
            BottomStatsBar(
                streak = uiState.streak,
                totalTime = uiState.totalTime,
                lastActivity = uiState.lastActivity
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// FIX(user-dynamic): Header avec badge Premium si applicable
@Composable
private fun ProfileHeader(
    isPremium: Boolean,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo ORA avec effet soleil
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "☀️",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "ORA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            )

            // FIX(user-dynamic): Badge Premium
            if (isPremium) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Icône edit
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Modifier le profil",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// FIX(user-dynamic): Afficher "Invité" si firstName/name est vide
// FIX(avatar): Utilise UserAvatar avec photo ou initiale
@Composable
private fun ProfileUserInfo(profile: UserProfile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Avatar circulaire avec photo ou initiale du prénom
        UserAvatar(
            firstName = profile.firstName.ifBlank { profile.name.ifBlank { "Invité" } },
            photoUrl = profile.photoUrl,
            size = 140.dp,
            fontSize = 60.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // FIX(user-dynamic): Nom en grand titre serif (ou "Invité")
        Text(
            text = profile.name.ifBlank { "Invité" },
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 48.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Motto
        Text(
            text = profile.motto,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        )

        // FIX(user-dynamic): Afficher le plan tier
        if (profile.planTier.isNotBlank() && profile.planTier != "Gratuit") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Plan: ${profile.planTier}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun PracticeTimeSection(
    practiceTimes: List<PracticeTime>,
    onPracticeClick: (String) -> Unit
) {
    Column {
        Text(
            text = "MON TEMPS PAR PRATIQUE",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        practiceTimes.forEach { practice ->
            PracticeCard(
                practice = practice,
                onClick = { onPracticeClick(practice.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PracticeCard(
    practice: PracticeTime,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = practice.color),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                practice.icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = practice.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                )
            }

            Text(
                text = practice.time,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun GratitudesCard(
    modifier: Modifier = Modifier,
    hasGratitudeToday: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "GRATITUDES",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasGratitudeToday) "✓ Complété" else "Aujourd'hui",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = if (hasGratitudeToday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (hasGratitudeToday) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoalsCard(
    modifier: Modifier = Modifier,
    goals: List<Goal>,
    onGoalToggle: (String) -> Unit
) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "OBJECTIFS",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            goals.forEach { goal ->
                GoalItem(
                    goal = goal,
                    onToggle = { onGoalToggle(goal.id) }
                )
            }
        }
    }
}

@Composable
private fun GoalItem(
    goal: Goal,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Checkbox(
            checked = goal.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.tertiary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = goal.text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        )
    }
}

@Composable
private fun BottomStatsBar(
    streak: Int,
    totalTime: String,
    lastActivity: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = "$streak jours",
                label = "d'affilée"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                value = totalTime,
                label = "en tout"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Dernière activité:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
                Text(
                    text = lastActivity,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    OraTheme {
        ProfileContent(
            uiState = ProfileUiState(
                userProfile = UserProfile(
                    name = "Clara",
                    motto = "Je prends soin de moi chaque jour",
                    isPremium = true,
                    planTier = "Premium"
                ),
                practiceTimes = listOf(
                    PracticeTime(
                        id = "yoga",
                        name = "Yoga",
                        time = "3h45 ce mois-ci",
                        color = Color(0xFFF4845F),
                        icon = Icons.Default.SelfImprovement
                    ),
                    PracticeTime(
                        id = "pilates",
                        name = "Pilates",
                        time = "2h15 ce mois-ci",
                        color = Color(0xFFFDB5A0),
                        icon = Icons.Default.FitnessCenter
                    )
                ),
                streak = 5,
                totalTime = "24h10",
                lastActivity = "Yoga doux - 25 min",
                hasGratitudeToday = true,
                goals = listOf(
                    Goal("1", "Lire plus", true),
                    Goal("2", "Arrêter l'alcool", true),
                    Goal("3", "10 min de réseaux sociaux max", false)
                )
            ),
            onEditProfileClick = {},
            onPracticeClick = {},
            onGoalToggle = {},
            onGratitudesClick = {}
        )
    }
}
