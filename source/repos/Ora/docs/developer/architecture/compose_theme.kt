package com.ora.wellbeing.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Ora Brand Color Palette
object OraColors {
    // Primary Brand Colors
    val Primary = Color(0xFF7B5CF2) // Violet apaisant principal
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFE9E1FF)
    val OnPrimaryContainer = Color(0xFF2D0F5C)

    // Secondary Colors (Saumon chaleureux)
    val Secondary = Color(0xFFFF8A80)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFEBEE)
    val OnSecondaryContainer = Color(0xFF5D1B1B)

    // Tertiary Colors (Vert nature)
    val Tertiary = Color(0xFF4CAF50)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFE8F5E8)
    val OnTertiaryContainer = Color(0xFF1B5D1F)

    // Neutral Colors
    val Surface = Color(0xFFFFFBFF)
    val OnSurface = Color(0xFF1D1B20)
    val SurfaceVariant = Color(0xFFF5F1F8)
    val OnSurfaceVariant = Color(0xFF4A4458)
    val Background = Color(0xFFFFFBFF)
    val OnBackground = Color(0xFF1D1B20)

    // Outline & Borders
    val Outline = Color(0xFF7C7589)
    val OutlineVariant = Color(0xFFCCC2DC)

    // Error Colors
    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // Dark Theme Colors
    object Dark {
        val Primary = Color(0xFFD0BBFF)
        val OnPrimary = Color(0xFF432377)
        val PrimaryContainer = Color(0xFF5B3B9E)
        val OnPrimaryContainer = Color(0xFFE9E1FF)

        val Secondary = Color(0xFFFFB3B3)
        val OnSecondary = Color(0xFF733030)
        val SecondaryContainer = Color(0xFF8C4545)
        val OnSecondaryContainer = Color(0xFFFFEBEE)

        val Tertiary = Color(0xFF81C784)
        val OnTertiary = Color(0xFF2E7D32)
        val TertiaryContainer = Color(0xFF388E3C)
        val OnTertiaryContainer = Color(0xFFE8F5E8)

        val Surface = Color(0xFF141218)
        val OnSurface = Color(0xFFE6E0E9)
        val SurfaceVariant = Color(0xFF4A4458)
        val OnSurfaceVariant = Color(0xFFCCC2DC)
        val Background = Color(0xFF141218)
        val OnBackground = Color(0xFFE6E0E9)

        val Outline = Color(0xFF968DA3)
        val OutlineVariant = Color(0xFF4A4458)

        val Error = Color(0xFFFFB4AB)
        val OnError = Color(0xFF690005)
        val ErrorContainer = Color(0xFF93000A)
        val OnErrorContainer = Color(0xFFFFDAD6)
    }

    // Semantic Colors for Wellbeing App
    val YogaPrimary = Color(0xFF8E24AA) // Violet yoga
    val MeditationPrimary = Color(0xFF26A69A) // Vert méditation
    val BreathingPrimary = Color(0xFF42A5F5) // Bleu respiration
    val JournalPrimary = Color(0xFFFF7043) // Orange journal
    val PilatesPrimary = Color(0xFFAB47BC) // Violet pilates

    // Mood Colors
    val MoodExcellent = Color(0xFF4CAF50)
    val MoodGood = Color(0xFF8BC34A)
    val MoodNeutral = Color(0xFFFFC107)
    val MoodBad = Color(0xFFFF9800)
    val MoodTerrible = Color(0xFFF44336)

    // Progress & Achievement Colors
    val SuccessGreen = Color(0xFF4CAF50)
    val WarningOrange = Color(0xFFFF9800)
    val InfoBlue = Color(0xFF2196F3)
    val StreakFire = Color(0xFFFF5722)
}

// Typography avec font system
val OraTypography = Typography(
    displayLarge = Typography().displayLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light
    ),
    displayMedium = Typography().displayMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light
    ),
    displaySmall = Typography().displaySmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = Typography().headlineLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = Typography().headlineSmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = Typography().titleLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = Typography().titleMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = Typography().titleSmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = Typography().bodyLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = Typography().bodyMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = Typography().bodySmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = Typography().labelLarge.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = Typography().labelMedium.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = Typography().labelSmall.copy(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium
    )
)

// Light Theme ColorScheme
private val OraLightColorScheme = lightColorScheme(
    primary = OraColors.Primary,
    onPrimary = OraColors.OnPrimary,
    primaryContainer = OraColors.PrimaryContainer,
    onPrimaryContainer = OraColors.OnPrimaryContainer,
    secondary = OraColors.Secondary,
    onSecondary = OraColors.OnSecondary,
    secondaryContainer = OraColors.SecondaryContainer,
    onSecondaryContainer = OraColors.OnSecondaryContainer,
    tertiary = OraColors.Tertiary,
    onTertiary = OraColors.OnTertiary,
    tertiaryContainer = OraColors.TertiaryContainer,
    onTertiaryContainer = OraColors.OnTertiaryContainer,
    error = OraColors.Error,
    onError = OraColors.OnError,
    errorContainer = OraColors.ErrorContainer,
    onErrorContainer = OraColors.OnErrorContainer,
    background = OraColors.Background,
    onBackground = OraColors.OnBackground,
    surface = OraColors.Surface,
    onSurface = OraColors.OnSurface,
    surfaceVariant = OraColors.SurfaceVariant,
    onSurfaceVariant = OraColors.OnSurfaceVariant,
    outline = OraColors.Outline,
    outlineVariant = OraColors.OutlineVariant
)

// Dark Theme ColorScheme
private val OraDarkColorScheme = darkColorScheme(
    primary = OraColors.Dark.Primary,
    onPrimary = OraColors.Dark.OnPrimary,
    primaryContainer = OraColors.Dark.PrimaryContainer,
    onPrimaryContainer = OraColors.Dark.OnPrimaryContainer,
    secondary = OraColors.Dark.Secondary,
    onSecondary = OraColors.Dark.OnSecondary,
    secondaryContainer = OraColors.Dark.SecondaryContainer,
    onSecondaryContainer = OraColors.Dark.OnSecondaryContainer,
    tertiary = OraColors.Dark.Tertiary,
    onTertiary = OraColors.Dark.OnTertiary,
    tertiaryContainer = OraColors.Dark.TertiaryContainer,
    onTertiaryContainer = OraColors.Dark.OnTertiaryContainer,
    error = OraColors.Dark.Error,
    onError = OraColors.Dark.OnError,
    errorContainer = OraColors.Dark.ErrorContainer,
    onErrorContainer = OraColors.Dark.OnErrorContainer,
    background = OraColors.Dark.Background,
    onBackground = OraColors.Dark.OnBackground,
    surface = OraColors.Dark.Surface,
    onSurface = OraColors.Dark.OnSurface,
    surfaceVariant = OraColors.Dark.SurfaceVariant,
    onSurfaceVariant = OraColors.Dark.OnSurfaceVariant,
    outline = OraColors.Dark.Outline,
    outlineVariant = OraColors.Dark.OutlineVariant
)

// Custom Shapes
val OraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Local Composition pour couleurs customisées
data class OraExtendedColors(
    val yogaPrimary: Color,
    val meditationPrimary: Color,
    val breathingPrimary: Color,
    val journalPrimary: Color,
    val pilatesPrimary: Color,
    val moodExcellent: Color,
    val moodGood: Color,
    val moodNeutral: Color,
    val moodBad: Color,
    val moodTerrible: Color,
    val successGreen: Color,
    val warningOrange: Color,
    val infoBlue: Color,
    val streakFire: Color
)

val LocalOraExtendedColors = staticCompositionLocalOf {
    OraExtendedColors(
        yogaPrimary = Color.Unspecified,
        meditationPrimary = Color.Unspecified,
        breathingPrimary = Color.Unspecified,
        journalPrimary = Color.Unspecified,
        pilatesPrimary = Color.Unspecified,
        moodExcellent = Color.Unspecified,
        moodGood = Color.Unspecified,
        moodNeutral = Color.Unspecified,
        moodBad = Color.Unspecified,
        moodTerrible = Color.Unspecified,
        successGreen = Color.Unspecified,
        warningOrange = Color.Unspecified,
        infoBlue = Color.Unspecified,
        streakFire = Color.Unspecified
    )
}

private val lightExtendedColors = OraExtendedColors(
    yogaPrimary = OraColors.YogaPrimary,
    meditationPrimary = OraColors.MeditationPrimary,
    breathingPrimary = OraColors.BreathingPrimary,
    journalPrimary = OraColors.JournalPrimary,
    pilatesPrimary = OraColors.PilatesPrimary,
    moodExcellent = OraColors.MoodExcellent,
    moodGood = OraColors.MoodGood,
    moodNeutral = OraColors.MoodNeutral,
    moodBad = OraColors.MoodBad,
    moodTerrible = OraColors.MoodTerrible,
    successGreen = OraColors.SuccessGreen,
    warningOrange = OraColors.WarningOrange,
    infoBlue = OraColors.InfoBlue,
    streakFire = OraColors.StreakFire
)

private val darkExtendedColors = lightExtendedColors // Même couleurs pour le moment

// Extension pour accès facile aux couleurs extended
val MaterialTheme.oraColors: OraExtendedColors
    @Composable
    get() = LocalOraExtendedColors.current

@Composable
fun OraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Désactivé pour garder la cohérence de marque
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> OraDarkColorScheme
        else -> OraLightColorScheme
    }

    val extendedColors = when {
        darkTheme -> darkExtendedColors
        else -> lightExtendedColors
    }

    CompositionLocalProvider(
        LocalOraExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OraTypography,
            shapes = OraShapes,
            content = content
        )
    }
}