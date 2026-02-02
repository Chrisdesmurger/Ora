package com.ora.wellbeing.presentation.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ora.wellbeing.R
import com.ora.wellbeing.data.cache.SubcategorySection
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.presentation.theme.TitleOrangeDark

/**
 * Get localized category name based on category ID
 * Maps category IDs to string resources for i18n support
 */
@Composable
private fun getLocalizedCategoryName(categoryId: String): String {
    return when (categoryId.lowercase()) {
        "meditation", "méditation" -> stringResource(R.string.category_meditation)
        "yoga" -> stringResource(R.string.category_yoga)
        "pilates" -> stringResource(R.string.category_pilates)
        "bien-etre", "bien-être", "wellness" -> stringResource(R.string.category_wellness)
        "respiration", "breathing" -> stringResource(R.string.category_breathing)
        "sommeil", "sleep" -> stringResource(R.string.category_sleep)
        "massage" -> stringResource(R.string.category_massage)
        else -> categoryId // Fallback to raw ID if no mapping found
    }
}

/**
 * ContentCategoryDetailScreen
 *
 * Detail screen for a single category (e.g., "Yoga", "Méditation")
 *
 * Layout:
 * - Category title with back navigation
 * - Vertical scrolling list of subcategory SECTIONS
 * - Each section has a title (subcategory name) and horizontal scrolling content cards
 *
 * Content cards feature:
 * - Background image with gradient overlay
 * - Duration badge (top left)
 * - Play button (center)
 * - Title (bottom)
 *
 * Subcategories are managed from the OraWebApp admin portal.
 */
@Composable
fun ContentCategoryDetailScreen(
    onBackClick: () -> Unit,
    onContentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContentCategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupedContent by viewModel.groupedContent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = getLocalizedCategoryName(uiState.categoryName),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TitleOrangeDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigation_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.error_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: stringResource(R.string.error_generic),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                groupedContent.isEmpty() && !uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.library_no_content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Vertical list of subcategory sections
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = groupedContent,
                            key = { it.subcategory.id }
                        ) { section ->
                            SubcategorySectionRow(
                                section = section,
                                onContentClick = onContentClick
                            )
                        }

                        // Bottom spacing
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * A subcategory section with title and horizontal scrolling content
 */
@Composable
private fun SubcategorySectionRow(
    section: SubcategorySection,
    onContentClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section title (subcategory name)
        Text(
            text = section.subcategory.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TitleOrangeDark,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Horizontal scrolling content cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = section.content,
                key = { it.id }
            ) { content ->
                ContentHorizontalCard(
                    content = content,
                    onClick = { onContentClick(content.id) }
                )
            }
        }
    }
}

/**
 * Content card for horizontal scroll display
 *
 * Design:
 * - Background image with gradient overlay
 * - Duration badge (top left)
 * - Play button (center)
 * - Title (bottom left)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentHorizontalCard(
    content: ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(180.dp)
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            val imageUrl = content.previewImageUrl?.takeIf { it.isNotBlank() }
                ?: content.thumbnailUrl?.takeIf { it.isNotBlank() }

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
                                startY = 100f
                            )
                        )
                )
            } else {
                // Fallback background
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }

            // Duration badge (top left)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = content.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Play button (center)
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
                shape = CircleShape,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.action_play),
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Title (bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (content.instructor.isNotEmpty()) {
                    Text(
                        text = content.instructor,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
