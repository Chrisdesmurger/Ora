package feature.library.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme
import core.design.components.*

data class VideoItem(
    val id: String,
    val title: String,
    val duration: String,
    val category: CategoryType,
    val isNew: Boolean = false
)

@Composable
fun LibraryScreen(
    onPlayVideo: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }

    // Données d'exemple
    val allVideos = remember {
        listOf(
            VideoItem("1", "Yoga matinal énergisant", "25:30", CategoryType.YOGA, true),
            VideoItem("2", "Pilates pour débutants", "18:45", CategoryType.PILATES),
            VideoItem("3", "Méditation de pleine conscience", "12:00", CategoryType.MEDITATION, true),
            VideoItem("4", "Respiration anti-stress", "08:20", CategoryType.BREATHING),
            VideoItem("5", "Yoga vinyasa flow", "35:15", CategoryType.YOGA),
            VideoItem("6", "Pilates avancé", "28:30", CategoryType.PILATES),
            VideoItem("7", "Méditation du soir", "15:45", CategoryType.MEDITATION),
            VideoItem("8", "Technique de respiration 4-7-8", "06:30", CategoryType.BREATHING, true),
            VideoItem("9", "Yoga restauratif", "42:00", CategoryType.YOGA),
            VideoItem("10", "Pilates core workout", "22:15", CategoryType.PILATES)
        )
    }

    // Filtrage des vidéos
    val filteredVideos = remember(searchQuery, selectedCategories) {
        allVideos.filter { video ->
            val matchesSearch = searchQuery.isEmpty() ||
                    video.title.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategories.isEmpty() ||
                    selectedCategories.contains(video.category.name.lowercase())

            matchesSearch && matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OraColors.Background),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Bibliothèque",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${filteredVideos.size} séances disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = OraColors.OnSurfaceVariant
                )
            }
        }

        item {
            // Recherche et filtres
            OraSearchAndFilter(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                categories = DefaultOraCategories,
                selectedCategories = selectedCategories,
                onCategoryToggle = { categoryId ->
                    selectedCategories = if (categoryId == "all") {
                        emptySet()
                    } else {
                        if (selectedCategories.contains(categoryId)) {
                            selectedCategories - categoryId
                        } else {
                            selectedCategories + categoryId
                        }
                    }
                }
            )
        }

        item {
            // Grille de vidéos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp) // Hauteur fixe pour éviter les problèmes de nested scrolling
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredVideos) { video ->
                    OraVideoCard(
                        title = video.title,
                        duration = video.duration,
                        isNew = video.isNew,
                        onClick = { onPlayVideo(video) },
                        modifier = Modifier.height(160.dp)
                    )
                }
            }
        }

        // Message si aucun résultat
        if (filteredVideos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OraColors.OraBeige
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Aucune séance trouvée",
                            style = MaterialTheme.typography.titleMedium,
                            color = OraColors.OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Essayez de modifier vos critères de recherche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OraColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun LibraryScreenPreview() {
    OraTheme {
        LibraryScreen()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LibraryScreenMobilePreview() {
    OraTheme {
        LibraryScreen()
    }
}