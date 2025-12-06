package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ora.wellbeing.data.model.CategoryItem

/**
 * CategoryCard - Large horizontal card for library categories
 *
 * Displays a category with:
 * - Full-width background (image or color)
 * - Category name overlay with dark gradient for readability
 * - Ripple effect on click
 *
 * Design inspired by "alo moves" app reference
 *
 * @param category The category to display
 * @param onClick Callback when card is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background: Image (from drawable or URL) or solid color
            when {
                category.iconResId != null -> {
                    // Image background from local drawable resource
                    AsyncImage(
                        model = category.iconResId,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                category.imageUrl != null -> {
                    // Image background from URL (fallback)
                    AsyncImage(
                        model = category.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // Solid color background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(category.color)
                    )
                }
            }

            // Category name text
            Text(
                text = category.name.uppercase(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(20.dp)
            )

            // Optional item count badge
            if (category.itemCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "${category.itemCount} contenus",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
