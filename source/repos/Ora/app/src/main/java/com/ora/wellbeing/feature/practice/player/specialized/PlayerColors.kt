package com.ora.wellbeing.feature.practice.player.specialized

import androidx.compose.ui.graphics.Color

/**
 * Palettes de couleurs pour chaque type de lecteur
 * Basées sur le Design System Ora officiel (docs/ORA_DESIGN_SYSTEM.md)
 *
 * Couleurs principales Ora :
 * - Primary (Orange Coral): #F18D5C
 * - Background (Beige): #F5EFE6
 * - Surface (Blanc cassé): #FFFBF8
 * - Texte: #4A4A4A
 */
object PlayerColors {

    // Couleurs Ora principales (réutilisées)
    private val OraPrimary = Color(0xFFF18D5C)          // Orange Coral
    private val OraBackground = Color(0xFFF5EFE6)       // Beige/Crème
    private val OraSurface = Color(0xFFFFFBF8)          // Blanc cassé chaud
    private val OraOnSurface = Color(0xFF4A4A4A)        // Texte principal (gris-brun)
    private val OraSecondary = Color(0xFFF5C9A9)        // Peach doux

    // Yoga/Pilates - Vert Sage (CategoryYogaGreen)
    object Yoga {
        val background = Color(0xFFA8C5B0)              // Vert sage (catégorie Yoga)
        val backgroundGradientEnd = Color(0xFFD4E8D4)   // Menthe douce (gradient)
        val accent = Color(0xFF7BA089)                  // Vert sage foncé
        val accentDark = Color(0xFF5A7A66)              // Vert encore plus foncé
        val onBackground = OraOnSurface                 // Texte Ora standard
        val surface = OraSurface                        // Surface Ora
        val chapterActive = Color(0xFF7BA089)           // Chapitre actif
        val chapterInactive = Color(0xFFD4E8D4)         // Chapitre inactif
    }

    // Méditation/Respiration - Lavande (CategoryMeditationLavender)
    object Meditation {
        val background = Color(0xFFC5B8D4)              // Lavande (catégorie Méditation)
        val backgroundGradientEnd = Color(0xFFE8E0F4)   // Lavande très clair
        val accent = Color(0xFF9B8BB0)                  // Lavande moyen
        val accentDark = Color(0xFF7A6A90)              // Lavande foncé
        val onBackground = OraOnSurface                 // Texte Ora standard
        val surface = OraSurface                        // Surface Ora
        val breatheIn = Color(0xFF9B8BB0)               // Inspiration
        val breatheOut = Color(0xFFC5B8D4)              // Expiration

        // Mode nuit - Tons bruns chauds (cohérent avec Ora dark mode)
        val nightBackground = Color(0xFF2A2520)         // Brun très foncé (Ora dark)
        val nightAccent = Color(0xFFC5B8D4)             // Lavande visible
        val nightOnBackground = Color(0xFFE8E0D8)       // Texte clair chaud
    }

    // Auto-massage/Bien-être - Orange Ora (couleur principale de la marque)
    object Massage {
        val background = OraBackground                  // Beige Ora (#F5EFE6)
        val backgroundGradientEnd = OraSurface          // Blanc cassé Ora
        val accent = OraPrimary                         // Orange Coral Ora (#F18D5C)
        val accentDark = Color(0xFFD76B3D)              // Orange foncé
        val onBackground = OraOnSurface                 // Texte Ora standard
        val surface = OraSurface                        // Surface Ora
        val zoneActive = OraSecondary                   // Peach doux (#F5C9A9)
        val zoneCompleted = Color(0xFFD4E8D4)           // Menthe (gratitude)
        val zonePending = Color(0xFFE8E0D8)             // Gris chaud
        val pressureLow = Color(0xFFA8C5B0)             // Vert sage (légère)
        val pressureMedium = OraPrimary                 // Orange Ora (moyenne)
        val pressureHigh = Color(0xFFD76B3D)            // Orange foncé (forte)
    }

    // Couleurs de catégories Ora (pour référence)
    object Categories {
        val yoga = Color(0xFFA8C5B0)                    // Vert sage
        val pilates = Color(0xFFF5C9A9)                 // Peach
        val meditation = Color(0xFFC5B8D4)              // Lavande
        val breathing = Color(0xFFA3C4E0)               // Bleu clair
    }
}
