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
import androidx.compose.ui.res.stringResource
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
import com.ora.wellbeing.presentation.screens.library.DailyNeedDetailScreen
import com.ora.wellbeing.presentation.screens.journal.DailyJournalEntryScreen
import com.ora.wellbeing.presentation.screens.journal.JournalCalendarScreen
import com.ora.wellbeing.presentation.screens.journal.JournalScreen
import com.ora.wellbeing.presentation.screens.programs.ProgramsScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileScreen
import com.ora.wellbeing.presentation.screens.profile.ProfileEditScreen
import com.ora.wellbeing.presentation.screens.settings.EmailPreferencesScreen
import com.ora.wellbeing.presentation.screens.stats.PracticeStatsScreen
import com.ora.wellbeing.feature.practice.ui.SpecializedPlayerScreen
import com.ora.wellbeing.presentation.screens.debug.FirestoreDebugScreen
import com.ora.wellbeing.presentation.screens.onboarding.OnboardingScreen
import com.ora.wellbeing.presentation.screens.onboarding.OnboardingCelebrationScreen

/**
 * FIX(auth): Navigation principale avec verification d'authentification
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

    // FIX(auth): Redirection vers Auth Flow si non connecte
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

    // FIX(onboarding): Redirection vers Onboarding si non complete
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
        contentWindowInsets = WindowInsets(0.dp),  // Maximiser la surface d'ecran - ne pas consommer les insets automatiquement
        bottomBar = {
            // Afficher la bottom bar seulement sur les ecrans principaux et si authentifie
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

            // Ecrans principaux avec bottom navigation
            composable(OraDestinations.Home.route) {
                HomeScreen(
                    onNavigateToContent = { contentId ->
                        // Redirection vers PracticeDetail au lieu de ContentDetail
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    },
                    onNavigateToProgram = { programId ->
                        navController.navigate(OraDestinations.ProgramDetail.createRoute(programId))
                    },
                    // Issue #37: Quick Sessions navigate to filtered library
                    onNavigateToQuickSession = { sessionType ->
                        navController.navigate(
                            OraDestinations.CategoryDetailFiltered.createRoute(
                                categoryId = sessionType.categoryId,
                                maxDurationMinutes = 10 // Sessions rapides: moins de 10 minutes
                            )
                        )
                    },
                    onNavigateToDailyNeedCategory = { categoryId ->
                        // NEW (Issue #33): Navigate to DailyNeedDetail screen
                        // Filtre par need_tags tous types de contenu (Yoga, Meditation, Pilates, Auto-massage)
                        navController.navigate(OraDestinations.DailyNeedDetail.createRoute(categoryId))
                    }
                )
            }

            // Library - NEW: Display categories screen directly
            composable(OraDestinations.Library.route) {
                com.ora.wellbeing.presentation.screens.library.ContentLibraryCategoriesScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(OraDestinations.CategoryDetail.createRoute(categoryId))
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

            // FIX(profile-edit): Ecran d'edition de profil
            // Issue #55: Added onNavigateToEmailPreferences callback
            composable(OraDestinations.EditProfile.route) {
                ProfileEditScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEmailPreferences = {
                        navController.navigate(OraDestinations.EmailPreferences.route)
                    }
                )
            }

            // NEW (Issue #55): Email Preferences Screen
            composable(OraDestinations.EmailPreferences.route) {
                EmailPreferencesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Ecran de statistiques detaillees d'une pratique
            composable(
                route = OraDestinations.PracticeStats.route,
                arguments = OraDestinations.PracticeStats.arguments
            ) {
                PracticeStatsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onStartNewSession = {
                        // TODO: Naviguer vers l'ecran de demarrage de session
                        // Pour l'instant, on retourne a l'ecran precedent
                        navController.popBackStack()
                    }
                )
            }

            // Ecran de detail d'une pratique - Lecteurs specialises par discipline
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

            // NEW: Library Categories Screen (main entry for library)
            composable(OraDestinations.LibraryCategories.route) {
                com.ora.wellbeing.presentation.screens.library.ContentLibraryCategoriesScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(OraDestinations.CategoryDetail.createRoute(categoryId))
                    }
                )
            }

            // NEW: Category Detail Screen (filtered content by category)
            composable(
                route = OraDestinations.CategoryDetail.route,
                arguments = OraDestinations.CategoryDetail.arguments
            ) {
                com.ora.wellbeing.presentation.screens.library.ContentCategoryDetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onContentClick = { contentId ->
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    }
                )
            }

            // NEW (Issue #37): Category Detail with Duration Filter for Quick Sessions
            composable(
                route = OraDestinations.CategoryDetailFiltered.route,
                arguments = OraDestinations.CategoryDetailFiltered.arguments
            ) {
                com.ora.wellbeing.presentation.screens.library.ContentCategoryDetailFilteredScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onContentClick = { contentId ->
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    }
                )
            }

            // NEW (Issue #33): Daily Need Detail Screen - filters by need_tags across all content types
            composable(
                route = OraDestinations.DailyNeedDetail.route,
                arguments = OraDestinations.DailyNeedDetail.arguments
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                DailyNeedDetailScreen(
                    categoryId = categoryId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToContent = { contentId ->
                        navController.navigate(OraDestinations.PracticeDetail.createRoute(contentId))
                    }
                )
            }


            // Debug Screen - Firestore Diagnostic
            composable(OraDestinations.FirestoreDebug.route) {
                FirestoreDebugScreen()
            }

            // TODO: Ajouter les autres destinations (ContentDetail, VideoPlayer, etc.)
            // Ces composables seront ajoutes avec les ecrans correspondants
        }
    }
}

/**
 * FIX (Issue #39 - Phase 1d): Use stringResource for labels
 */
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
                        contentDescription = stringResource(item.labelRes),
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = null,
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
