package com.ora.wellbeing.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ora.wellbeing.presentation.components.SubcategoryChip
import com.ora.wellbeing.presentation.components.ContentCard

/**
 * ContentCategoryDetailFilteredScreen
 *
 * Detail screen for a category with optional duration filtering
 * Used by Quick Sessions to show only short content (< 10 minutes)
 *
 * Displays:
 * - Category title with duration indicator
 * - Horizontal scrollable filter chips (subcategories/tags)
 * - 2-column grid of filtered content items
 *
 * @param maxDurationMinutes Optional max duration filter (e.g., 10 for quick sessions)
 * @param onBackClick Callback when back button is pressed
 * @param onContentClick Callback when a content item is clicked
 */
@Composable
fun ContentCategoryDetailFilteredScreen(
    maxDurationMinutes: Int? = null,
    onBackClick: () -> Unit,
    onContentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContentCategoryDetailFilteredViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedSubcategory by viewModel.selectedSubcategory.collectAsState()
    val availableSubcategories by viewModel.availableSubcategories.collectAsState()

    // Set the duration filter from navigation parameter
    LaunchedEffect(maxDurationMinutes) {
        viewModel.setMaxDurationFilter(maxDurationMinutes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.categoryName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        // Show duration filter indicator
                        if (uiState.maxDurationFilter != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Moins de ${uiState.maxDurationFilter} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour"
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
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erreur",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Une erreur est survenue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Subcategory filter chips
                        if (availableSubcategories.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "Tous" chip (clear filter)
                                item {
                                    SubcategoryChip(
                                        label = "Tous",
                                        isSelected = selectedSubcategory == null,
                                        onClick = { viewModel.clearFilter() }
                                    )
                                }

                                // Subcategory chips from tags
                                items(
                                    items = availableSubcategories,
                                    key = { it }
                                ) { subcategory ->
                                    SubcategoryChip(
                                        label = subcategory,
                                        isSelected = selectedSubcategory == subcategory,
                                        onClick = { viewModel.onSubcategoryClick(subcategory) }
                                    )
                                }
                            }

                            Divider(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }

                        // Content grid
                        if (uiState.allContent.isEmpty()) {
                            // Empty state
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
                                        text = if (uiState.maxDurationFilter != null) {
                                            "Aucun contenu court disponible"
                                        } else {
                                            "Aucun contenu disponible"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (uiState.maxDurationFilter != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Essayez la bibliotheque complete",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                    ContentCard(
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
