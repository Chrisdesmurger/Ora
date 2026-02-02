package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

enum class CategoryType(
    val displayName: String,
    val color: Color,
    val icon: ImageVector
) {
    YOGA("Yoga", OraColors.YogaGreen, Icons.Default.SelfImprovement),
    PILATES("Pilates", OraColors.PilatesOrange, Icons.Default.SelfImprovement),
    MEDITATION("Méditation", OraColors.MeditationPurple, Icons.Default.SelfImprovement),
    BREATHING("Respiration", OraColors.BreathingBlue, Icons.Default.SelfImprovement)
}

@Composable
fun OraCategoryButton(
    category: CategoryType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) category.color else category.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icône de personnage en méditation/yoga
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) Color.White else category.color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nom de la catégorie
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isSelected) Color.White else category.color
            )
        }
    }
}

@Composable
fun OraCategoryGrid(
    selectedCategory: CategoryType? = null,
    onCategoryClick: (CategoryType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(CategoryType.values().size) { index ->
            val category = CategoryType.values()[index]
            OraCategoryButton(
                category = category,
                onClick = { onCategoryClick(category) },
                isSelected = selectedCategory == category
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraCategoryButtonPreview() {
    OraTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OraCategoryButton(
                    category = CategoryType.YOGA,
                    onClick = {},
                    isSelected = false
                )
                OraCategoryButton(
                    category = CategoryType.PILATES,
                    onClick = {},
                    isSelected = true
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OraCategoryButton(
                    category = CategoryType.MEDITATION,
                    onClick = {},
                    isSelected = false
                )
                OraCategoryButton(
                    category = CategoryType.BREATHING,
                    onClick = {},
                    isSelected = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraCategoryGridPreview() {
    OraTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
                .padding(16.dp)
        ) {
            OraCategoryGrid(
                selectedCategory = CategoryType.YOGA,
                onCategoryClick = {}
            )
        }
    }
}