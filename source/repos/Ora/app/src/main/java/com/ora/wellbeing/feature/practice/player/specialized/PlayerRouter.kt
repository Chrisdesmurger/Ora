package com.ora.wellbeing.feature.practice.player.specialized

import androidx.compose.runtime.Composable
import com.ora.wellbeing.feature.practice.player.specialized.massage.MassagePlayerScreen
import com.ora.wellbeing.feature.practice.player.specialized.meditation.MeditationPlayerScreen
import com.ora.wellbeing.feature.practice.player.specialized.yoga.YogaPlayerScreen

/**
 * Router qui dirige vers le lecteur spécialisé approprié selon le type de pratique
 */
@Composable
fun PlayerRouter(
    practiceId: String,
    playerType: PlayerType,
    onBack: () -> Unit,
    onMinimize: () -> Unit
) {
    when (playerType) {
        PlayerType.YOGA -> {
            YogaPlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        PlayerType.MEDITATION -> {
            MeditationPlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
        PlayerType.MASSAGE -> {
            MassagePlayerScreen(
                practiceId = practiceId,
                onBack = onBack,
                onMinimize = onMinimize
            )
        }
    }
}
