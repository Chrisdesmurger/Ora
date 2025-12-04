package com.ora.wellbeing.feature.practice.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.feature.practice.player.specialized.PlayerRouter
import com.ora.wellbeing.feature.practice.player.specialized.PlayerType

/**
 * Écran wrapper qui charge la pratique et route vers le lecteur spécialisé approprié.
 *
 * Ce composant :
 * 1. Charge les informations de la pratique via le ViewModel
 * 2. Détermine le type de lecteur approprié selon la discipline
 * 3. Route vers YogaPlayerScreen, MeditationPlayerScreen ou MassagePlayerScreen
 *
 * Usage dans la navigation :
 * ```kotlin
 * composable(OraDestinations.PracticeDetail.route) { backStackEntry ->
 *     val practiceId = backStackEntry.arguments?.getString("id") ?: return@composable
 *     SpecializedPlayerScreen(
 *         practiceId = practiceId,
 *         onBack = { navController.popBackStack() },
 *         onMinimize = { navController.popBackStack() }
 *     )
 * }
 * ```
 */
@Composable
fun SpecializedPlayerScreen(
    practiceId: String,
    onBack: () -> Unit,
    onMinimize: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Charger la pratique pour déterminer son type
    LaunchedEffect(practiceId) {
        viewModel.loadPractice(practiceId)
    }

    // Déterminer le type de lecteur basé sur la discipline
    val playerType = remember(uiState.practice) {
        uiState.practice?.let { practice ->
            PlayerType.fromDiscipline(practice.discipline)
        }
    }

    // Router vers le lecteur approprié
    when {
        uiState.isLoading -> {
            // Afficher l'écran de chargement standard
            PlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        uiState.error != null -> {
            // En cas d'erreur, utiliser le lecteur standard
            PlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        playerType != null -> {
            // Router vers le lecteur spécialisé
            PlayerRouter(
                practiceId = practiceId,
                playerType = playerType,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        else -> {
            // Fallback vers le lecteur standard
            PlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
    }
}
