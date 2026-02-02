package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.UserProgram
import com.ora.wellbeing.data.service.EmailNotificationService
import com.ora.wellbeing.domain.repository.UserProgramRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgramRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val emailNotificationService: EmailNotificationService
) : UserProgramRepository {

    companion object {
        private const val COLLECTION_USER_PROGRAMS = "user_programs"
        private const val SUBCOLLECTION_ENROLLED = "enrolled"
        private const val COLLECTION_PROGRAMS = "programs"
    }

    // Coroutine scope for fire-and-forget email operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun getEnrolledPrograms(uid: String): Flow<List<UserProgram>> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .orderBy("startedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getEnrolledPrograms: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull {
                            try {
                                it.toObject(UserProgram::class.java)
                            } catch (e: Exception) {
                                Timber.e(e, "Error parsing UserProgram: ${it.id}")
                                null
                            }
                        }
                        trySend(programs)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getActivePrograms(uid: String): Flow<List<UserProgram>> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .whereEqualTo("isCompleted", false)
            .orderBy("lastSessionAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getActivePrograms: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull {
                            try {
                                it.toObject(UserProgram::class.java)
                            } catch (e: Exception) {
                                Timber.e(e, "Error parsing UserProgram: ${it.id}")
                                null
                            }
                        }
                        trySend(programs)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getCompletedPrograms(uid: String): Flow<List<UserProgram>> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .whereEqualTo("isCompleted", true)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getCompletedPrograms: Error")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val programs = snapshot.documents.mapNotNull {
                            try {
                                it.toObject(UserProgram::class.java)
                            } catch (e: Exception) {
                                Timber.e(e, "Error parsing UserProgram: ${it.id}")
                                null
                            }
                        }
                        trySend(programs)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getUserProgram(uid: String, programId: String): Flow<UserProgram?> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(programId.isNotBlank()) { "Program ID cannot be blank" }

        val listenerRegistration = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .document(programId)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getUserProgram: Error")
                    }
                    snapshot != null -> {
                        val program = if (snapshot.exists()) {
                            try {
                                snapshot.toObject(UserProgram::class.java)
                            } catch (e: Exception) {
                                Timber.e(e, "Error parsing UserProgram: ${snapshot.id}")
                                null
                            }
                        } else null
                        trySend(program)
                    }
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun enrollInProgram(uid: String, programId: String, totalDays: Int): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(programId.isNotBlank()) { "Program ID cannot be blank" }
        require(totalDays > 0) { "Total days must be > 0" }

        val userProgram = UserProgram.createEnrollment(uid, programId, totalDays)

        firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .document(programId)
            .set(userProgram)
            .await()

        Timber.i("enrollInProgram: Enrolled $uid in $programId")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "enrollInProgram: Error ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "enrollInProgram: Error")
        Result.failure(e)
    }

    override suspend fun updateProgress(uid: String, programId: String, currentDay: Int, completedSessionId: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(programId.isNotBlank()) { "Program ID cannot be blank" }

        firestore.runTransaction { transaction ->
            val docRef = firestore
                .collection(COLLECTION_USER_PROGRAMS)
                .document(uid)
                .collection(SUBCOLLECTION_ENROLLED)
                .document(programId)

            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("User program not found")
            }

            val userProgram = snapshot.toObject(UserProgram::class.java)
                ?: throw IllegalStateException("Cannot parse user program")

            // Update progress directly on the model
            userProgram.currentDay = currentDay
            userProgram.lastSessionAt = Timestamp.now()
            userProgram.updatedAt = Timestamp.now()

            transaction.set(docRef, userProgram)
        }.await()

        Timber.i("updateProgress: Updated $uid/$programId to day $currentDay")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "updateProgress: Error")
        Result.failure(e)
    }

    override suspend fun completeProgram(uid: String, programId: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(programId.isNotBlank()) { "Program ID cannot be blank" }

        firestore.runTransaction { transaction ->
            val docRef = firestore
                .collection(COLLECTION_USER_PROGRAMS)
                .document(uid)
                .collection(SUBCOLLECTION_ENROLLED)
                .document(programId)

            val snapshot = transaction.get(docRef)
            if (!snapshot.exists()) {
                throw IllegalStateException("User program not found")
            }

            val userProgram = snapshot.toObject(UserProgram::class.java)
                ?: throw IllegalStateException("Cannot parse user program")

            // Mark as complete directly on the model
            userProgram.isCompleted = true
            userProgram.completedAt = Timestamp.now()
            userProgram.currentDay = userProgram.totalDays
            userProgram.updatedAt = Timestamp.now()

            transaction.set(docRef, userProgram)
        }.await()

        Timber.i("completeProgram: Completed $uid/$programId")

        // Send program complete email (fire-and-forget, silent failure)
        scope.launch {
            try {
                // Fetch the program title from the programs collection
                val programTitle = fetchProgramTitle(programId)
                emailNotificationService.sendProgramCompleteEmail(
                    uid = uid,
                    programId = programId,
                    programTitle = programTitle
                )
                Timber.d("completeProgram: Program complete email triggered for $programId")
            } catch (e: Exception) {
                Timber.e(e, "completeProgram: Failed to send program complete email")
            }
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "completeProgram: Error")
        Result.failure(e)
    }

    /**
     * Fetches the program title from the programs collection.
     * Returns a default title if the program is not found or on error.
     */
    private suspend fun fetchProgramTitle(programId: String): String {
        return try {
            val snapshot = firestore
                .collection(COLLECTION_PROGRAMS)
                .document(programId)
                .get()
                .await()

            snapshot.getString("title") ?: "Programme"
        } catch (e: Exception) {
            Timber.w(e, "fetchProgramTitle: Could not fetch title for $programId, using default")
            "Programme"
        }
    }

    override suspend fun unenrollFromProgram(uid: String, programId: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(programId.isNotBlank()) { "Program ID cannot be blank" }

        firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .document(programId)
            .delete()
            .await()

        Timber.i("unenrollFromProgram: Unenrolled $uid from $programId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "unenrollFromProgram: Error")
        Result.failure(e)
    }

    override suspend fun getEnrolledProgramCount(uid: String): Int = try {
        val snapshot = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .get()
            .await()

        snapshot.size()
    } catch (e: Exception) {
        Timber.e(e, "getEnrolledProgramCount: Error")
        0
    }

    override suspend fun getCompletedProgramCount(uid: String): Int = try {
        val snapshot = firestore
            .collection(COLLECTION_USER_PROGRAMS)
            .document(uid)
            .collection(SUBCOLLECTION_ENROLLED)
            .whereEqualTo("isCompleted", true)
            .get()
            .await()

        snapshot.size()
    } catch (e: Exception) {
        Timber.e(e, "getCompletedProgramCount: Error")
        0
    }
}
