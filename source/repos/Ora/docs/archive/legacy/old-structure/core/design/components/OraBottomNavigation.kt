package core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import core.design.OraColors
import core.design.OraTheme

enum class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    JOURNAL("Journal", Icons.Default.MenuBook, Icons.Filled.MenuBook),
    BEAUTY("Beauty Tips", Icons.Default.Favorite, Icons.Filled.Favorite),
    HOME("Home", Icons.Default.Home, Icons.Filled.Home),
    ADVICE("Advice", Icons.Default.Lightbulb, Icons.Filled.Lightbulb),
    PROFILE("Profile", Icons.Default.Person, Icons.Filled.Person)
}

@Composable
fun OraBottomNavigation(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = OraColors.BottomNavPink
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            BottomNavItem.values().forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedItem == item) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OraColors.BottomNavSelected,
                        selectedTextColor = OraColors.BottomNavSelected,
                        unselectedIconColor = OraColors.BottomNavUnselected,
                        unselectedTextColor = OraColors.BottomNavUnselected,
                        indicatorColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OraBottomNavigationPreview() {
    OraTheme {
        var selectedItem by remember { mutableStateOf(BottomNavItem.HOME) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OraColors.Background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                OraBottomNavigation(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        }
    }
}