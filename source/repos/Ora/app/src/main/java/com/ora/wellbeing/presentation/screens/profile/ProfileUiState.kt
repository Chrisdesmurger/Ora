package com.ora.wellbeing.presentation.screens.profile

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * État de l'interface utilisateur pour l'écran Profile
 * Modélise exactement le mockup image9.png
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null,
    val practiceTimes: List<PracticeTime> = emptyList(),
    val streak: Int = 0,
    val totalTime: String = "",
    val lastActivity: String = "",
    val hasGratitudeToday: Boolean = false,
    val goals: List<Goal> = emptyList()
)

/**
 * Profil utilisateur simplifié selon mockup
 */
data class UserProfile(
    val name: String,
    val motto: String,
    val photoUrl: String? = null
)

/**
 * Temps de pratique par activité
 */
data class PracticeTime(
    val id: String,
    val name: String,
    val time: String,
    val color: Color,
    val icon: ImageVector? = null
)

/**
 * Objectif utilisateur avec état de complétion
 */
data class Goal(
    val id: String,
    val text: String,
    val isCompleted: Boolean
)