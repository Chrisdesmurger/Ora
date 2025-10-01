package com.ora.wellbeing.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.domain.model.ContentCategory
import com.ora.wellbeing.ui.components.CategoryButton
import com.ora.wellbeing.ui.components.VideoCard
import com.ora.wellbeing.ui.theme.OraColors

@Composable
fun LibraryScreen(
    onNavigateToCategory: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OraColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Filtres en haut
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ContentCategory.values()) { category ->
                val categoryColor = when (category) {
                    ContentCategory.YOGA -> OraColors.YogaGreen
                    ContentCategory.PILATES -> OraColors.SoftPink
                    ContentCategory.MEDITATION -> OraColors.MeditationPurple
                    ContentCategory.BREATHING -> OraColors.BreathingBlue
                }

                CategoryButton(
                    title = category.displayName,
                    color = categoryColor,
                    isSelected = uiState.selectedFilter == category,
                    onClick = {
                        viewModel.onEvent(
                            LibraryUiEvent.FilterByCategory(
                                if (uiState.selectedFilter == category) null else category
                            )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Titre "Pratiques"
        Text(
            text = "Pratiques",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = OraColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section Méditation
        if (uiState.selectedFilter == null || uiState.selectedFilter == ContentCategory.MEDITATION) {
            Text(
                text = "Méditation",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(uiState.meditationVideos.take(4)) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            viewModel.onEvent(LibraryUiEvent.VideoClicked(video))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Section Yoga
        if (uiState.selectedFilter == null || uiState.selectedFilter == ContentCategory.YOGA) {
            Text(
                text = "Yoga",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(uiState.yogaVideos.take(4)) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            viewModel.onEvent(LibraryUiEvent.VideoClicked(video))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Section Pilates (si activée)
        if (uiState.selectedFilter == ContentCategory.PILATES) {
            Text(
                text = "Pilates",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder pour les vidéos Pilates
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vidéos Pilates à venir",
                    color = OraColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Section Respiration (si activée)
        if (uiState.selectedFilter == ContentCategory.BREATHING) {
            Text(
                text = "Respiration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OraColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Placeholder pour les vidéos de respiration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Exercices de respiration à venir",
                    color = OraColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Gestion du loading
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OraColors.Primary)
        }
    }

    // Gestion des erreurs
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Afficher un snackbar ou toast avec l'erreur
        }
    }
}