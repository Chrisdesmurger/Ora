package com.ora.wellbeing.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object Journal : Screen("journal", "Journal", Icons.Default.MenuBook)
    object Library : Screen("library", "Bibliothèque", Icons.Default.VideoLibrary)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
    object Search : Screen("search", "Recherche", Icons.Default.Search)

    // Écrans de détail
    object JournalDetail : Screen("journal_detail", "Journal Quotidien")
    object CategoryDetail : Screen("category_detail", "Catégorie")
}

val bottomNavigationItems = listOf(
    Screen.Home,
    Screen.Search,
    Screen.Library,
    Screen.Journal,
    Screen.Profile
)