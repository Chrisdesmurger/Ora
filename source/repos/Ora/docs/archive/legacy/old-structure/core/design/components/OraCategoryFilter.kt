package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

data class FilterCategory(
    val name: String,
    val color: Color,
    val id: String = name.lowercase()
)

@Composable
fun OraCategoryFilter(
    categories: List<FilterCategory>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    showAllOption: Boolean = true
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Option "Tous" si demandée
        if (showAllOption) {
            item {
                OraCategoryFilterChip(
                    text = "Tous",
                    isSelected = selectedCategories.isEmpty(),
                    color = OraColors.OnSurfaceVariant,
                    onClick = { onCategoryToggle("all") }
                )
            }
        }

        // Catégories spécifiques
        items(categories) { category ->
            OraCategoryFilterChip(
                text = category.name,
                isSelected = selectedCategories.contains(category.id),
                color = category.color,
                onClick = { onCategoryToggle(category.id) }
            )
        }
    }
}

@Composable
private fun OraCategoryFilterChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .background(
                if (isSelected) {
                    color
                } else {
                    color.copy(alpha = 0.15f)
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) {
                Color.White
            } else {
                color
            },
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun OraSearchAndFilter(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    categories: List<FilterCategory>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barre de recherche
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = {
                Text(
                    text = "Rechercher une séance...",
                    color = OraColors.OnSurfaceVariant
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OraColors.OraOrange,
                unfocusedBorderColor = OraColors.OnSurfaceVariant.copy(alpha = 0.3f),
                focusedTextColor = OraColors.OnSurface,
                unfocusedTextColor = OraColors.OnSurface
            ),
            singleLine = true
        )

        // Filtres par catégorie
        OraCategoryFilter(
            categories = categories,
            selectedCategories = selectedCategories,
            onCategoryToggle = onCategoryToggle
        )
    }
}

// Catégories prédéfinies basées sur les mockups
val DefaultOraCategories = listOf(
    FilterCategory("Yoga", OraColors.YogaGreen),
    FilterCategory("Pilates", OraColors.PilatesOrange),
    FilterCategory("Méditation", OraColors.MeditationPurple),
    FilterCategory("Respiration", OraColors.BreathingBlue)
)

@Preview(showBackground = true)
@Composable
fun OraCategoryFilterPreview() {
    OraTheme {
        var selectedCategories by remember { mutableStateOf(setOf<String>()) }
        var searchQuery by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Version avec recherche
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

            // Version filtres uniquement
            OraCategoryFilter(
                categories = DefaultOraCategories,
                selectedCategories = setOf("yoga"),
                onCategoryToggle = { }
            )
        }
    }
}