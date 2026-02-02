package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Avatar circulaire de l'utilisateur avec photo ou initiale
 * - Si photoUrl est fournie : affiche l'image
 * - Sinon : affiche un cercle coloré avec la première lettre du firstName
 *
 * @param firstName Prénom de l'utilisateur (pour l'initiale)
 * @param photoUrl URL de la photo de profil (optionnel)
 * @param size Taille du cercle en Dp
 * @param fontSize Taille du texte pour l'initiale
 * @param backgroundColor Couleur de fond du cercle (par défaut: tertiary)
 */
@Composable
fun UserAvatar(
    firstName: String,
    photoUrl: String? = null,
    size: Dp = 140.dp,
    fontSize: TextUnit = 60.sp,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            // Afficher la photo si disponible
            AsyncImage(
                model = photoUrl,
                contentDescription = "Photo de profil",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Afficher l'initiale si pas de photo
            val initial = firstName.firstOrNull()?.uppercase() ?: "?"
            Text(
                text = initial,
                style = MaterialTheme.typography.displayLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize
                )
            )
        }
    }
}
