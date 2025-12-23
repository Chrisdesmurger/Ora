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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.R
import com.ora.wellbeing.presentation.theme.*
import com.ora.wellbeing.presentation.components.UserAvatar

/**
 * ProfileScreen - Complete redesign based on mockup (Issue #64)
 * Shows monthly stats, challenge progress, and favorites
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show errors in Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.userProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            ProfileContent(
                uiState = uiState,
                onSettingsClick = onNavigateToSettings
            )
        }

        // SnackbarHost positioned at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)  // Au-dessus de la navigation bar
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()  // Respecter la zone de la barre de statut
            .padding(horizontal = 16.dp),  // Réduire le padding horizontal pour maximiser l'espace
        verticalArrangement = Arrangement.spacedBy(12.dp),  // Réduire l'espacement entre les items
        contentPadding = PaddingValues(bottom = 16.dp)  // Padding en bas uniquement
    ) {

        // Header: "Hi, Name" on left, photo on right, settings icon
        item {
            ProfileHeader(
                userProfile = uiState.userProfile,
                currentMonthName = uiState.currentMonthName,
                onSettingsClick = onSettingsClick
            )
        }

        // Calendar and Statistics side by side (like mockup)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left: Completed Calendar
                CompletedCalendarSection(
                    currentMonthName = uiState.currentMonthName,
                    currentMonthPercent = uiState.currentMonthCompletionPercent,
                    previousMonthStats = uiState.previousMonthStats,
                    modifier = Modifier.weight(1f)
                )

                // Right: My Statistics
                MyStatisticsSection(
                    completedWorkouts = uiState.completedWorkouts,
                    challengesInProgress = uiState.challengesInProgress,
                    completedChallenges = uiState.completedChallenges,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Challenge in Progress Section
        uiState.activeChallenge?.let { challenge ->
            item {
                ChallengeProgressSection(challenge = challenge)
            }
        }

        // Favorites Section
        item {
            FavoritesSection(
                favoriteWorkoutsCount = uiState.favoriteWorkoutsCount,
                favoriteChallengesCount = uiState.favoriteChallengesCount
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

/**
 * Header with "Hi, Name" on left, photo on right (like mockup)
 */
@Composable
private fun ProfileHeader(
    userProfile: UserProfile?,
    currentMonthName: String,
    onSettingsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top row: "Hi, Name" on left, Settings icon + Photo on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: "Hi, Name"
            Column {
                Text(
                    text = stringResource(R.string.profile_hi),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = userProfile?.firstName?.ifBlank { userProfile.name } ?: stringResource(R.string.greeting_guest),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 32.sp
                    )
                )
            }

            // Right: Settings icon + Profile photo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                UserAvatar(
                    firstName = userProfile?.firstName?.ifBlank { userProfile.name.ifBlank { stringResource(R.string.greeting_guest) } } ?: stringResource(R.string.greeting_guest),
                    photoUrl = userProfile?.photoUrl,
                    size = 80.dp,
                    fontSize = 36.sp
                )
            }
        }

    }
}

/**
 * Completed Calendar Section (compact, vertical layout with card background like mockup)
 */
@Composable
private fun CompletedCalendarSection(
    currentMonthName: String,
    currentMonthPercent: Int,
    previousMonthStats: List<MonthlyCompletion>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Title outside card: "Current month [MonthName]" with calendar icon
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color(0xFFF18D5C),
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.profile_current_month),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TitleOrangeDark,
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TitleOrangeDark,
                        fontSize = 22.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card with white/pale background
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF5)), // Very pale peach
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {

        // Current month percentage (LARGE with orange curve accent like mockup)
        Column(horizontalAlignment = Alignment.Start) {
            // Large percentage with decorative curve
            Box(
                modifier = Modifier
                    .drawBehind {
                        // Draw orange decorative arc (like in mockup)
                        val strokeWidth = 6.dp.toPx()
                        val arcSize = size.width * 0.28f

                        drawArc(
                            color = Color(0xFFF18D5C),
                            startAngle = -135f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth),
                            topLeft = Offset(-arcSize * 0.2f, -arcSize * 0.1f),
                            size = androidx.compose.ui.geometry.Size(arcSize, arcSize)
                        )
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$currentMonthPercent",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF18D5C), // Coral orange
                            fontSize = 56.sp
                        )
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF18D5C),
                            fontSize = 42.sp
                        ),
                        modifier = Modifier.offset(y = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Previous months (vertical list like mockup)
            previousMonthStats.forEach { monthStat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthStat.monthName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.program_progress_percentage, monthStat.completionPercent),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF18D5C),
                            fontSize = 14.sp
                        )
                    )
                }
            }
                }
            }
        }
    }
}

/**
 * My Statistics Section (large numbers on left with card background like mockup)
 */
@Composable
private fun MyStatisticsSection(
    completedWorkouts: Int,
    challengesInProgress: Int,
    completedChallenges: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Title outside card
        Text(
            text = stringResource(R.string.profile_my),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark,
                fontSize = 16.sp
            )
        )
        Text(
            text = stringResource(R.string.profile_statistics),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card with white/pale background
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF5)), // Very pale peach
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatisticItemLarge(
                    value = completedWorkouts,
                    label = stringResource(R.string.profile_completed_workouts),
                    color = Color(0xFFF18D5C)
                )

                StatisticItemLarge(
                    value = challengesInProgress,
                    label = stringResource(R.string.profile_challenges_in_progress),
                    color = Color(0xFFFDB5A0)
                )

                StatisticItemLarge(
                    value = completedChallenges,
                    label = stringResource(R.string.profile_completed_challenges),
                    color = Color(0xFF7BA089)
                )
            }
        }
    }
}

/**
 * Individual statistic item with large number (like mockup)
 */
@Composable
private fun StatisticItemLarge(
    value: Int,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Very large number on left (like mockup)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 48.sp
            )
        )

        // Label on right
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 17.sp
            )
        )
    }
}

/**
 * Challenge in Progress Section (horizontal layout with card background like mockup)
 */
@Composable
private fun ChallengeProgressSection(challenge: ActiveChallenge) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Title outside card
        Text(
            text = stringResource(R.string.profile_challenge),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark,
                fontSize = 16.sp
            )
        )
        Text(
            text = stringResource(R.string.profile_in_progress),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TitleOrangeDark,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card with white/pale background
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAF5)), // Very pale peach
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Challenge name on left, percentage on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = stringResource(R.string.program_progress_percentage, challenge.progressPercent),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF18D5C),
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = challenge.progressPercent / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFF9B7A), // Orange from mockup
                    trackColor = Color(0xFFFFE5D9)
                )
            }
        }
    }
}

/**
 * Favorites Section (two cards side by side)
 */
@Composable
private fun FavoritesSection(
    favoriteWorkoutsCount: Int,
    favoriteChallengesCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Favorite Workouts
        FavoriteCard(
            count = favoriteWorkoutsCount,
            label = stringResource(R.string.profile_favorite_workouts),
            icon = Icons.Default.FavoriteBorder,
            backgroundColor = Color(0xFFFFF9F0),
            modifier = Modifier.weight(1f)
        )

        // Favorite Challenges
        FavoriteCard(
            count = favoriteChallengesCount,
            label = stringResource(R.string.profile_favorite_challenges),
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFFFFF5F0),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual favorite card (compact layout like mockup)
 */
@Composable
private fun FavoriteCard(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF18D5C),
                modifier = Modifier.size(26.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 32.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    ),
                    maxLines = 2,
                    softWrap = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
