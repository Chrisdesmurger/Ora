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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ora.wellbeing.domain.model.ContentCategory
import com.ora.wellbeing.domain.model.VideoContent
import com.ora.wellbeing.ui.components.CategoryButton
import com.ora.wellbeing.ui.components.VideoCard
import com.ora.wellbeing.ui.theme.OraColors

@Composable
fun CategoryDetailScreen(
    category: String,
    onBackClick: () -> Unit
) {
    val contentCategory = try {
        ContentCategory.valueOf(category.uppercase())
    } catch (e: IllegalArgumentException) {
        ContentCategory.YOGA
    }

    var selectedSubFilter by remember { mutableStateOf<String?>(null) }

    // Mock data pour les filtres spécialisés
    val subFilters = when (contentCategory) {
        ContentCategory.MEDITATION -> listOf("Débutant", "Intermédiaire", "Avancé", "Guidée", "Libre")
        ContentCategory.YOGA -> listOf("Hatha", "Vinyasa", "Yin", "Ashtanga", "Restaurateur")
        ContentCategory.PILATES -> listOf("Mat", "Équipement", "Core", "Postural", "Dynamique")
        ContentCategory.BREATHING -> listOf("Relaxation", "Énergisant", "Concentration", "Cohérence", "4-7-8")
    }

    // Mock data pour les vidéos
    val featuredVideo = VideoContent(
        id = "featured_${category}",
        title = when (contentCategory) {
            ContentCategory.MEDITATION -> "Méditation pleine conscience - 20 min"
            ContentCategory.YOGA -> "Vinyasa Flow matinal - 30 min"
            ContentCategory.PILATES -> "Pilates Core Workout - 25 min"
            ContentCategory.BREATHING -> "Respiration 4-7-8 pour dormir - 10 min"
        },
        description = "Séance recommandée pour aujourd'hui",
        thumbnailUrl = "https://example.com/${category}_featured.jpg",
        videoUrl = "https://example.com/${category}_featured.mp4",
        duration = when (contentCategory) {
            ContentCategory.MEDITATION -> "20 min"
            ContentCategory.YOGA -> "30 min"
            ContentCategory.PILATES -> "25 min"
            ContentCategory.BREATHING -> "10 min"
        },
        category = contentCategory,
        isFeatured = true
    )

    val videoList = (1..8).map { index ->
        VideoContent(
            id = "${category}_$index",
            title = "${contentCategory.displayName} séance $index",
            description = "Description de la séance $index",
            thumbnailUrl = "https://example.com/${category}_$index.jpg",
            videoUrl = "https://example.com/${category}_$index.mp4",
            duration = "${(10..45).random()} min",
            category = contentCategory,
            isNew = index <= 2
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OraColors.Background)
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Mes séances de ${contentCategory.displayName.lowercase()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OraColors.TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = OraColors.Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OraColors.Background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Filtres spécialisés
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(subFilters) { filter ->
                    val categoryColor = when (contentCategory) {
                        ContentCategory.YOGA -> OraColors.YogaGreen
                        ContentCategory.PILATES -> OraColors.SoftPink
                        ContentCategory.MEDITATION -> OraColors.MeditationPurple
                        ContentCategory.BREATHING -> OraColors.BreathingBlue
                    }

                    CategoryButton(
                        title = filter,
                        color = categoryColor,
                        isSelected = selectedSubFilter == filter,
                        onClick = {
                            selectedSubFilter = if (selectedSubFilter == filter) null else filter
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vidéo featured
            VideoCard(
                video = featuredVideo,
                isLarge = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                onClick = {
                    // Lancer la vidéo featured
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grille de vidéos secondaires
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(600.dp)
            ) {
                items(videoList) { video ->
                    VideoCard(
                        video = video,
                        onClick = {
                            // Lancer la vidéo
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}