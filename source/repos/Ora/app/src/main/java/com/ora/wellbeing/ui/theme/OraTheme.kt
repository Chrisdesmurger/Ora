package com.ora.wellbeing.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OraColors.Primary,
    onPrimary = Color.White,
    primaryContainer = OraColors.Primary.copy(alpha = 0.1f),
    onPrimaryContainer = OraColors.Primary,
    secondary = OraColors.YogaGreen,
    onSecondary = Color.White,
    secondaryContainer = OraColors.YogaGreen.copy(alpha = 0.1f),
    onSecondaryContainer = OraColors.YogaGreen,
    tertiary = OraColors.MeditationPurple,
    onTertiary = Color.White,
    tertiaryContainer = OraColors.MeditationPurple.copy(alpha = 0.1f),
    onTertiaryContainer = OraColors.MeditationPurple,
    background = OraColors.Background,
    onBackground = OraColors.TextPrimary,
    surface = OraColors.White,
    onSurface = OraColors.TextPrimary,
    surfaceVariant = OraColors.Background,
    onSurfaceVariant = OraColors.TextSecondary,
    outline = OraColors.TextLight,
    error = OraColors.Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = OraColors.Primary,
    onPrimary = Color.White,
    primaryContainer = OraColors.Primary.copy(alpha = 0.2f),
    onPrimaryContainer = OraColors.Primary,
    secondary = OraColors.YogaGreen,
    onSecondary = Color.White,
    secondaryContainer = OraColors.YogaGreen.copy(alpha = 0.2f),
    onSecondaryContainer = OraColors.YogaGreen,
    tertiary = OraColors.MeditationPurple,
    onTertiary = Color.White,
    tertiaryContainer = OraColors.MeditationPurple.copy(alpha = 0.2f),
    onTertiaryContainer = OraColors.MeditationPurple,
    background = Color(0xFF1C1C1C),
    onBackground = Color.White,
    surface = Color(0xFF2C2C2C),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF808080),
    error = OraColors.Error,
    onError = Color.White
)

@Composable
fun OraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
            val window = (view.context as androidx.activity.ComponentActivity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OraTypography,
        content = content
    )
}