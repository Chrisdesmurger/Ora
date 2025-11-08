package com.ora.wellbeing.presentation.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToContent: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToFilters: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(LibraryUiEvent.LoadLibraryData)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()  // Respecter la zone de la barre de statut
            .padding(horizontal = 12.dp)  // Réduire le padding horizontal pour maximiser l'espace
    ) {
        // Header avec recherche et filtres
        LibraryHeader(
            onSearchClick = onNavigateToSearch,
            onFiltersClick = onNavigateToFilters
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenu de la bibliothèque
        LibraryContent(
            uiState = uiState,
            onContentClick = onNavigateToContent,
            onCategoryClick = { category ->
                viewModel.onEvent(LibraryUiEvent.FilterByCategory(category))
            },
            onDurationFilterClick = { duration ->
                viewModel.onEvent(LibraryUiEvent.FilterByDuration(duration))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryHeader(
    onSearchClick: () -> Unit,
    onFiltersClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Barre de recherche factice (cliquable)
        Card(
            onClick = onSearchClick,
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rechercher",
                    tint = Color(0xFF6B4E3D) // Marron pour cohérence
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Rechercher du contenu...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B4E3D).copy(alpha = 0.6f)
                )
            }
        }

        // Bouton filtres
        FilledTonalIconButton(
            onClick = onFiltersClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF6B4E3D)
            )
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtres"
            )
        }
    }
}

@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    onContentClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onDurationFilterClick: (String) -> Unit
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = CategoryMeditationLavender)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Filtres rapides par catégorie
        item {
            CategoriesSection(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = onCategoryClick
            )
        }

        // Filtres par durée
        item {
            DurationFiltersSection(
                selectedDuration = uiState.selectedDuration,
                onDurationClick = onDurationFilterClick
            )
        }

        // Contenu populaire
        if (uiState.popularContent.isNotEmpty()) {
            item {
                ContentSection(
                    title = "Populaire",
                    content = uiState.popularContent,
                    onContentClick = onContentClick
                )
            }
        }

        // Nouveau contenu
        if (uiState.newContent.isNotEmpty()) {
            item {
                ContentSection(
                    title = "Nouveautés",
                    content = uiState.newContent,
                    onContentClick = onContentClick,
                    showNewBadge = true
                )
            }
        }

        // Contenu filtré
        if (uiState.filteredContent.isNotEmpty()) {
            item {
                ContentSection(
                    title = when {
                        uiState.selectedCategory != null -> "Contenu ${uiState.selectedCategory}"
                        uiState.selectedDuration != null -> "Sessions ${uiState.selectedDuration}"
                        else -> "Tout le contenu"
                    },
                    content = uiState.filteredContent,
                    onContentClick = onContentClick,
                    isGrid = true
                )
            }
        }
    }
}

@Composable
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "yoga" -> CategoryYogaGreen
        "pilates" -> CategoryPilatesPeach
        "respiration", "breathing" -> CategoryBreathingBlue
        "méditation", "meditation" -> CategoryMeditationLavender
        else -> Color(0xFFB4A5D4) // Violet par défaut
    }
}

@Composable
private fun CategoriesSection(
    categories: List<String>,
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Catégories",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B4E3D), // Marron foncé
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bouton "Tout"
            item {
                FilterChip(
                    onClick = { onCategoryClick("") },
                    label = { Text("Tout") },
                    selected = selectedCategory == null || selectedCategory.isEmpty(),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = CategoryMeditationLavender,
                        labelColor = Color(0xFF6B4E3D),
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (selectedCategory == null || selectedCategory.isEmpty()) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }

            items(categories) { category ->
                val categoryColor = getCategoryColor(category)
                FilterChip(
                    onClick = { onCategoryClick(category) },
                    label = { Text(category) },
                    selected = selectedCategory == category,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = categoryColor.copy(alpha = 0.3f),
                        selectedContainerColor = categoryColor,
                        labelColor = Color(0xFF6B4E3D),
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun DurationFiltersSection(
    selectedDuration: String?,
    onDurationClick: (String) -> Unit
) {
    val durations = listOf("5-10 min", "10-20 min", "20+ min")

    Column {
        Text(
            text = "Durée",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6B4E3D),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bouton "Toutes"
            item {
                FilterChip(
                    onClick = { onDurationClick("") },
                    label = { Text("Toutes") },
                    selected = selectedDuration == null || selectedDuration.isEmpty(),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = CategoryMeditationLavender,
                        labelColor = Color(0xFF6B4E3D),
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (selectedDuration == null || selectedDuration.isEmpty()) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }

            items(durations) { duration ->
                FilterChip(
                    onClick = { onDurationClick(duration) },
                    label = { Text(duration) },
                    selected = selectedDuration == duration,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = CategoryBreathingBlue,
                        labelColor = Color(0xFF6B4E3D),
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (selectedDuration == duration) {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ContentSection(
    title: String,
    content: List<LibraryUiState.ContentItem>,
    onContentClick: (String) -> Unit,
    isGrid: Boolean = false,
    showNewBadge: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF6B4E3D), // Marron foncé
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isGrid) {
            // Affichage en grille pour le contenu filtré
            content.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        ContentCard(
                            content = item,
                            onClick = { onContentClick(item.id) },
                            modifier = Modifier.weight(1f),
                            showNewBadge = showNewBadge
                        )
                    }
                    // Remplir l'espace si la ligne n'est pas complète
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowItems != content.chunked(2).last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Affichage horizontal pour les sections populaire et nouveautés
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(content) { item ->
                    ContentCard(
                        content = item,
                        onClick = { onContentClick(item.id) },
                        modifier = Modifier.width(180.dp),
                        showNewBadge = showNewBadge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentCard(
    content: LibraryUiState.ContentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showNewBadge: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Badge NOUVEAU si nécessaire
                if (showNewBadge) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = "NOUVEAU",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Badge catégorie
                Surface(
                    color = getCategoryColor(content.category).copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = content.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF6B4E3D),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6B4E3D)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF6B4E3D).copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = content.duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B4E3D).copy(alpha = 0.6f)
                    )
                }

                if (content.instructor.isNotEmpty()) {
                    Text(
                        text = "avec ${content.instructor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B4E3D).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibraryScreenPreview() {
    OraTheme {
        LibraryScreen(
            onNavigateToContent = {},
            onNavigateToSearch = {},
            onNavigateToFilters = {}
        )
    }
}