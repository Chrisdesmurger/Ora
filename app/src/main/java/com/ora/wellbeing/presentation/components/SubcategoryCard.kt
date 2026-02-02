package com.ora.wellbeing.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.presentation.theme.OraTheme

/**
 * SubcategoryCard - Visual card component for subcategories
 *
 * Displays a subcategory as a horizontal scrollable card with:
 * - Background image or color
 * - Gradient overlay for text readability
 * - Name and optional item count
 *
 * Design: Compact card suitable for horizontal LazyRow
 * Size: ~140dp width x 100dp height
 *
 * @param subcategory The subcategory data
 * @param onClick Callback when card is clicked
 * @param isSelected Whether this subcategory is currently selected
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryCard(
    subcategory: SubcategoryItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = subcategory.color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp,
            pressedElevation = 4.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image if available
            val imageUrl = subcategory.imageUrl ?: subcategory.iconUrl
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Item count badge (top right)
                if (subcategory.itemCount > 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (imageUrl != null) {
                                Color.White.copy(alpha = 0.9f)
                            } else {
                                Color.Black.copy(alpha = 0.1f)
                            }
                        ) {
                            Text(
                                text = "${subcategory.itemCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (imageUrl != null) {
                                    Color.Black.copy(alpha = 0.7f)
                                } else {
                                    Color.Black.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Name (bottom)
                Text(
                    text = subcategory.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (imageUrl != null) Color.White else Color.Black.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                )
            }
        }
    }
}

/**
 * Compact version for smaller spaces
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryCardCompact(
    subcategory: SubcategoryItem,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = subcategory.color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            val imageUrl = subcategory.imageUrl ?: subcategory.iconUrl
            if (!imageUrl.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }

            // Name centered
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = subcategory.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (imageUrl != null) Color.White else Color.Black.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubcategoryCardPreview() {
    OraTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            SubcategoryCard(
                subcategory = SubcategoryItem(
                    id = "guided",
                    parentCategory = "Meditation",
                    name = "Méditation guidée",
                    color = Color(0xFFD4C4E8),
                    itemCount = 12
                ),
                onClick = {},
                isSelected = false
            )
            SubcategoryCard(
                subcategory = SubcategoryItem(
                    id = "sleep",
                    parentCategory = "Meditation",
                    name = "Sommeil",
                    color = Color(0xFFC4D8E8),
                    itemCount = 8
                ),
                onClick = {},
                isSelected = true
            )
        }
    }
}
