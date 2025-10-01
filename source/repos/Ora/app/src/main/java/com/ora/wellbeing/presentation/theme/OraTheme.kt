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
// Basée sur l'analyse des mockups officiels Ora

// Couleur principale - Orange Coral (Logo et CTA)
private val LightPrimary = Color(0xFFF18D5C)        // Orange coral chaleureux
private val LightSecondary = Color(0xFFF5C9A9)      // Peach doux (accents)
private val LightTertiary = Color(0xFFA8C5B0)       // Vert sage (Yoga)

// Arrière-plans et surfaces
private val LightBackground = Color(0xFFF5EFE6)     // Beige/crème chaleureux
private val LightSurface = Color(0xFFFFFBF8)        // Blanc cassé légèrement chaud
private val LightSurfaceVariant = Color(0xFFF5E1C9) // Peach très clair pour cartes

// Textes
private val LightOnPrimary = Color.White            // Texte sur orange
private val LightOnSecondary = Color(0xFF4A4A4A)    // Texte foncé sur peach
private val LightOnTertiary = Color(0xFF2D3D2D)     // Texte foncé sur vert
private val LightOnBackground = Color(0xFF4A4A4A)   // Texte principal (gris foncé)
private val LightOnSurface = Color(0xFF4A4A4A)      // Texte sur surfaces

// Couleurs de catégories (pour badges et cartes)
val CategoryYogaGreen = Color(0xFFA8C5B0)           // Yoga - Vert sage
val CategoryPilatesPeach = Color(0xFFF5C9A9)        // Pilates - Peach
val CategoryMeditationLavender = Color(0xFFC5B8D4)  // Méditation - Lavande
val CategoryBreathingBlue = Color(0xFFA3C4E0)       // Respiration - Bleu clair

// Couleurs pour les cartes de gratitude
val GratitudePink = Color(0xFFF5D4D4)               // Rose très doux
val GratitudePeach = Color(0xFFF5E1C9)              // Peach doux
val GratitudeMint = Color(0xFFD4E8D4)               // Menthe douce

// Mode sombre - Adaptation de la palette chaude
private val DarkPrimary = Color(0xFFF5A879)         // Orange plus clair pour le dark mode
private val DarkSecondary = Color(0xFFE8B892)       // Peach plus saturé
private val DarkTertiary = Color(0xFF94B5A0)        // Vert sage plus vif

private val DarkBackground = Color(0xFF2A2520)      // Brun très foncé
private val DarkSurface = Color(0xFF3A3330)         // Brun foncé pour cartes
private val DarkSurfaceVariant = Color(0xFF4A4038)  // Brun moyen

private val DarkOnPrimary = Color(0xFF1A1A1A)       // Texte très foncé sur orange
private val DarkOnSecondary = Color(0xFF1A1A1A)     // Texte foncé sur peach
private val DarkOnTertiary = Color(0xFF1A1A1A)      // Texte foncé sur vert
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivé par défaut pour garder la palette Ora
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OraTypography,
        content = content
    )
}
