package com.ora.wellbeing.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ora.wellbeing.data.mapper.SubcategoryMapper
import com.ora.wellbeing.data.model.SubcategoryItem
import com.ora.wellbeing.data.model.firestore.SubcategoryDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
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
     */
    private suspend fun getFromSubcategoriesCollection(parentCategory: String): List<SubcategoryItem> {
        return try {
            val snapshot = firestore
                .collection(SUBCATEGORIES_COLLECTION)
                .whereEqualTo("parent_category", parentCategory)
                .whereEqualTo("is_active", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val subcategoryDoc = doc.toObject(SubcategoryDocument::class.java)
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
                .whereEqualTo("is_active", true)
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
     *
     * @param parentCategory Category ID
     * @return List of default SubcategoryItems
     */
    fun getDefaultSubcategories(parentCategory: String): List<SubcategoryItem> {
        return when (parentCategory.lowercase()) {
            "meditation" -> listOf(
                SubcategoryItem("guided", parentCategory, "Méditation guidée", filterTags = listOf("guided", "guidée")),
                SubcategoryItem("sleep", parentCategory, "Sommeil", filterTags = listOf("sleep", "sommeil")),
                SubcategoryItem("breathing", parentCategory, "Respiration", filterTags = listOf("breathing", "respiration")),
                SubcategoryItem("stress", parentCategory, "Anti-stress", filterTags = listOf("stress", "anti-stress", "relaxation"))
            )
            "yoga" -> listOf(
                SubcategoryItem("gentle", parentCategory, "Yoga doux", filterTags = listOf("gentle", "doux", "beginner")),
                SubcategoryItem("dynamic", parentCategory, "Yoga dynamique", filterTags = listOf("dynamic", "dynamique", "vinyasa")),
                SubcategoryItem("morning", parentCategory, "Yoga du matin", filterTags = listOf("morning", "matin")),
                SubcategoryItem("evening", parentCategory, "Yoga du soir", filterTags = listOf("evening", "soir"))
            )
            "pilates" -> listOf(
                SubcategoryItem("beginner", parentCategory, "Débutant", filterTags = listOf("beginner", "débutant")),
                SubcategoryItem("intermediate", parentCategory, "Intermédiaire", filterTags = listOf("intermediate", "intermédiaire")),
                SubcategoryItem("core", parentCategory, "Abdos", filterTags = listOf("core", "abdos", "abs")),
                SubcategoryItem("full-body", parentCategory, "Corps entier", filterTags = listOf("full-body", "corps-entier"))
            )
            "bien-etre" -> listOf(
                SubcategoryItem("relaxation", parentCategory, "Relaxation", filterTags = listOf("relaxation", "relax")),
                SubcategoryItem("stretching", parentCategory, "Étirements", filterTags = listOf("stretching", "étirement")),
                SubcategoryItem("massage", parentCategory, "Auto-massage", filterTags = listOf("massage", "auto-massage"))
            )
            else -> emptyList()
        }
    }
}
