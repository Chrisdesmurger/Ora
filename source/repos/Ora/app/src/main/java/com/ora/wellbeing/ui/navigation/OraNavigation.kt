package com.ora.wellbeing.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ora.wellbeing.ui.screens.home.HomeScreen
import com.ora.wellbeing.ui.screens.journal.JournalScreen
import com.ora.wellbeing.ui.screens.journal.JournalDetailScreen
import com.ora.wellbeing.ui.screens.library.LibraryScreen
import com.ora.wellbeing.ui.screens.library.CategoryDetailScreen
import com.ora.wellbeing.ui.navigation.components.OraBottomNavigation

@Composable
fun OraNavigation(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            OraBottomNavigation(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCategory = { category ->
                        navController.navigate("${Screen.CategoryDetail.route}/$category")
                    }
                )
            }

            composable(Screen.Journal.route) {
                JournalScreen(
                    onNavigateToDetail = {
                        navController.navigate(Screen.JournalDetail.route)
                    }
                )
            }

            composable(Screen.JournalDetail.route) {
                JournalDetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onNavigateToCategory = { category ->
                        navController.navigate("${Screen.CategoryDetail.route}/$category")
                    }
                )
            }

            composable("${Screen.CategoryDetail.route}/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                CategoryDetailScreen(
                    category = category,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                // TODO: Implémenter l'écran profil
            }

            composable(Screen.Search.route) {
                // TODO: Implémenter l'écran recherche
            }
        }
    }
}