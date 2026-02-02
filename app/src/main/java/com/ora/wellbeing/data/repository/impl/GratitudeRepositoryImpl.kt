package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.GratitudeEntry
import com.ora.wellbeing.domain.repository.GratitudeRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore implementation of GratitudeRepository
 * Collection: gratitudes/{uid}/entries/{date}
 * Offline-first with persistent cache, real-time sync
 */
@Singleton
class GratitudeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : GratitudeRepository {

    companion object {
        private const val COLLECTION_GRATITUDES = "gratitudes"
        private const val SUBCOLLECTION_ENTRIES = "entries"
    }

    override fun getTodayEntry(uid: String): Flow<GratitudeEntry?> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        Timber.d("getTodayEntry: Listening to entry $uid/$today")

        val listenerRegistration = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(today)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getTodayEntry: Error snapshot listener")
                        when (error.code) {
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                                Timber.e("getTodayEntry: Permission denied - token expired?")
                            }
                            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                                Timber.w("getTodayEntry: Network unavailable - using cache")
                            }
                            else -> {
                                Timber.e("getTodayEntry: Unexpected error ${error.code}")
                            }
                        }
                        // Don't close flow, keep listener active
                    }
                    snapshot != null -> {
                        val entry = if (snapshot.exists()) {
                            snapshot.toObject(GratitudeEntry::class.java)
                        } else {
                            Timber.d("getTodayEntry: No entry for today")
                            null
                        }
                        trySend(entry)
                    }
                }
            }

        awaitClose {
            Timber.d("getTodayEntry: Removing listener for $uid")
            listenerRegistration.remove()
        }
    }

    override fun getRecentEntries(uid: String, limit: Int): Flow<List<GratitudeEntry>> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(limit > 0) { "Limit must be > 0" }

        Timber.d("getRecentEntries: Listening to entries for $uid (limit=$limit)")

        val listenerRegistration = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getRecentEntries: Error snapshot listener")
                        trySend(emptyList()) // Emit empty list on error
                    }
                    snapshot != null -> {
                        val entries = snapshot.documents.mapNotNull { it.toObject(GratitudeEntry::class.java) }
                        Timber.d("getRecentEntries: Received ${entries.size} entries")
                        trySend(entries)
                    }
                }
            }

        awaitClose {
            Timber.d("getRecentEntries: Removing listener for $uid")
            listenerRegistration.remove()
        }
    }

    override fun getEntriesByDateRange(uid: String, startDate: String, endDate: String): Flow<List<GratitudeEntry>> = callbackFlow {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(startDate.isNotBlank()) { "Start date cannot be blank" }
        require(endDate.isNotBlank()) { "End date cannot be blank" }

        Timber.d("getEntriesByDateRange: Listening to entries $uid from $startDate to $endDate")

        val listenerRegistration = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                when {
                    error != null -> {
                        Timber.e(error, "getEntriesByDateRange: Error snapshot listener")
                        trySend(emptyList())
                    }
                    snapshot != null -> {
                        val entries = snapshot.documents.mapNotNull { it.toObject(GratitudeEntry::class.java) }
                        Timber.d("getEntriesByDateRange: Received ${entries.size} entries")
                        trySend(entries)
                    }
                }
            }

        awaitClose {
            Timber.d("getEntriesByDateRange: Removing listener for $uid")
            listenerRegistration.remove()
        }
    }

    override suspend fun createEntry(entry: GratitudeEntry): Result<Unit> = try {
        require(entry.uid.isNotBlank()) { "UID cannot be blank" }
        require(entry.date.isNotBlank()) { "Date cannot be blank" }

        Timber.d("createEntry: Creating entry ${entry.uid}/${entry.date}")

        firestore
            .collection(COLLECTION_GRATITUDES)
            .document(entry.uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(entry.date)
            .set(entry)
            .await()

        Timber.i("createEntry: Entry created successfully")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "createEntry: Firestore error ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "createEntry: Unexpected error")
        Result.failure(e)
    }

    override suspend fun updateEntry(entry: GratitudeEntry): Result<Unit> = try {
        require(entry.uid.isNotBlank()) { "UID cannot be blank" }
        require(entry.date.isNotBlank()) { "Date cannot be blank" }

        Timber.d("updateEntry: Updating entry ${entry.uid}/${entry.date}")

        // updatedAt will be automatically set by @ServerTimestamp when using set()
        val updatedEntry = entry.copy(updatedAt = Timestamp.now())

        firestore
            .collection(COLLECTION_GRATITUDES)
            .document(entry.uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(entry.date)
            .set(updatedEntry)
            .await()

        Timber.i("updateEntry: Entry updated successfully")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "updateEntry: Firestore error ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "updateEntry: Unexpected error")
        Result.failure(e)
    }

    override suspend fun deleteEntry(uid: String, date: String): Result<Unit> = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(date.isNotBlank()) { "Date cannot be blank" }

        Timber.d("deleteEntry: Deleting entry $uid/$date")

        firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .document(date)
            .delete()
            .await()

        Timber.i("deleteEntry: Entry deleted successfully")
        Result.success(Unit)
    } catch (e: FirebaseFirestoreException) {
        Timber.e(e, "deleteEntry: Firestore error ${e.code}")
        Result.failure(e)
    } catch (e: Exception) {
        Timber.e(e, "deleteEntry: Unexpected error")
        Result.failure(e)
    }

    override suspend fun calculateStreak(uid: String): Int {
        return try {
            require(uid.isNotBlank()) { "UID cannot be blank" }

            Timber.d("calculateStreak: Calculating streak for $uid")

            // Get all entries ordered by date DESC
            val snapshot = firestore
                .collection(COLLECTION_GRATITUDES)
                .document(uid)
                .collection(SUBCOLLECTION_ENTRIES)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()

            val entries = snapshot.documents.mapNotNull { it.toObject(GratitudeEntry::class.java) }

            if (entries.isEmpty()) {
                Timber.d("calculateStreak: No entries found, streak = 0")
                0
            } else {
                // Check if there's an entry for today or yesterday
                val today = LocalDate.now()
                val mostRecentDate = LocalDate.parse(entries.first().date, DateTimeFormatter.ISO_LOCAL_DATE)

                // If most recent entry is more than 1 day old, streak is broken
                val daysSinceLastEntry = java.time.temporal.ChronoUnit.DAYS.between(mostRecentDate, today)
                if (daysSinceLastEntry > 1) {
                    Timber.d("calculateStreak: Last entry was $daysSinceLastEntry days ago, streak = 0")
                    0
                } else {
                    // Count consecutive days
                    var streak = 1
                    var expectedDate = mostRecentDate.minusDays(1)

                    for (i in 1 until entries.size) {
                        val currentDate = LocalDate.parse(entries[i].date, DateTimeFormatter.ISO_LOCAL_DATE)
                        if (currentDate == expectedDate) {
                            streak++
                            expectedDate = expectedDate.minusDays(1)
                        } else {
                            break // Streak broken
                        }
                    }

                    Timber.d("calculateStreak: Streak = $streak days")
                    streak
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "calculateStreak: Error calculating streak")
            0
        }
    }

    override suspend fun getTotalEntryCount(uid: String): Int = try {
        require(uid.isNotBlank()) { "UID cannot be blank" }

        Timber.d("getTotalEntryCount: Getting count for $uid")

        val snapshot = firestore
            .collection(COLLECTION_GRATITUDES)
            .document(uid)
            .collection(SUBCOLLECTION_ENTRIES)
            .get()
            .await()

        val count = snapshot.size()
        Timber.d("getTotalEntryCount: Count = $count")
        count
    } catch (e: Exception) {
        Timber.e(e, "getTotalEntryCount: Error getting count")
        0
    }
}
