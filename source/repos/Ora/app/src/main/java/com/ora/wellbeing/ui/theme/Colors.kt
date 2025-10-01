package com.ora.wellbeing.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette de couleurs Ora - Design zen et apaisant
 * FIX(build-debug-android): Ajout de toutes les couleurs référencées dans les screens
 */
object OraColors {
    // Couleurs principales
    val Primary = Color(0xFF6B4FA1) // Violet zen
    val Secondary = Color(0xFF4FA19F) // Teal apaisant
    val Tertiary = Color(0xFFF4A5A5) // Rose doux

    // Catégories de contenu
    val MeditationPurple = Color(0xFF7B68EE)
    val YogaGreen = Color(0xFF4CAF50)
    val JournalPink = Color(0xFFFF6B9D)
    val ProgramsBlue = Color(0xFF42A5F5)

    // FIX: Ajout couleurs manquantes pour les écrans
    val SoftPink = Color(0xFFF4A5A5)
    val BreathingBlue = Color(0xFF64B5F6)

    // FIX: Ajout couleurs pour les gratitudes (3 teintes différentes)
    val Gratitude1 = Color(0xFFFFE0B2) // Orange pâle
    val Gratitude2 = Color(0xFFC8E6C9) // Vert pâle
    val Gratitude3 = Color(0xFFD1C4E9) // Violet pâle

    // Niveaux de difficulté
    val LevelBeginner = Color(0xFF81C784)
    val LevelIntermediate = Color(0xFFFFB74D)
    val LevelAdvanced = Color(0xFFE57373)

    // Textes
    val TextPrimary = Color(0xFF2C2C2C)
    val TextSecondary = Color(0xFF757575)
    val TextLight = Color(0xFFBDBDBD)

    // Backgrounds
    val Background = Color(0xFFF8F9FA)
    val White = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)

    // États
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFE53935)
    val Info = Color(0xFF2196F3)

    // Streak colors
    val StreakBronze = Color(0xFFCD7F32)
    val StreakSilver = Color(0xFFC0C0C0)
    val StreakGold = Color(0xFFFFD700)

    // Gradients (start, end colors)
    val GradientMeditation = listOf(Color(0xFF7B68EE), Color(0xFF9F7AEA))
    val GradientYoga = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
    val GradientJournal = listOf(Color(0xFFFF6B9D), Color(0xFFFF8AB4))
    val GradientProgram = listOf(Color(0xFF42A5F5), Color(0xFF64B5F6))
}