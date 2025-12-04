package com.ora.wellbeing.feature.practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ora.wellbeing.feature.practice.player.specialized.PlayerRouter
import com.ora.wellbeing.feature.practice.player.specialized.PlayerType

/**
 * Écran wrapper qui détermine le type de lecteur et route vers le lecteur spécialisé.
 *
 * Chaque lecteur spécialisé (Yoga, Méditation, Massage) gère son propre chargement,
 * ce composant sert uniquement à déterminer quel lecteur afficher.
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
        playerType != null -> {
            // Router vers le lecteur spécialisé
            PlayerRouter(
                practiceId = practiceId,
                playerType = playerType,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        uiState.isLoading -> {
            // Afficher un écran de chargement minimal pendant la détermination du type
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Chargement...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            // Fallback : utiliser le lecteur Yoga par défaut (vidéo)
            // ou afficher une erreur
            PlayerRouter(
                practiceId = practiceId,
                playerType = PlayerType.YOGA,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
    }
}
