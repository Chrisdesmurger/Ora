package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * État de l'interface utilisateur pour l'écran Profile
 * Redesign basé sur le mockup (Issue #64)
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null,

    // Monthly completion stats
    val currentMonthName: String = "",
    val currentMonthCompletionPercent: Int = 0,
    val previousMonthStats: List<MonthlyCompletion> = emptyList(),

    // My Statistics
    val completedWorkouts: Int = 0,
    val challengesInProgress: Int = 0,
    val completedChallenges: Int = 0,

    // Challenge in Progress
    val activeChallenge: ActiveChallenge? = null,

    // Favorites
    val favoriteWorkoutsCount: Int = 0,
    val favoriteChallengesCount: Int = 0,

    // Legacy fields (kept for backward compatibility)
    val practiceTimes: List<PracticeTime> = emptyList(),
    val streak: Int = 0,
    val totalTime: String = "",
    val lastActivity: String = "",
    val hasGratitudeToday: Boolean = false,
    val goals: List<Goal> = emptyList()
)

/**
 * Profil utilisateur
 */
data class UserProfile(
    val name: String,
    val firstName: String = "",
    val motto: String,
    val photoUrl: String? = null,
    val isPremium: Boolean = false,
    val planTier: String = "Gratuit"
)

/**
 * Monthly completion statistics
 */
data class MonthlyCompletion(
    val monthName: String, // e.g., "June", "May"
    val completionPercent: Int // 0-100
)

/**
 * Active challenge data
 */
data class ActiveChallenge(
    val id: String,
    val name: String,
    val progressPercent: Int, // 0-100
    val currentDay: Int,
    val totalDays: Int
)

/**
 * Temps de pratique par activité (legacy)
 */
data class PracticeTime(
    val id: String,
    val name: String,
    val time: String,
    val color: Color,
    val icon: ImageVector? = null
)

/**
 * Objectif utilisateur avec état de complétion (legacy)
 */
data class Goal(
    val id: String,
    val text: String,
    val isCompleted: Boolean
)
