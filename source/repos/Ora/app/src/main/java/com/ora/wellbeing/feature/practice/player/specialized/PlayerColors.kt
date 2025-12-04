package com.ora.wellbeing.feature.practice.player.specialized

import androidx.compose.ui.graphics.Color

/**
 * Palettes de couleurs pour chaque type de lecteur
 */
object PlayerColors {

    // Yoga/Pilates - Vert menthe et vert sauge
    object Yoga {
        val background = Color(0xFFD4E8D9)      // Vert menthe clair
        val backgroundGradientEnd = Color(0xFFE8F4EB) // Vert très clair
        val accent = Color(0xFF7BA089)          // Vert sauge
        val accentDark = Color(0xFF5A7A66)      // Vert sauge foncé
        val onBackground = Color(0xFF2C2C2C)    // Texte foncé
        val surface = Color(0xFFFFFBF8)         // Surface blanche
        val chapterActive = Color(0xFF7BA089)   // Chapitre actif
        val chapterInactive = Color(0xFFB8D4C0) // Chapitre inactif
    }

    // Méditation/Respiration - Lavande
    object Meditation {
        val background = Color(0xFFD4C4E8)      // Lavande
        val backgroundGradientEnd = Color(0xFFE8E0F4) // Lavande très clair
        val accent = Color(0xFF8B7BA0)          // Lavande foncé
        val accentDark = Color(0xFF6B5A80)      // Lavande très foncé
        val onBackground = Color(0xFF2C2C2C)    // Texte foncé
        val surface = Color(0xFFFFFBF8)         // Surface blanche
        val breatheIn = Color(0xFF9B8BB0)       // Inspiration
        val breatheOut = Color(0xFFB8A8C8)      // Expiration

        // Mode nuit
        val nightBackground = Color(0xFF1A1A2E)
        val nightAccent = Color(0xFFE0B0FF)
        val nightOnBackground = Color(0xFFE8E0F4)
    }

    // Auto-massage/Bien-être - Orange Ora
    object Massage {
        val background = Color(0xFFFFF5F0)      // Beige chaud (Ora background)
        val backgroundGradientEnd = Color(0xFFFFFAF5) // Beige très clair
        val accent = Color(0xFFF4845F)          // Orange Ora
        val accentDark = Color(0xFFD76B3D)      // Orange foncé
        val onBackground = Color(0xFF2C2C2C)    // Texte foncé
        val surface = Color(0xFFFFFBF8)         // Surface blanche
        val zoneActive = Color(0xFFFFD4C4)      // Zone active - Pêche
        val zoneCompleted = Color(0xFFD4E8D9)   // Zone terminée - Vert menthe
        val zonePending = Color(0xFFE8E8E8)     // Zone en attente - Gris clair
        val pressureLow = Color(0xFFB8D4C0)     // Pression légère
        val pressureMedium = Color(0xFFF4845F)  // Pression moyenne
        val pressureHigh = Color(0xFFD76B3D)    // Pression forte
    }
}
