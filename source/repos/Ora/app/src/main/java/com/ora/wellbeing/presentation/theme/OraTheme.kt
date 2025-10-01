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

// Couleurs Ora pour le bien-être
private val LightPrimary = Color(0xFF6B73FF)      // Bleu violet zen
private val LightSecondary = Color(0xFF03DAC6)     // Teal apaisante
private val LightTertiary = Color(0xFFFF6B9D)      // Rose doux
private val LightBackground = Color(0xFFFFFBFE)     // Blanc cassé
private val LightSurface = Color(0xFFF7F2FA)       // Violet très clair
private val LightOnPrimary = Color.White
private val LightOnSecondary = Color.Black
private val LightOnTertiary = Color.White
private val LightOnBackground = Color(0xFF1C1B1F)
private val LightOnSurface = Color(0xFF1C1B1F)

private val DarkPrimary = Color(0xFF8B93FF)        // Bleu violet plus clair
private val DarkSecondary = Color(0xFF66FFF2)      // Teal lumineuse
private val DarkTertiary = Color(0xFFFF94B3)       // Rose plus clair
private val DarkBackground = Color(0xFF1C1B1F)     // Noir doux
private val DarkSurface = Color(0xFF2B2930)        // Gris foncé
private val DarkOnPrimary = Color.Black
private val DarkOnSecondary = Color.Black
private val DarkOnTertiary = Color.Black
private val DarkOnBackground = Color(0xFFE6E1E5)
private val DarkOnSurface = Color(0xFFE6E1E5)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
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
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onTertiary = DarkOnTertiary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
)

@Composable
fun OraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OraTypography,
        content = content
    )
}