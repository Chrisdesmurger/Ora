package com.ora.wellbeing.data.model

/**
 * Modèle pour les catégories de besoins quotidiens
 * Ces catégories sont codées en dur car elles sont fixes et ne changent pas
 */
data class DailyNeedCategory(
    val id: String,
    val nameFr: String,
    val nameEn: String,
    val filterTags: List<String>,
    val description: String = ""
) {
    companion object {
        /**
         * Liste des 4 catégories de besoins quotidiens
         */
        fun getAllCategories(): List<DailyNeedCategory> = listOf(
            DailyNeedCategory(
                id = "anti-stress",
                nameFr = "Anti-stress",
                nameEn = "Anti-stress",
                filterTags = listOf("stress_relief", "anxiety_relief", "calm"),
                description = "Pratiques pour réduire le stress et l'anxiété"
            ),
            DailyNeedCategory(
                id = "energie-matinale",
                nameFr = "Énergie matinale",
                nameEn = "Morning Energy",
                filterTags = listOf("morning_energy", "energizing", "wake_up"),
                description = "Pratiques énergisantes pour bien commencer la journée"
            ),
            DailyNeedCategory(
                id = "relaxation",
                nameFr = "Relaxation",
                nameEn = "Relaxation",
                filterTags = listOf("relaxation", "deep_relaxation", "unwind"),
                description = "Pratiques pour se détendre profondément"
            ),
            DailyNeedCategory(
                id = "pratique-du-soir",
                nameFr = "Pratique du soir",
                nameEn = "Evening Practice",
                filterTags = listOf("evening_practice", "sleep_preparation", "bedtime"),
                description = "Pratiques pour se préparer au sommeil"
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
