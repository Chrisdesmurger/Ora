package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.model.Program
import com.ora.wellbeing.domain.repository.ProgramRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore implementation of ProgramRepository (read-only)
 * Collection: programs/{programId}
 * Offline-first with persistent cache, real-time sync
 */
@Singleton
class ProgramRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProgramRepository {

    companion object {
        private const val COLLECTION_PROGRAMS = "programs"
    }

    override fun getAllPrograms(): Flow<List<Program>> = callbackFlow {
        Timber.d("getAllPrograms: Listening to all programs")

        val listenerRegistration = firestore
            .collection(COLLECTION_PROGRAMS)
            .whereEqualTo("isActive", true)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getAllPrograms: Error snapshot listener")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull { it.toObject(Program::class.java) }
                        Timber.d("getAllPrograms: Received ${programs.size} programs")
                        trySend(programs)
                    }
                }
            }

        awaitClose {
            Timber.d("getAllPrograms: Removing listener")
            listenerRegistration.remove()
        }
    }

    override fun getProgramsByCategory(category: String): Flow<List<Program>> = callbackFlow {
        require(category.isNotBlank()) { "Category cannot be blank" }

        Timber.d("getProgramsByCategory: Listening to programs in category=$category")

        val listenerRegistration = firestore
            .collection(COLLECTION_PROGRAMS)
            .whereEqualTo("isActive", true)
            .whereEqualTo("category", category)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getProgramsByCategory: Error snapshot listener")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull { it.toObject(Program::class.java) }
                        Timber.d("getProgramsByCategory: Received ${programs.size} programs")
                        trySend(programs)
                    }
                }
            }

        awaitClose {
            Timber.d("getProgramsByCategory: Removing listener")
            listenerRegistration.remove()
        }
    }

    override fun getProgram(programId: String): Flow<Program?> = callbackFlow {
        require(programId.isNotBlank()) { "Program ID cannot be blank" }

        Timber.d("getProgram: Listening to program $programId")

        val listenerRegistration = firestore
            .collection(COLLECTION_PROGRAMS)
            .document(programId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getProgram: Error snapshot listener")
                        when (error.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                Timber.e("getProgram: Permission denied")
                            }
                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                Timber.w("getProgram: Network unavailable - using cache")
                            }
                            else -> {
                                Timber.e("getProgram: Unexpected error ${error.code}")
                            }
                        }
                    }
                    snapshot != null -> {
                        val program = if (snapshot.exists()) {
                            snapshot.toObject(Program::class.java)
                        } else {
                            Timber.w("getProgram: Program $programId not found")
                            null
                        }
                        trySend(program)
                    }
                }
            }

        awaitClose {
            Timber.d("getProgram: Removing listener for $programId")
            listenerRegistration.remove()
        }
    }

    override fun getPopularPrograms(limit: Int): Flow<List<Program>> = callbackFlow {
        require(limit > 0) { "Limit must be > 0" }

        Timber.d("getPopularPrograms: Listening to popular programs (limit=$limit)")

        val listenerRegistration = firestore
            .collection(COLLECTION_PROGRAMS)
            .whereEqualTo("isActive", true)
            .orderBy("participantCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getPopularPrograms: Error snapshot listener")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull { it.toObject(Program::class.java) }
                        Timber.d("getPopularPrograms: Received ${programs.size} programs")
                        trySend(programs)
                    }
                }
            }

        awaitClose {
            Timber.d("getPopularPrograms: Removing listener")
            listenerRegistration.remove()
        }
    }

    override fun getProgramsByLevel(level: String): Flow<List<Program>> = callbackFlow {
        require(level.isNotBlank()) { "Level cannot be blank" }

        Timber.d("getProgramsByLevel: Listening to programs with level=$level")

        val listenerRegistration = firestore
            .collection(COLLECTION_PROGRAMS)
            .whereEqualTo("isActive", true)
            .whereEqualTo("level", level)
            .orderBy("rating", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getProgramsByLevel: Error snapshot listener")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull { it.toObject(Program::class.java) }
                        Timber.d("getProgramsByLevel: Received ${programs.size} programs")
                        trySend(programs)
                    }
                }
            }

        awaitClose {
            Timber.d("getProgramsByLevel: Removing listener")
            listenerRegistration.remove()
        }
    }

    override suspend fun getTotalProgramCount(): Int = try {
        Timber.d("getTotalProgramCount: Getting count")

        val snapshot = firestore
            .collection(COLLECTION_PROGRAMS)
            .whereEqualTo("isActive", true)
            .get()
            .await()

        val count = snapshot.size()
        Timber.d("getTotalProgramCount: Count = $count")
        count
    } catch (e: Exception) {
        Timber.e(e, "getTotalProgramCount: Error getting count")
        0
    }
}
