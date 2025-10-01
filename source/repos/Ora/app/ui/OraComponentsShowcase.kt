package app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

/**
 * Écran de démonstration de tous les composants Ora UI
 * Utilisé pour les tests visuels et la documentation
 */
@Composable
fun OraComponentsShowcase(
    modifier: Modifier = Modifier
) {
    var selectedBottomNavItem by remember { mutableStateOf(BottomNavItem.HOME) }
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }
    var selectedCategory by remember { mutableStateOf<CategoryType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }
    var journalText by remember { mutableStateOf("") }
    var gratitudeItems by remember {
        mutableStateOf(
            listOf(
                GratitudeItem("Ma famille qui me soutient", true, GratitudeCardColor.PINK),
                GratitudeItem("", false, GratitudeCardColor.ORANGE),
                GratitudeItem("Le soleil ce matin", false, GratitudeCardColor.GREEN)
            )
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ora UI Components",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OraColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tous les composants de l'application Ora",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OraColors.OnSurfaceVariant
                    )
                }
            }

            item {
                ShowcaseSection(title = "Logo") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OraLogo(showSubtitle = true)
                        OraLogo(showSubtitle = false)
                    }
                }
            }

            item {
                ShowcaseSection(title = "Boutons Catégorie") {
                    OraCategoryGrid(
                        selectedCategory = selectedCategory,
                        onCategoryClick = { selectedCategory = it }
                    )
                }
            }

            item {
                ShowcaseSection(title = "Cards Vidéo") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OraVideoCard(
                            title = "Yoga matinal énergisant",
                            duration = "25:30",
                            isNew = true,
                            onClick = {}
                        )

                        OraSuggestionCard(
                            subtitle = "Méditation de pleine conscience",
                            duration = "15 minutes",
                            onClick = {}
                        )
                    }
                }
            }

            item {
                ShowcaseSection(title = "Sélecteur d'Humeur") {
                    OraMoodSelector(
                        selectedMood = selectedMood,
                        onMoodSelected = { selectedMood = it }
                    )
                }
            }

            item {
                ShowcaseSection(title = "Cards Gratitude") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        gratitudeItems.forEachIndexed { index, item ->
                            OraGratitudeCard(
                                text = item.text,
                                onTextChange = { newText ->
                                    gratitudeItems = gratitudeItems.toMutableList().apply {
                                        this[index] = item.copy(text = newText)
                                    }
                                },
                                isChecked = item.isChecked,
                                onCheckedChange = { checked ->
                                    gratitudeItems = gratitudeItems.toMutableList().apply {
                                        this[index] = item.copy(isChecked = checked)
                                    }
                                },
                                cardColor = item.color
                            )
                        }
                    }
                }
            }

            item {
                ShowcaseSection(title = "Filtres de Catégorie") {
                    OraSearchAndFilter(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        categories = DefaultOraCategories,
                        selectedCategories = selectedFilters,
                        onCategoryToggle = { categoryId ->
                            selectedFilters = if (categoryId == "all") {
                                emptySet()
                            } else {
                                if (selectedFilters.contains(categoryId)) {
                                    selectedFilters - categoryId
                                } else {
                                    selectedFilters + categoryId
                                }
                            }
                        }
                    )
                }
            }

            item {
                ShowcaseSection(title = "Champs de Texte") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OraTextField(
                            value = journalText,
                            onValueChange = { journalText = it },
                            label = "Journal",
                            placeholder = "Écris tes pensées..."
                        )

                        OraJournalTextField(
                            value = journalText,
                            onValueChange = { journalText = it },
                            title = "Mes pensées du jour"
                        )
                    }
                }
            }

            item {
                ShowcaseSection(title = "Tracker d'Habitudes") {
                    OraHabitTracker(
                        habitName = "Méditation quotidienne",
                        habitDays = (1..28).map { dayNumber ->
                            HabitDay(
                                dayOfWeek = when (dayNumber % 7) {
                                    1 -> "L"; 2 -> "M"; 3 -> "M"; 4 -> "J"
                                    5 -> "V"; 6 -> "S"; 0 -> "D"; else -> "L"
                                },
                                dayNumber = dayNumber,
                                isCompleted = kotlin.random.Random.nextBoolean(),
                                completionColor = listOf(
                                    OraColors.YogaGreen,
                                    OraColors.PilatesOrange,
                                    OraColors.MeditationPurple,
                                    OraColors.BreathingBlue
                                ).random()
                            )
                        },
                        onDayClick = {}
                    )
                }
            }
        }

        // Bottom Navigation
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            OraBottomNavigation(
                selectedItem = selectedBottomNavItem,
                onItemSelected = { selectedBottomNavItem = it }
            )
        }
    }
}

@Composable
private fun ShowcaseSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = OraColors.OnSurface,
            fontWeight = FontWeight.SemiBold
        )

        content()
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun OraComponentsShowcasePreview() {
    OraTheme {
        OraComponentsShowcase()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun OraComponentsShowcaseMobilePreview() {
    OraTheme {
        OraComponentsShowcase()
    }
}