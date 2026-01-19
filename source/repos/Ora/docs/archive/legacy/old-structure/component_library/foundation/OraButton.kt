package com.ora.wellbeing.ui.components.foundation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ora.wellbeing.ui.theme.OraTheme
import com.ora.wellbeing.ui.theme.oraColors

/**
 * Variantes de boutons Ora
 */
enum class OraButtonVariant {
    Primary,        // Bouton principal avec couleur de marque
    Secondary,      // Bouton secondaire avec outline
    Text,          // Bouton texte sans background
    Error,         // Bouton pour actions destructrices
    Success        // Bouton pour actions positives
}

/**
 * Tailles de boutons Ora
 */
enum class OraButtonSize {
    Small,         // 32dp height
    Medium,        // 40dp height
    Large          // 48dp height (défaut)
}

/**
 * Bouton principal de l'application Ora
 */
@Composable
fun OraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OraButtonVariant = OraButtonVariant.Primary,
    size: OraButtonSize = OraButtonSize.Large,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    contentDescription: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val colors = getButtonColors(variant)
    val height = getButtonHeight(size)
    val typography = getButtonTypography(size)
    val shape = RoundedCornerShape(12.dp)

    // Animation des couleurs pour les changements d'état
    val animatedContainerColor by animateColorAsState(
        targetValue = if (enabled) colors.containerColor else colors.disabledContainerColor,
        animationSpec = tween(200),
        label = "container_color"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) colors.contentColor else colors.disabledContentColor,
        animationSpec = tween(200),
        label = "content_color"
    )

    when (variant) {
        OraButtonVariant.Primary, OraButtonVariant.Error, OraButtonVariant.Success -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .semantics {
                        contentDescription?.let { this.contentDescription = it }
                        role = Role.Button
                    },
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedContainerColor,
                    contentColor = animatedContentColor,
                    disabledContainerColor = colors.disabledContainerColor,
                    disabledContentColor = colors.disabledContentColor
                ),
                contentPadding = getButtonContentPadding(size),
                interactionSource = interactionSource
            ) {
                OraButtonContent(
                    text = text,
                    icon = icon,
                    iconPosition = iconPosition,
                    loading = loading,
                    typography = typography,
                    contentColor = animatedContentColor
                )
            }
        }

        OraButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .semantics {
                        contentDescription?.let { this.contentDescription = it }
                        role = Role.Button
                    },
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = animatedContentColor,
                    disabledContentColor = colors.disabledContentColor
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (enabled) colors.containerColor else colors.disabledContainerColor
                ),
                contentPadding = getButtonContentPadding(size),
                interactionSource = interactionSource
            ) {
                OraButtonContent(
                    text = text,
                    icon = icon,
                    iconPosition = iconPosition,
                    loading = loading,
                    typography = typography,
                    contentColor = animatedContentColor
                )
            }
        }

        OraButtonVariant.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .semantics {
                        contentDescription?.let { this.contentDescription = it }
                        role = Role.Button
                    },
                enabled = enabled && !loading,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = animatedContentColor,
                    disabledContentColor = colors.disabledContentColor
                ),
                contentPadding = getButtonContentPadding(size),
                interactionSource = interactionSource
            ) {
                OraButtonContent(
                    text = text,
                    icon = icon,
                    iconPosition = iconPosition,
                    loading = loading,
                    typography = typography,
                    contentColor = animatedContentColor
                )
            }
        }
    }
}

/**
 * Position de l'icône dans le bouton
 */
enum class IconPosition {
    Start, End
}

/**
 * Contenu interne du bouton avec gestion de l'icône et du loading
 */
@Composable
private fun OraButtonContent(
    text: String,
    icon: ImageVector?,
    iconPosition: IconPosition,
    loading: Boolean,
    typography: TextStyle,
    contentColor: Color
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = contentColor,
            strokeWidth = 2.dp
        )
    } else {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null && iconPosition == IconPosition.Start) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                style = typography,
                color = contentColor
            )

            if (icon != null && iconPosition == IconPosition.End) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Structure pour les couleurs de bouton
 */
data class OraButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

/**
 * Obtient les couleurs selon la variante
 */
@Composable
private fun getButtonColors(variant: OraButtonVariant): OraButtonColors {
    val theme = MaterialTheme.colorScheme
    val oraColors = MaterialTheme.oraColors

    return when (variant) {
        OraButtonVariant.Primary -> OraButtonColors(
            containerColor = theme.primary,
            contentColor = theme.onPrimary,
            disabledContainerColor = theme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = theme.onSurface.copy(alpha = 0.38f)
        )

        OraButtonVariant.Secondary -> OraButtonColors(
            containerColor = theme.primary,
            contentColor = theme.primary,
            disabledContainerColor = theme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = theme.onSurface.copy(alpha = 0.38f)
        )

        OraButtonVariant.Text -> OraButtonColors(
            containerColor = Color.Transparent,
            contentColor = theme.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = theme.onSurface.copy(alpha = 0.38f)
        )

        OraButtonVariant.Error -> OraButtonColors(
            containerColor = theme.error,
            contentColor = theme.onError,
            disabledContainerColor = theme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = theme.onSurface.copy(alpha = 0.38f)
        )

        OraButtonVariant.Success -> OraButtonColors(
            containerColor = oraColors.successGreen,
            contentColor = theme.onPrimary,
            disabledContainerColor = theme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = theme.onSurface.copy(alpha = 0.38f)
        )
    }
}

/**
 * Obtient la hauteur selon la taille
 */
private fun getButtonHeight(size: OraButtonSize): androidx.compose.ui.unit.Dp {
    return when (size) {
        OraButtonSize.Small -> 32.dp
        OraButtonSize.Medium -> 40.dp
        OraButtonSize.Large -> 48.dp
    }
}

/**
 * Obtient la typographie selon la taille
 */
@Composable
private fun getButtonTypography(size: OraButtonSize): TextStyle {
    return when (size) {
        OraButtonSize.Small -> MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Medium
        )
        OraButtonSize.Medium -> MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Medium
        )
        OraButtonSize.Large -> MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Obtient le padding du contenu selon la taille
 */
private fun getButtonContentPadding(size: OraButtonSize): PaddingValues {
    return when (size) {
        OraButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        OraButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        OraButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun OraButtonPreview() {
    OraTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OraButton(
                text = "Commencer maintenant",
                onClick = { },
                variant = OraButtonVariant.Primary
            )

            OraButton(
                text = "Voir plus",
                onClick = { },
                variant = OraButtonVariant.Secondary
            )

            OraButton(
                text = "Passer",
                onClick = { },
                variant = OraButtonVariant.Text
            )

            OraButton(
                text = "Supprimer",
                onClick = { },
                variant = OraButtonVariant.Error
            )

            OraButton(
                text = "Terminé",
                onClick = { },
                variant = OraButtonVariant.Success
            )

            OraButton(
                text = "Chargement...",
                onClick = { },
                loading = true
            )

            OraButton(
                text = "Désactivé",
                onClick = { },
                enabled = false
            )
        }
    }
}