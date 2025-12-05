package com.ora.wellbeing.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.ora.wellbeing.presentation.screens.journal.DailyJournalEntryScreen
import com.ora.wellbeing.presentation.screens.journal.JournalCalendarScreen
import com.ora.wellbeing.presentation.screens.journal.JournalScreen
import com.ora.wellbeing.presentation.screens.programs.ProgramsScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileEditScreen
import com.ora.wellbeing.presentation.screens.stats.PracticeStatsScreen
import com.ora.wellbeing.feature.practice.ui.SpecializedPlayerScreen
import com.ora.wellbeing.presentation.screens.debug.FirestoreDebugScreen
import com.ora.wellbeing.presentation.screens.onboarding.OnboardingScreen
import com.ora.wellbeing.presentation.screens.onboarding.OnboardingCelebrationScreen

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
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    // FIX(auth): Redirection vers Auth Flow si non connecté
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && currentDestination?.route != "auth_flow") {
            navController.navigate("auth_flow") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    // FIX(onboarding): Redirection vers Onboarding si non complété
    LaunchedEffect(isAuthenticated, userProfile) {
        val profile = userProfile; if (isAuthenticated && profile != null && !profile.hasCompletedOnboarding) {
            val currentRoute = currentDestination?.route
            // Only redirect if not already on onboarding screens
            if (currentRoute != OraDestinations.Onboarding.route &&
                currentRoute != OraDestinations.OnboardingCelebration.route) {
                navController.navigate(OraDestinations.Onboarding.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),  // Maximiser la surface d'écran - ne pas consommer les insets automatiquement
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
            startDestination = if (isAuthenticated) OraDestinations.Home.route else "auth_flow",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Registration Onboarding Flow (6 screens + questionnaire)
            // Shown to non-authenticated users
            composable("auth_flow") {
                AuthNavGraph(
                    onAuthComplete = {
                        // After registration and account creation,
                        // the existing logic will redirect to OnboardingScreen if needed
                        navController.navigate(OraDestinations.Home.route) {
                            popUpTo("auth_flow") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Login screen for existing users (accessible from Welcome screen)
            composable(OraDestinations.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(OraDestinations.Home.route) {
                            popUpTo("auth_flow") {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Onboarding Flow
            composable(OraDestinations.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(OraDestinations.OnboardingCelebration.route) {
                            popUpTo(OraDestinations.Onboarding.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(OraDestinations.OnboardingCelebration.route) {
                OnboardingCelebrationScreen(
                    onContinue = {
                        navController.navigate(OraDestinations.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
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
                // Show daily journal entry form directly instead of list view
                DailyJournalEntryScreen(
                    date = null, // Today's entry
                    onNavigateBack = {
                        // Navigate back to home when closing from bottom nav
                        navController.navigate(OraDestinations.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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

            // REDESIGNED: Profile screen with new UI (Issue #64)
            composable(OraDestinations.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(OraDestinations.EditProfile.route)
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

            // Écran de détail d'une pratique - Lecteurs spécialisés par discipline
            composable(
                route = OraDestinations.PracticeDetail.route,
                arguments = OraDestinations.PracticeDetail.arguments
            ) { backStackEntry ->
                val practiceId = backStackEntry.arguments?.getString("id") ?: return@composable
                SpecializedPlayerScreen(
                    practiceId = practiceId,
                    onBack = { navController.popBackStack() },
                    onMinimize = { navController.popBackStack() }
                )
            }

            // NEW: Daily Journal Entry Screen
            composable(
                route = OraDestinations.DailyJournalEntry.route,
                arguments = OraDestinations.DailyJournalEntry.arguments
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date")
                DailyJournalEntryScreen(
                    date = date,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // NEW: Journal Calendar Screen
            composable(OraDestinations.JournalCalendar.route) {
                JournalCalendarScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEntry = { date ->
                        navController.navigate(OraDestinations.DailyJournalEntry.createRoute(date))
                    }
                )
            }

            // Debug Screen - Firestore Diagnostic
            composable(OraDestinations.FirestoreDebug.route) {
                FirestoreDebugScreen()
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
    NavigationBar(
        modifier = Modifier.height(64.dp)
    ) {
        bottomNavigationItems.forEach { item ->
            val selected = currentDestination == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
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
