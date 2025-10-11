package com.ora.wellbeing.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ora.wellbeing.presentation.screens.auth.AuthScreen
import com.ora.wellbeing.presentation.screens.home.HomeScreen
import com.ora.wellbeing.presentation.screens.library.LibraryScreen
import com.ora.wellbeing.presentation.screens.journal.JournalScreen
import com.ora.wellbeing.presentation.screens.programs.ProgramsScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileEditScreen
import com.ora.wellbeing.presentation.screens.stats.PracticeStatsScreen
import com.ora.wellbeing.feature.practice.ui.PracticeDetailScreen

/**
 * FIX(auth): Navigation principale avec vérification d'authentification
 */
@Composable
fun OraNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    authViewModel: OraAuthViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

    // FIX(auth): Redirection vers Auth si non connecté
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && currentDestination?.route != OraDestinations.Auth.route) {
            navController.navigate(OraDestinations.Auth.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Afficher la bottom bar seulement sur les écrans principaux et si authentifié
            if (isAuthenticated && NavigationUtils.isMainDestination(currentDestination?.route)) {
                OraBottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination?.route
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) OraDestinations.Home.route else OraDestinations.Auth.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // FIX(auth): Écran d'authentification
            composable(OraDestinations.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(OraDestinations.Home.route) {
                            popUpTo(OraDestinations.Auth.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Écrans principaux avec bottom navigation
            composable(OraDestinations.Home.route) {
                HomeScreen(
                    onNavigateToContent = { contentId ->
                        // Redirection vers PracticeDetail au lieu de ContentDetail
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    },
                    onNavigateToProgram = { programId ->
                        navController.navigate(OraDestinations.ProgramDetail.createRoute(programId))
                    },
                    onNavigateToQuickSession = { sessionType ->
                        navController.navigate(OraDestinations.QuickSession.createRoute(sessionType.name))
                    }
                )
            }

            composable(OraDestinations.Library.route) {
                LibraryScreen(
                    onNavigateToContent = { contentId ->
                        // Redirection vers PracticeDetail au lieu de ContentDetail
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(OraDestinations.LibrarySearch.route)
                    },
                    onNavigateToFilters = {
                        navController.navigate(OraDestinations.LibraryFilters.route)
                    }
                )
            }

            composable(OraDestinations.Journal.route) {
                JournalScreen(
                    onNavigateToEntry = { date ->
                        navController.navigate(OraDestinations.JournalEntry.createRoute(date))
                    },
                    onNavigateToHistory = {
                        navController.navigate(OraDestinations.JournalHistory.route)
                    }
                )
            }

            composable(OraDestinations.Programs.route) {
                ProgramsScreen(
                    onNavigateToProgram = { programId ->
                        navController.navigate(OraDestinations.ProgramDetail.createRoute(programId))
                    },
                    onNavigateToActiveProgram = { programId ->
                        navController.navigate(OraDestinations.ActiveProgram.createRoute(programId))
                    }
                )
            }

            composable(OraDestinations.Profile.route) {
                ProfileScreen(
                    onNavigateToEditProfile = {
                        navController.navigate(OraDestinations.EditProfile.route)
                    },
                    onNavigateToPracticeStats = { practiceId ->
                        navController.navigate(OraDestinations.PracticeStats.createRoute(practiceId))
                    },
                    onNavigateToGratitudes = {
                        navController.navigate(OraDestinations.Journal.route)
                    }
                )
            }

            // FIX(profile-edit): Écran d'édition de profil
            composable(OraDestinations.EditProfile.route) {
                ProfileEditScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Écran de statistiques détaillées d'une pratique
            composable(
                route = OraDestinations.PracticeStats.route,
                arguments = OraDestinations.PracticeStats.arguments
            ) {
                PracticeStatsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onStartNewSession = {
                        // TODO: Naviguer vers l'écran de démarrage de session
                        // Pour l'instant, on retourne à l'écran précédent
                        navController.popBackStack()
                    }
                )
            }

            // Écran de détail d'une pratique (vidéo/audio player)
            composable(
                route = OraDestinations.PracticeDetail.route,
                arguments = OraDestinations.PracticeDetail.arguments
            ) { backStackEntry ->
                val practiceId = backStackEntry.arguments?.getString("id") ?: return@composable
                PracticeDetailScreen(
                    practiceId = practiceId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // TODO: Ajouter les autres destinations (ContentDetail, VideoPlayer, etc.)
            // Ces composables seront ajoutés avec les écrans correspondants
        }
    }
}

@Composable
private fun OraBottomNavigationBar(
    navController: NavHostController,
    currentDestination: String?
) {
    NavigationBar {
        bottomNavigationItems.forEach { item ->
            val selected = currentDestination == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
