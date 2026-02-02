package com.ora.wellbeing.presentation.screens.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.presentation.components.ContentCard

/**
 * NEW (Issue #33): Écran de détail pour une catégorie de besoin quotidien
 * Affiche tous les types de contenu (Yoga, Méditation, Pilates, Auto-massage)
 * filtrés par les need_tags de la catégorie sélectionnée
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyNeedDetailScreen(
    categoryId: String,
    onNavigateBack: () -> Unit,
    onNavigateToContent: (String) -> Unit,
    viewModel: DailyNeedDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(categoryId) {
        viewModel.onEvent(DailyNeedDetailUiEvent.LoadCategory(categoryId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.categoryName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Erreur inconnue",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .padding(16.dp)
                    )
                }
                uiState.filteredContent.isEmpty() -> {
                    Text(
                        text = "Aucune pratique disponible pour le moment",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            items = uiState.filteredContent,
                            key = { it.id }
                        ) { content ->
                            ContentCard(
                                content = content,
                                onClick = { onNavigateToContent(content.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
