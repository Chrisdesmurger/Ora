package com.ora.wellbeing.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.domain.model.ContentCategory
import com.ora.wellbeing.ui.components.CategoryButton
import com.ora.wellbeing.ui.components.OraLogo
import com.ora.wellbeing.ui.components.VideoCard
import com.ora.wellbeing.ui.theme.OraColors

@Composable
fun HomeScreen(
    onNavigateToCategory: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OraColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header avec logo ORA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OraLogo(size = 50)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section "Your suggestion of the day"
        Text(
            text = "Your suggestion of the day",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OraColors.TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vidéo featured - Morning Yoga Flow
        uiState.featuredVideo?.let { video ->
            VideoCard(
                video = video,
                isLarge = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                onClick = {
                    viewModel.onEvent(HomeUiEvent.FeaturedVideoClicked)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Boutons catégories
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(uiState.categories) { category ->
                val categoryColor = when (category) {
                    ContentCategory.YOGA -> OraColors.YogaGreen
                    ContentCategory.PILATES -> OraColors.SoftPink
                    ContentCategory.MEDITATION -> OraColors.MeditationPurple
                    ContentCategory.BREATHING -> OraColors.BreathingBlue
                }

                CategoryButton(
                    title = category.displayName,
                    color = categoryColor,
                    onClick = {
                        onNavigateToCategory(category.name.lowercase())
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton "Get Started"
        Button(
            onClick = {
                viewModel.onEvent(HomeUiEvent.GetStartedClicked)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = OraColors.Primary
            ),
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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