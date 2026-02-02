package com.ora.wellbeing.presentation.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.presentation.components.SubcategoryCard
import com.ora.wellbeing.presentation.theme.TitleOrangeDark

/**
 * ContentCategoryDetailScreen
 *
 * Detail screen for a single category (e.g., "Méditation")
 * Displays:
 * - Category title with back navigation
 * - Horizontal scrollable CARDS for subcategories (managed via OraWebApp)
 * - 2-column grid of content items
 *
 * Subcategories are visual cards that scroll horizontally,
 * managed from the OraWebApp admin portal.
 */
@Composable
fun ContentCategoryDetailScreen(
    onBackClick: () -> Unit,
    onContentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContentCategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val subcategories by viewModel.subcategories.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.categoryName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = TitleOrangeDark
                        )
                        if (uiState.totalContentCount > 0) {
                            Text(
                                text = "${uiState.allContent.size} / ${uiState.totalContentCount} contenus",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Subcategory cards section (horizontal scroll)
                        if (subcategories.isNotEmpty()) {
                            SubcategoriesSection(
                                subcategories = subcategories,
                                selectedSubcategory = selectedSubcategory,
                                onSubcategoryClick = { viewModel.onSubcategorySelect(it) },
                                onClearFilter = { viewModel.clearFilter() }
                            )

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Content grid
                        if (uiState.allContent.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (selectedSubcategory != null) {
                                            stringResource(R.string.library_no_content_filtered)
                                        } else {
                                            stringResource(R.string.library_no_content)
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (selectedSubcategory != null) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        TextButton(onClick = { viewModel.clearFilter() }) {
                                            Text(stringResource(R.string.library_show_all))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.allContent,
                                    key = { it.id }
                                ) { content ->
                                    ContentGridCard(
                                        content = content,
                                        onClick = { onContentClick(content.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Subcategories section with horizontal scrolling cards
 */
@Composable
private fun SubcategoriesSection(
    subcategories: List<SubcategoryItem>,
    selectedSubcategory: SubcategoryItem?,
    onSubcategoryClick: (SubcategoryItem?) -> Unit,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        // Section title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.library_subcategories),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TitleOrangeDark
            )
            if (selectedSubcategory != null) {
                TextButton(
                    onClick = onClearFilter,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.library_show_all),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Horizontal scrolling subcategory cards
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "All" card (first position)
            item {
                AllSubcategoryCard(
                    isSelected = selectedSubcategory == null,
                    onClick = onClearFilter,
                    totalCount = subcategories.sumOf { it.itemCount }
                )
            }

            // Subcategory cards
            items(
                items = subcategories,
                key = { it.id }
            ) { subcategory ->
                SubcategoryCard(
                    subcategory = subcategory,
                    onClick = { onSubcategoryClick(subcategory) },
                    isSelected = selectedSubcategory?.id == subcategory.id
                )
            }
        }
    }
}

/**
 * "All" card shown first in subcategory row
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllSubcategoryCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(140.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Count badge
                if (totalCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "$totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.library_all_content),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Content card for the grid (with image)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentGridCard(
    content: ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {}
            }

            // Duration badge (top left)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = content.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Content info (bottom)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (imageUrl != null) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (content.instructor.isNotEmpty()) {
                    Text(
                        text = content.instructor,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (imageUrl != null) {
                            Color.White.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}
