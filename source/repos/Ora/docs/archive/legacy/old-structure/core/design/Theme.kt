package core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Couleurs Ora basées sur les mockups
object OraColors {
    // Couleurs principales
    val OraOrange = Color(0xFFFF6B35)
    val OraOrangeLight = Color(0xFFFFB396)
    val OraBeige = Color(0xFFFFF8F0)

    // Couleurs catégories
    val YogaGreen = Color(0xFF7CB342)
    val PilatesOrange = Color(0xFFFF7043)
    val MeditationPurple = Color(0xFF9C27B0)
    val BreathingBlue = Color(0xFF42A5F5)

    // Couleurs mood/UI
    val MoodHappy = Color(0xFF4CAF50)
    val MoodNeutral = Color(0xFFFF9800)
    val MoodSad = Color(0xFFFF5722)
    val MoodAngry = Color(0xFFF44336)

    // Couleurs gratitude cards
    val GratitudePink = Color(0xFFFFCDD2)
    val GratitudeOrange = Color(0xFFFFE0B2)
    val GratitudeGreen = Color(0xFFC8E6C9)

    // Couleurs navigation
    val BottomNavPink = Color(0xFFFFE4E1)
    val BottomNavSelected = Color(0xFFFF6B35)
    val BottomNavUnselected = Color(0xFF9E9E9E)

    // Couleurs de base
    val Background = Color(0xFFFFFBF7)
    val Surface = Color.White
    val OnSurface = Color(0xFF2C2C2C)
    val OnSurfaceVariant = Color(0xFF757575)
}

// Formes Ora avec coins arrondis
val OraShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

// Typographie Ora
val OraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

// Schéma de couleurs Material 3 adapté à Ora
private val OraLightColorScheme = lightColorScheme(
    primary = OraColors.OraOrange,
    onPrimary = Color.White,
    primaryContainer = OraColors.OraOrangeLight,
    onPrimaryContainer = OraColors.OnSurface,
    secondary = OraColors.YogaGreen,
    onSecondary = Color.White,
    background = OraColors.Background,
    onBackground = OraColors.OnSurface,
    surface = OraColors.Surface,
    onSurface = OraColors.OnSurface,
    surfaceVariant = OraColors.OraBeige,
    onSurfaceVariant = OraColors.OnSurfaceVariant
)

@Composable
fun OraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OraLightColorScheme,
        typography = OraTypography,
        shapes = OraShapes,
        content = content
    )
}