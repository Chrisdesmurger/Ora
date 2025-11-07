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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.userProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            ProfileContent(
                uiState = uiState,
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier.padding(paddingValues)
            )
        }
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
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

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

        item { Spacer(modifier = Modifier.height(16.dp)) }
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
                    text = "Hi,",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = userProfile?.firstName?.ifBlank { userProfile.name } ?: "Invité",
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
                    firstName = userProfile?.firstName?.ifBlank { userProfile.name.ifBlank { "Invité" } } ?: "Invité",
                    photoUrl = userProfile?.photoUrl,
                    size = 80.dp,
                    fontSize = 36.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current month badge
        CurrentMonthBadge(monthName = currentMonthName)
    }
}

/**
 * Current month badge with calendar icon (like mockup)
 */
@Composable
private fun CurrentMonthBadge(monthName: String) {
    Surface(
        color = Color(0xFFFDB5A0), // Light peach from mockup
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Current month",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

/**
 * Completed Calendar Section (compact, vertical layout like mockup)
 */
@Composable
private fun CompletedCalendarSection(
    currentMonthName: String,
    currentMonthPercent: Int,
    previousMonthStats: List<MonthlyCompletion>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Completed",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        )
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Current month percentage (LARGE with orange curve accent like mockup)
        Column(horizontalAlignment = Alignment.Start) {
            // Large percentage with decorative curve
            Box(
                modifier = Modifier
                    .drawBehind {
                        // Draw orange decorative arc (like in mockup)
                        val strokeWidth = 8.dp.toPx()
                        val arcSize = size.width * 0.3f

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
                            fontSize = 64.sp
                        )
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF18D5C),
                            fontSize = 48.sp
                        ),
                        modifier = Modifier.offset(y = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        text = "${monthStat.completionPercent}%",
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

/**
 * My Statistics Section (large numbers on left like mockup)
 */
@Composable
private fun MyStatisticsSection(
    completedWorkouts: Int,
    challengesInProgress: Int,
    completedChallenges: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "My",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        )
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Stats with large numbers on left
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            StatisticItemLarge(
                value = completedWorkouts,
                label = "completed\nworkouts",
                color = Color(0xFFF18D5C)
            )

            StatisticItemLarge(
                value = challengesInProgress,
                label = "challenges\nin progress",
                color = Color(0xFFFDB5A0)
            )

            StatisticItemLarge(
                value = completedChallenges,
                label = "completed\nchallenges",
                color = Color(0xFF7BA089)
            )
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Very large number on left (like mockup)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 56.sp
            )
        )

        // Label on right
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 19.sp
            )
        )
    }
}

/**
 * Challenge in Progress Section (horizontal layout like mockup)
 */
@Composable
private fun ChallengeProgressSection(challenge: ActiveChallenge) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Challenge",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF18D5C),
                fontSize = 16.sp
            )
        )
        Text(
            text = "in progress",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF18D5C),
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                text = "${challenge.progressPercent}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF18D5C),
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            label = "Favorite Workouts",
            icon = Icons.Default.FavoriteBorder,
            backgroundColor = Color(0xFFFFF9F0),
            modifier = Modifier.weight(1f)
        )

        // Favorite Challenges
        FavoriteCard(
            count = favoriteChallengesCount,
            label = "Favorite Challenges",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF18D5C),
                modifier = Modifier.size(28.dp)
            )

            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 36.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
