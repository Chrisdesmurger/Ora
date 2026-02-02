package com.ora.wellbeing.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Couleurs Ora - Palette Chaude et Apaisante
// Extraites directement des mockups officiels Ora

// Couleur principale - Orange Ora (Logo, boutons, accents)
private val LightPrimary = Color(0xFFF4845F)        // Orange Ora principal
private val LightSecondary = Color(0xFFFDB5A0)      // Orange clair (lighter)
private val LightTertiary = Color(0xFF7BA089)       // Vert sage

// Arriere-plans et surfaces
private val LightBackground = Color(0xFFFFF5F0)     // Fond beige chaud (mockup exact)
private val LightSurface = Color(0xFFFFFBF8)        // Cartes blanc casse
private val LightSurfaceVariant = Color(0xFFFFF9F0) // Variant pour inputs

// Textes
private val LightOnPrimary = Color.White            // Texte sur orange
private val LightOnSecondary = Color(0xFF3D2C2C)    // Texte fonce
private val LightOnTertiary = Color.White           // Texte sur vert
private val LightOnBackground = Color(0xFF2C2C2C)   // Texte principal (brun tres fonce)
private val LightOnSurface = Color(0xFF2C2C2C)      // Texte sur surfaces
private val LightOnSurfaceVariant = Color(0xFF6B6B6B) // Texte moyen (gris)

// Couleurs de categories - Palette Ora plus saturee et chaleureuse
val CategoryYogaGreen = Color(0xFF7BA089)           // Yoga - Vert sage (tertiary color)
val CategoryPilatesPeach = Color(0xFFFDB5A0)        // Pilates - Orange peche (secondary color)
val CategoryMeditationLavender = Color(0xFFD4C4E8)  // Meditation - Lavande (conservee)
val CategoryBreathingBlue = Color(0xFFC4D8E8)       // Respiration - Bleu clair
val CategoryWellnessBeige = Color(0xFFFFE5CC)       // Bien-etre - Orange peche doux (GratitudePeach)
// Issue #37: Added Auto-massage category color
val CategoryAutoMassageWarm = Color(0xFFE8D4C4)     // Auto-massage - Beige rose chaud

// Couleurs pour les cartes de gratitude (mockup Journal)
val GratitudePink = Color(0xFFFFD4CC)               // Rose peche
val GratitudePeach = Color(0xFFFFE5CC)              // Orange peche
val GratitudeMint = Color(0xFFD4E8D9)               // Vert menthe
val GratitudeYellow = Color(0xFFFFF4CC)             // Jaune doux

// Couleurs pour les titres (Design System - Option 2 & 3)
val TitleOrangeDark = Color(0xFFD76B3D)             // Orange fonce pour titres principaux
val TitleGreenSage = Color(0xFF7BA089)              // Vert sage pour sections zen/journal

// Mode sombre - Adaptation de la palette chaude
private val DarkPrimary = Color(0xFFF5A879)         // Orange plus clair pour le dark mode
private val DarkSecondary = Color(0xFFE8B892)       // Peach plus sature
private val DarkTertiary = Color(0xFF94B5A0)        // Vert sage plus vif

private val DarkBackground = Color(0xFF2A2520)      // Brun tres fonce
private val DarkSurface = Color(0xFF3A3330)         // Brun fonce pour cartes
private val DarkSurfaceVariant = Color(0xFF4A4038)  // Brun moyen

private val DarkOnPrimary = Color(0xFF1A1A1A)       // Texte tres fonce sur orange
private val DarkOnSecondary = Color(0xFF1A1A1A)     // Texte fonce sur peach
private val DarkOnTertiary = Color(0xFF1A1A1A)      // Texte fonce sur vert
private val DarkOnBackground = Color(0xFFE8E0D8)    // Texte clair sur fond sombre
private val DarkOnSurface = Color(0xFFE8E0D8)       // Texte clair sur surfaces

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onTertiary = LightOnTertiary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onTertiary = DarkOnTertiary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

@Composable
fun OraTheme(
    darkTheme: Boolean = false, // FORCE Light Theme - pas de dark mode
    dynamicColor: Boolean = false, // Desactive par defaut pour garder la palette Ora
    content: @Composable () -> Unit
) {
    // Toujours utiliser le theme clair (fond beige #FFF5F0)
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            // Toujours en mode clair (status bar avec icones sombres sur fond clair)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OraTypography,
        content = content
    )
}
