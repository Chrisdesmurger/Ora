package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.domain.repository.ContentRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContentRepository {

    companion object {
        private const val COLLECTION_CONTENT = "content"
    }

    override fun getAllContent(): Flow<List<ContentItem>> = callbackFlow {
        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getAllContent: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getContentByCategory(category: String): Flow<List<ContentItem>> = callbackFlow {
        require(category.isNotBlank()) { "Category cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .whereEqualTo("category", category)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getContentByCategory: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getContent(contentId: String): Flow<ContentItem?> = callbackFlow {
        require(contentId.isNotBlank()) { "Content ID cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .document(contentId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getContent: Error")
                    }
                    snapshot != null -> {
                        val content = if (snapshot.exists()) snapshot.toObject(ContentItem::class.java) else null
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getPopularContent(limit: Int): Flow<List<ContentItem>> = callbackFlow {
        require(limit > 0) { "Limit must be > 0" }

        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .whereEqualTo("isPopular", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getPopularContent: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getNewContent(limit: Int): Flow<List<ContentItem>> = callbackFlow {
        require(limit > 0) { "Limit must be > 0" }

        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .whereEqualTo("isNew", true)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getNewContent: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getContentByDuration(minMinutes: Int, maxMinutes: Int): Flow<List<ContentItem>> = callbackFlow {
        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .whereGreaterThanOrEqualTo("durationMinutes", minMinutes)
            .whereLessThanOrEqualTo("durationMinutes", maxMinutes)
            .orderBy("durationMinutes")
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getContentByDuration: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun searchContent(query: String): Flow<List<ContentItem>> {
        // Firestore doesn't support full-text search, so we filter client-side
        return getAllContent().map { contentList ->
            if (query.isBlank()) {
                contentList
            } else {
                contentList.filter { it.matchesQuery(query) }
            }
        }
    }

    override fun getContentByInstructor(instructor: String): Flow<List<ContentItem>> = callbackFlow {
        require(instructor.isNotBlank()) { "Instructor cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .whereEqualTo("instructor", instructor)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getContentByInstructor: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val content = snapshot.documents.mapNotNull { it.toObject(ContentItem::class.java) }
                        trySend(content)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun getTotalContentCount(): Int = try {
        val snapshot = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .get()
            .await()

        snapshot.size()
    } catch (e: Exception) {
        Timber.e(e, "getTotalContentCount: Error")
        0
    }

    override suspend fun getAllInstructors(): List<String> = try {
        val snapshot = firestore
            .collection(COLLECTION_CONTENT)
            .whereEqualTo("isActive", true)
            .get()
            .await()

        snapshot.documents
            .mapNotNull { it.getString("instructor") }
            .distinct()
            .sorted()
    } catch (e: Exception) {
        Timber.e(e, "getAllInstructors: Error")
        emptyList()
    }
}
