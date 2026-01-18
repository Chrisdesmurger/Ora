package feature.home.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme
import core.design.components.*

@Composable
fun HomeScreen(
    onNavigateToCategory: (CategoryType) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OraColors.Background),
        contentPadding = PaddingValues(bottom = 100.dp), // Space for bottom nav
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header avec logo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OraLogo(showSubtitle = false)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Bienvenue dans votre espace bien-être",
                    style = MaterialTheme.typography.titleMedium,
                    color = OraColors.OnSurfaceVariant
                )
            }
        }

        item {
            // Suggestion du jour
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Suggestion du jour",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OraSuggestionCard(
                    subtitle = "Méditation matinale douce",
                    duration = "15 minutes",
                    onClick = { onPlayVideo("morning-meditation") }
                )
            }
        }

        item {
            // Catégories
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Choisir une pratique",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OraCategoryGrid(
                    selectedCategory = null,
                    onCategoryClick = onNavigateToCategory
                )
            }
        }

        item {
            // Vidéos populaires
            Column {
                Text(
                    text = "Séances populaires",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OraColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, bottom = 16.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(
                        listOf(
                            Triple("Yoga du matin", "20:15", true),
                            Triple("Respiration profonde", "08:30", false),
                            Triple("Méditation guidée", "12:00", true),
                            Triple("Pilates débutant", "25:45", false)
                        )
                    ) { (title, duration, isNew) ->
                        OraVideoCard(
                            title = title,
                            duration = duration,
                            isNew = isNew,
                            onClick = { onPlayVideo(title) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        item {
            // Sélecteur d'humeur
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                var selectedMood by remember { mutableStateOf<MoodType?>(null) }

                OraMoodSelector(
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it }
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun HomeScreenPreview() {
    OraTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenMobilePreview() {
    OraTheme {
        HomeScreen()
    }
}