package com.ora.wellbeing.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.mapper.SubcategoryMapper
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.data.model.firestore.SubcategoryDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SubcategoryRepository - Loads subcategories from Firestore
 *
 * Subcategories are stored in Firestore and managed via OraWebApp admin portal.
 *
 * Firestore structure options:
 * 1. Separate collection: "subcategories/{subcategoryId}" with parent_category field
 * 2. Embedded in categories: "categories/{categoryId}" with subcategories array
 *
 * This implementation supports both patterns.
 */
@Singleton
class SubcategoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val SUBCATEGORIES_COLLECTION = "subcategories"
        private const val CATEGORIES_COLLECTION = "categories"
    }

    /**
     * Get subcategories for a specific parent category
     *
     * Tries:
     * 1. First looks for subcategories collection filtered by parent_category
     * 2. Falls back to embedded subcategories in category document
     *
     * @param parentCategory Category ID (e.g., "Meditation", "Yoga")
     * @return Flow of SubcategoryItem list
     */
    fun getSubcategoriesForCategory(parentCategory: String): Flow<List<SubcategoryItem>> = flow {
        try {
            // Try separate collection first
            val subcategories = getFromSubcategoriesCollection(parentCategory)

            if (subcategories.isNotEmpty()) {
                Timber.d("Loaded ${subcategories.size} subcategories from collection for $parentCategory")
                emit(subcategories)
            } else {
                // Fallback to embedded subcategories in category document
                val embedded = getEmbeddedSubcategories(parentCategory)
                Timber.d("Loaded ${embedded.size} embedded subcategories for $parentCategory")
                emit(embedded)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading subcategories for $parentCategory")
            emit(emptyList())
        }
    }

    /**
     * Load subcategories from dedicated collection
     * Note: Firestore uses "category" (lowercase) and "status" fields
     */
    private suspend fun getFromSubcategoriesCollection(parentCategory: String): List<SubcategoryItem> {
        return try {
            // Query with lowercase category to match Firestore (e.g., "meditation" not "Meditation")
            val categoryLower = parentCategory.lowercase()
            val snapshot = firestore
                .collection(SUBCATEGORIES_COLLECTION)
                .whereEqualTo("category", categoryLower)
                .whereEqualTo("status", "active")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Timber.d("Parsing subcategory doc: ${doc.id}, data=${doc.data}")
                    val subcategoryDoc = doc.toObject(SubcategoryDocument::class.java)
                    Timber.d("Parsed SubcategoryDocument: nameFr=${subcategoryDoc?.nameFr}, nameEn=${subcategoryDoc?.nameEn}, nameEs=${subcategoryDoc?.nameEs}")
                    if (subcategoryDoc != null) {
                        SubcategoryMapper.fromFirestore(doc.id, subcategoryDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping subcategory document ${doc.id}")
                    null
                }
            }.sortedBy { it.order }
        } catch (e: Exception) {
            Timber.w(e, "Subcategories collection not found or error")
            emptyList()
        }
    }

    /**
     * Load embedded subcategories from category document
     *
     * Expected structure:
     * categories/{categoryId} {
     *   name: "Meditation",
     *   subcategories: [
     *     { id: "guided", name: "Méditation guidée", ... },
     *     { id: "sleep", name: "Sommeil", ... }
     *   ]
     * }
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun getEmbeddedSubcategories(parentCategory: String): List<SubcategoryItem> {
        return try {
            val doc = firestore
                .collection(CATEGORIES_COLLECTION)
                .document(parentCategory)
                .get()
                .await()

            if (doc.exists()) {
                val subcategoriesData = doc.get("subcategories") as? List<Map<String, Any>>
                SubcategoryMapper.fromFirestoreMapList(subcategoriesData)
            } else {
                Timber.d("Category document not found: $parentCategory")
                emptyList()
            }
        } catch (e: Exception) {
            Timber.w(e, "Error loading embedded subcategories for $parentCategory")
            emptyList()
        }
    }

    /**
     * Get all subcategories (for admin/debug purposes)
     */
    fun getAllSubcategories(): Flow<List<SubcategoryItem>> = flow {
        try {
            val snapshot = firestore
                .collection(SUBCATEGORIES_COLLECTION)
                .whereEqualTo("status", "active")
                .get()
                .await()

            val subcategories = snapshot.documents.mapNotNull { doc ->
                try {
                    val subcategoryDoc = doc.toObject(SubcategoryDocument::class.java)
                    if (subcategoryDoc != null) {
                        SubcategoryMapper.fromFirestore(doc.id, subcategoryDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "Error mapping subcategory ${doc.id}")
                    null
                }
            }.sortedWith(compareBy({ it.parentCategory }, { it.order }))

            emit(subcategories)
        } catch (e: Exception) {
            Timber.e(e, "Error loading all subcategories")
            emit(emptyList())
        }
    }

    /**
     * Get default subcategories when Firestore has none
     * These are fallback subcategories derived from common tags
     * Names are localized based on device language
     *
     * @param parentCategory Category ID
     * @return List of default SubcategoryItems
     */
    fun getDefaultSubcategories(parentCategory: String): List<SubcategoryItem> {
        val lang = getCurrentLanguageCode()
        return when (parentCategory.lowercase()) {
            "meditation" -> listOf(
                SubcategoryItem("guided", parentCategory, getLocalizedSubcategoryName("guided_meditation", lang), filterTags = listOf("guided", "guidée")),
                SubcategoryItem("sleep", parentCategory, getLocalizedSubcategoryName("sleep", lang), filterTags = listOf("sleep", "sommeil")),
                SubcategoryItem("breathing", parentCategory, getLocalizedSubcategoryName("breathing", lang), filterTags = listOf("breathing", "respiration")),
                SubcategoryItem("stress", parentCategory, getLocalizedSubcategoryName("anti_stress", lang), filterTags = listOf("stress", "anti-stress", "relaxation"))
            )
            "yoga" -> listOf(
                SubcategoryItem("gentle", parentCategory, getLocalizedSubcategoryName("gentle_yoga", lang), filterTags = listOf("gentle", "doux", "beginner")),
                SubcategoryItem("dynamic", parentCategory, getLocalizedSubcategoryName("dynamic_yoga", lang), filterTags = listOf("dynamic", "dynamique", "vinyasa")),
                SubcategoryItem("morning", parentCategory, getLocalizedSubcategoryName("morning_yoga", lang), filterTags = listOf("morning", "matin")),
                SubcategoryItem("evening", parentCategory, getLocalizedSubcategoryName("evening_yoga", lang), filterTags = listOf("evening", "soir"))
            )
            "pilates" -> listOf(
                SubcategoryItem("beginner", parentCategory, getLocalizedSubcategoryName("beginner", lang), filterTags = listOf("beginner", "débutant")),
                SubcategoryItem("intermediate", parentCategory, getLocalizedSubcategoryName("intermediate", lang), filterTags = listOf("intermediate", "intermédiaire")),
                SubcategoryItem("core", parentCategory, getLocalizedSubcategoryName("core", lang), filterTags = listOf("core", "abdos", "abs")),
                SubcategoryItem("full-body", parentCategory, getLocalizedSubcategoryName("full_body", lang), filterTags = listOf("full-body", "corps-entier"))
            )
            "bien-etre" -> listOf(
                SubcategoryItem("relaxation", parentCategory, getLocalizedSubcategoryName("relaxation", lang), filterTags = listOf("relaxation", "relax")),
                SubcategoryItem("stretching", parentCategory, getLocalizedSubcategoryName("stretching", lang), filterTags = listOf("stretching", "étirement")),
                SubcategoryItem("massage", parentCategory, getLocalizedSubcategoryName("self_massage", lang), filterTags = listOf("massage", "auto-massage"))
            )
            else -> emptyList()
        }
    }

    /**
     * Get current device language code
     */
    private fun getCurrentLanguageCode(): String {
        val locale = Locale.getDefault().language
        return when (locale) {
            "fr" -> "fr"
            "en" -> "en"
            "es" -> "es"
            else -> "fr"
        }
    }

    /**
     * Get localized subcategory name
     */
    private fun getLocalizedSubcategoryName(key: String, lang: String): String {
        val translations = mapOf(
            "guided_meditation" to mapOf("fr" to "Méditation guidée", "en" to "Guided Meditation", "es" to "Meditación guiada"),
            "sleep" to mapOf("fr" to "Sommeil", "en" to "Sleep", "es" to "Sueño"),
            "breathing" to mapOf("fr" to "Respiration", "en" to "Breathing", "es" to "Respiración"),
            "anti_stress" to mapOf("fr" to "Anti-stress", "en" to "Anti-stress", "es" to "Anti-estrés"),
            "gentle_yoga" to mapOf("fr" to "Yoga doux", "en" to "Gentle Yoga", "es" to "Yoga suave"),
            "dynamic_yoga" to mapOf("fr" to "Yoga dynamique", "en" to "Dynamic Yoga", "es" to "Yoga dinámico"),
            "morning_yoga" to mapOf("fr" to "Yoga du matin", "en" to "Morning Yoga", "es" to "Yoga matutino"),
            "evening_yoga" to mapOf("fr" to "Yoga du soir", "en" to "Evening Yoga", "es" to "Yoga nocturno"),
            "beginner" to mapOf("fr" to "Débutant", "en" to "Beginner", "es" to "Principiante"),
            "intermediate" to mapOf("fr" to "Intermédiaire", "en" to "Intermediate", "es" to "Intermedio"),
            "core" to mapOf("fr" to "Abdos", "en" to "Core", "es" to "Abdominales"),
            "full_body" to mapOf("fr" to "Corps entier", "en" to "Full Body", "es" to "Cuerpo completo"),
            "relaxation" to mapOf("fr" to "Relaxation", "en" to "Relaxation", "es" to "Relajación"),
            "stretching" to mapOf("fr" to "Étirements", "en" to "Stretching", "es" to "Estiramientos"),
            "self_massage" to mapOf("fr" to "Auto-massage", "en" to "Self-massage", "es" to "Auto-masaje")
        )
        return translations[key]?.get(lang) ?: translations[key]?.get("fr") ?: key
    }
}
