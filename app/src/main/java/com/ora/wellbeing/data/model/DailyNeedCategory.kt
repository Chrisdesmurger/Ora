package com.ora.wellbeing.data.model

import com.ora.wellbeing.core.localization.LocalizationProvider

/**
 * Modèle pour les catégories de besoins quotidiens
 * Ces catégories sont codées en dur car elles sont fixes et ne changent pas
 *
 * **i18n Support** (Issue #39):
 * - French (fr): nameFr
 * - English (en): nameEn
 * - Spanish (es): nameEs
 *
 * Use `getLocalizedName()` to get the name in the current locale.
 */
data class DailyNeedCategory(
    val id: String,
    val nameFr: String,
    val nameEn: String,
    val nameEs: String, // NEW - Issue #39
    val filterTags: List<String>,
    val descriptionFr: String = "",
    val descriptionEn: String = "",
    val descriptionEs: String = "" // NEW - Issue #39
) {
    /**
     * Get the localized name based on the current locale.
     *
     * @param locale Locale code ("fr", "en", "es"). Defaults to current app locale.
     * @return Localized name with fallback to English
     */
    fun getLocalizedName(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale) {
            "fr" -> nameFr
            "es" -> nameEs.ifEmpty { nameEn }
            else -> nameEn.ifEmpty { nameFr } // Default to English, fallback to French
        }
    }

    /**
     * Get the localized description based on the current locale.
     *
     * @param locale Locale code ("fr", "en", "es"). Defaults to current app locale.
     * @return Localized description with fallback to English
     */
    fun getLocalizedDescription(locale: String = LocalizationProvider.DEFAULT_LOCALE): String {
        return when (locale) {
            "fr" -> descriptionFr
            "es" -> descriptionEs.ifEmpty { descriptionEn }
            else -> descriptionEn.ifEmpty { descriptionFr }
        }
    }

    companion object {
        /**
         * Liste des 4 catégories de besoins quotidiens
         */
        fun getAllCategories(): List<DailyNeedCategory> = listOf(
            DailyNeedCategory(
                id = "anti-stress",
                nameFr = "Anti-stress",
                nameEn = "Anti-stress",
                nameEs = "Anti-estrés", // NEW - Issue #39
                filterTags = listOf("stress_relief", "anxiety_relief", "calm"),
                descriptionFr = "Pratiques pour réduire le stress et l'anxiété",
                descriptionEn = "Practices to reduce stress and anxiety",
                descriptionEs = "Prácticas para reducir el estrés y la ansiedad"
            ),
            DailyNeedCategory(
                id = "energie-matinale",
                nameFr = "Énergie matinale",
                nameEn = "Morning Energy",
                nameEs = "Energía matutina", // NEW - Issue #39
                filterTags = listOf("morning_energy", "energizing", "wake_up"),
                descriptionFr = "Pratiques énergisantes pour bien commencer la journée",
                descriptionEn = "Energizing practices to start your day well",
                descriptionEs = "Prácticas energizantes para empezar bien el día"
            ),
            DailyNeedCategory(
                id = "relaxation",
                nameFr = "Relaxation",
                nameEn = "Relaxation",
                nameEs = "Relajación", // NEW - Issue #39
                filterTags = listOf("relaxation", "deep_relaxation", "unwind"),
                descriptionFr = "Pratiques pour se détendre profondément",
                descriptionEn = "Practices for deep relaxation",
                descriptionEs = "Prácticas para relajarse profundamente"
            ),
            DailyNeedCategory(
                id = "pratique-du-soir",
                nameFr = "Pratique du soir",
                nameEn = "Evening Practice",
                nameEs = "Práctica nocturna", // NEW - Issue #39
                filterTags = listOf("evening_practice", "sleep_preparation", "bedtime"),
                descriptionFr = "Pratiques pour se préparer au sommeil",
                descriptionEn = "Practices to prepare for sleep",
                descriptionEs = "Prácticas para prepararse para dormir"
            )
        )

        /**
         * Récupère une catégorie par son ID
         */
        fun getCategoryById(id: String): DailyNeedCategory? {
            return getAllCategories().find { it.id == id }
        }
    }
}
