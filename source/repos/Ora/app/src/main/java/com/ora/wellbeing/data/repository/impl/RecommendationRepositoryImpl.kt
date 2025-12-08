package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.UserRecommendation
import com.ora.wellbeing.domain.repository.ContentRepository
import com.ora.wellbeing.domain.repository.RecommendationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RecommendationRepository
 *
 * Fetches personalized recommendations from users/{uid}/recommendations subcollection
 * Uses the "latest" document which is always updated by Cloud Functions
 *
 * Flow:
 * 1. Cloud Functions generate recommendations (on onboarding complete, weekly, or manual)
 * 2. Recommendations are stored in Firestore with lesson IDs
 * 3. This repository fetches the recommendation document
 * 4. Then fetches the actual ContentItem details for each lesson ID
 */
@Singleton
class RecommendationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val contentRepository: ContentRepository
) : RecommendationRepository {

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_RECOMMENDATIONS = "recommendations"
        private const val DOC_LATEST = "latest"
    }

    /**
     * Observes the latest recommendations for a user
     * Uses Firestore snapshot listener for real-time updates
     */
    override fun getLatestRecommendation(uid: String): Flow<UserRecommendation?> = callbackFlow {
        Timber.d("RecommendationRepository: Starting to observe recommendations for user $uid")

        val docRef = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(SUBCOLLECTION_RECOMMENDATIONS)
            .document(DOC_LATEST)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "RecommendationRepository: Error observing recommendations")
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                Timber.d("RecommendationRepository: No recommendations found for user $uid")
                trySend(null)
                return@addSnapshotListener
            }

            try {
                val recommendation = snapshot.toObject(UserRecommendation::class.java)
                Timber.d("RecommendationRepository: Received ${recommendation?.lessonIds?.size ?: 0} recommendations")
                trySend(recommendation)
            } catch (e: Exception) {
                Timber.e(e, "RecommendationRepository: Error parsing recommendation")
                trySend(null)
            }
        }

        awaitClose {
            Timber.d("RecommendationRepository: Stopping observation for user $uid")
            listener.remove()
        }
    }

    /**
     * Observes personalized content recommendations for a user
     * First fetches the recommendation document, then resolves lesson IDs to ContentItems
     */
    override fun getRecommendedContent(uid: String, limit: Int): Flow<List<ContentItem>> = flow {
        Timber.d("RecommendationRepository: Fetching recommended content for user $uid (limit=$limit)")

        // Get latest recommendation
        val recommendation = getLatestRecommendation(uid).first()

        if (recommendation == null || recommendation.lessonIds.isEmpty()) {
            Timber.d("RecommendationRepository: No recommendations available")
            emit(emptyList())
            return@flow
        }

        val lessonIds = recommendation.lessonIds.take(limit)
        Timber.d("RecommendationRepository: Found ${lessonIds.size} recommended lesson IDs")

        // Fetch all content and filter by lesson IDs
        val allContent = contentRepository.getAllContent().first()
        val recommendedContent = lessonIds.mapNotNull { lessonId ->
            allContent.find { it.id == lessonId }
        }

        // Sort by recommendation score (maintain order from recommendation)
        val orderedContent = lessonIds.mapNotNull { lessonId ->
            recommendedContent.find { it.id == lessonId }
        }

        Timber.d("RecommendationRepository: Resolved ${orderedContent.size} content items")
        emit(orderedContent)
    }

    /**
     * Observes recommended content with real-time updates
     * Combines recommendation listener with content resolution
     */
    fun observeRecommendedContent(uid: String, limit: Int = 5): Flow<List<ContentItem>> {
        return getLatestRecommendation(uid).map { recommendation ->
            if (recommendation == null || recommendation.lessonIds.isEmpty()) {
                Timber.d("RecommendationRepository: No recommendations in observation")
                return@map emptyList()
            }

            val lessonIds = recommendation.lessonIds.take(limit)

            // Fetch content for each lesson ID
            val allContent = contentRepository.getAllContent().first()
            lessonIds.mapNotNull { lessonId ->
                allContent.find { it.id == lessonId }
            }
        }
    }

    /**
     * Checks if user has recommendations generated
     */
    override suspend fun hasRecommendations(uid: String): Boolean {
        return try {
            val docSnapshot = firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_RECOMMENDATIONS)
                .document(DOC_LATEST)
                .get()
                .await()

            val exists = docSnapshot.exists()
            Timber.d("RecommendationRepository: hasRecommendations($uid) = $exists")
            exists
        } catch (e: Exception) {
            Timber.e(e, "RecommendationRepository: Error checking recommendations")
            false
        }
    }

    /**
     * Gets the recommendation history for a user
     */
    override suspend fun getRecommendationHistory(uid: String, limit: Int): List<UserRecommendation> {
        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_RECOMMENDATIONS)
                .orderBy("generated_at", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val history = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UserRecommendation::class.java)
            }

            Timber.d("RecommendationRepository: Fetched ${history.size} historical recommendations")
            history
        } catch (e: Exception) {
            Timber.e(e, "RecommendationRepository: Error fetching history")
            emptyList()
        }
    }

    /**
     * Force refresh recommendations from Firestore
     * Clears any local cache and re-fetches
     */
    override suspend fun refreshRecommendations(uid: String) {
        Timber.d("RecommendationRepository: Refreshing recommendations for user $uid")

        try {
            // Force fetch from server (bypass cache)
            firestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_RECOMMENDATIONS)
                .document(DOC_LATEST)
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()

            Timber.d("RecommendationRepository: Recommendations refreshed")
        } catch (e: Exception) {
            Timber.e(e, "RecommendationRepository: Error refreshing recommendations")
            throw e
        }
    }
}
