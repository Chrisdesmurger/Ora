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

        // Header: User name, photo, settings icon, current month badge
        item {
            ProfileHeader(
                userProfile = uiState.userProfile,
                currentMonthName = uiState.currentMonthName,
                onSettingsClick = onSettingsClick
            )
        }

        // Completed Calendar Section
        item {
            CompletedCalendarSection(
                currentMonthName = uiState.currentMonthName,
                currentMonthPercent = uiState.currentMonthCompletionPercent,
                previousMonthStats = uiState.previousMonthStats
            )
        }

        // My Statistics Section
        item {
            MyStatisticsSection(
                completedWorkouts = uiState.completedWorkouts,
                challengesInProgress = uiState.challengesInProgress,
                completedChallenges = uiState.completedChallenges
            )
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
 * Header with user name, photo, settings icon, and current month badge
 */
@Composable
private fun ProfileHeader(
    userProfile: UserProfile?,
    currentMonthName: String,
    onSettingsClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Settings icon (top right)
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User name
        Text(
            text = userProfile?.name ?: "Invité",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 36.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Profile photo
        UserAvatar(
            firstName = userProfile?.firstName?.ifBlank { userProfile.name.ifBlank { "Invité" } } ?: "Invité",
            photoUrl = userProfile?.photoUrl,
            size = 120.dp,
            fontSize = 54.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current month badge
        CurrentMonthBadge(monthName = currentMonthName)
    }
}

/**
 * Current month badge (e.g., "July")
 */
@Composable
private fun CurrentMonthBadge(monthName: String) {
    Surface(
        color = Color(0xFFFDB5A0), // Light peach from mockup
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = monthName,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

/**
 * Completed Calendar Section
 */
@Composable
private fun CompletedCalendarSection(
    currentMonthName: String,
    currentMonthPercent: Int,
    previousMonthStats: List<MonthlyCompletion>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Completed Calendar",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F0)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large percentage display
                Text(
                    text = "$currentMonthPercent%",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF18D5C), // Coral orange
                        fontSize = 72.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Previous months
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    previousMonthStats.forEach { monthStat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthStat.monthName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "${monthStat.completionPercent}%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp
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
 * My Statistics Section
 */
@Composable
private fun MyStatisticsSection(
    completedWorkouts: Int,
    challengesInProgress: Int,
    completedChallenges: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "My Statistics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticItem(
                    value = completedWorkouts,
                    label = "completed workouts",
                    color = Color(0xFFF18D5C)
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                StatisticItem(
                    value = challengesInProgress,
                    label = "challenges in progress",
                    color = Color(0xFFFDB5A0)
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                StatisticItem(
                    value = completedChallenges,
                    label = "completed challenges",
                    color = Color(0xFF7BA089)
                )
            }
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
private fun StatisticItem(
    value: Int,
    label: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        )
    }
}

/**
 * Challenge in Progress Section
 */
@Composable
private fun ChallengeProgressSection(challenge: ActiveChallenge) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Challenge in progress",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 22.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = challenge.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Day ${challenge.currentDay}/${challenge.totalDays}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                Column {
                    LinearProgressIndicator(
                        progress = challenge.progressPercent / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFF9B7A), // Orange from mockup
                        trackColor = Color(0xFFFFE5D9)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${challenge.progressPercent}%",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9B7A),
                            fontSize = 14.sp
                        )
                    )
                }
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
 * Individual favorite card
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
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                modifier = Modifier.size(32.dp)
            )

            Column {
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
