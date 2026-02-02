package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.model.DailyJournalEntry
import com.ora.wellbeing.domain.repository.DailyJournalRepository
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
 * Implementation of DailyJournalRepository using Firestore
 * Collection structure: users/{uid}/dailyJournal/{date}
 */
@Singleton
class DailyJournalRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyJournalRepository {

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_DAILY_JOURNAL = "dailyJournal"
        private const val FIELD_DATE = "date"
        private const val FIELD_MOOD = "mood"
        private const val FIELD_UID = "uid"
    }

    /**
     * Gets reference to user's daily journal collection
     */
    private fun getUserJournalCollection(uid: String) =
        firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_DAILY_JOURNAL)

    override suspend fun saveDailyEntry(entry: DailyJournalEntry): Result<Unit> {
        return try {
            Timber.d("saveDailyEntry: Saving entry for date=${entry.date}, uid=${entry.uid}")

            // Use date as document ID for easy retrieval
            getUserJournalCollection(entry.uid)
                .document(entry.date)
                .set(entry)
                .await()

            Timber.i("saveDailyEntry: Successfully saved entry for ${entry.date}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "saveDailyEntry: Error saving entry for ${entry.date}")
            Result.failure(e)
        }
    }

    override suspend fun getEntryByDate(uid: String, date: String): Result<DailyJournalEntry?> {
        return try {
            Timber.d("getEntryByDate: Fetching entry for date=$date, uid=$uid")

            val snapshot = getUserJournalCollection(uid)
                .document(date)
                .get()
                .await()

            val entry = snapshot.toObject(DailyJournalEntry::class.java)
            Timber.d("getEntryByDate: Entry ${if (entry != null) "found" else "not found"} for $date")

            Result.success(entry)
        } catch (e: Exception) {
            Timber.e(e, "getEntryByDate: Error fetching entry for $date")
            Result.failure(e)
        }
    }

    override fun getTodayEntry(uid: String): Flow<DailyJournalEntry?> = callbackFlow {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        Timber.d("getTodayEntry: Observing entry for today=$today, uid=$uid")

        val listener = getUserJournalCollection(uid)
            .document(today)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getTodayEntry: Error observing today's entry")
                    close(error)
                    return@addSnapshotListener
                }

                val entry = snapshot?.toObject(DailyJournalEntry::class.java)
                Timber.d("getTodayEntry: Emitting ${if (entry != null) "entry" else "null"} for today")
                trySend(entry)
            }

        awaitClose {
            Timber.d("getTodayEntry: Closing listener for today's entry")
            listener.remove()
        }
    }

    override fun observeEntriesForMonth(uid: String, yearMonth: String): Flow<List<DailyJournalEntry>> = callbackFlow {
        // Calculate start and end dates for the month
        val startDate = "$yearMonth-01"
        val endDate = try {
            val localDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE)
            val lastDay = localDate.lengthOfMonth()
            "$yearMonth-$lastDay"
        } catch (e: Exception) {
            Timber.e(e, "observeEntriesForMonth: Invalid yearMonth format: $yearMonth")
            close(e)
            return@callbackFlow
        }

        Timber.d("observeEntriesForMonth: Observing entries from $startDate to $endDate")

        val listener = getUserJournalCollection(uid)
            .whereGreaterThanOrEqualTo(FIELD_DATE, startDate)
            .whereLessThanOrEqualTo(FIELD_DATE, endDate)
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "observeEntriesForMonth: Error observing month entries")
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.toObjects(DailyJournalEntry::class.java) ?: emptyList()
                Timber.d("observeEntriesForMonth: Emitting ${entries.size} entries for $yearMonth")
                trySend(entries)
            }

        awaitClose {
            Timber.d("observeEntriesForMonth: Closing listener for month $yearMonth")
            listener.remove()
        }
    }

    override suspend fun getRecentEntries(uid: String, limit: Int): Result<List<DailyJournalEntry>> {
        return try {
            Timber.d("getRecentEntries: Fetching $limit recent entries for uid=$uid")

            val snapshot = getUserJournalCollection(uid)
                .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val entries = snapshot.toObjects(DailyJournalEntry::class.java)
            Timber.d("getRecentEntries: Found ${entries.size} recent entries")

            Result.success(entries)
        } catch (e: Exception) {
            Timber.e(e, "getRecentEntries: Error fetching recent entries")
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(uid: String, date: String): Result<Unit> {
        return try {
            Timber.d("deleteEntry: Deleting entry for date=$date, uid=$uid")

            getUserJournalCollection(uid)
                .document(date)
                .delete()
                .await()

            Timber.i("deleteEntry: Successfully deleted entry for $date")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "deleteEntry: Error deleting entry for $date")
            Result.failure(e)
        }
    }

    override suspend fun getEntriesByMood(uid: String, mood: String, limit: Int): Result<List<DailyJournalEntry>> {
        return try {
            Timber.d("getEntriesByMood: Fetching entries with mood=$mood, limit=$limit")

            val snapshot = getUserJournalCollection(uid)
                .whereEqualTo(FIELD_MOOD, mood)
                .orderBy(FIELD_DATE, Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val entries = snapshot.toObjects(DailyJournalEntry::class.java)
            Timber.d("getEntriesByMood: Found ${entries.size} entries with mood=$mood")

            Result.success(entries)
        } catch (e: Exception) {
            Timber.e(e, "getEntriesByMood: Error fetching entries by mood")
            Result.failure(e)
        }
    }

    override suspend fun getTotalEntryCount(uid: String): Int {
        return try {
            Timber.d("getTotalEntryCount: Counting total entries for uid=$uid")

            val snapshot = getUserJournalCollection(uid)
                .get()
                .await()

            val count = snapshot.size()
            Timber.d("getTotalEntryCount: Found $count total entries")
            count
        } catch (e: Exception) {
            Timber.e(e, "getTotalEntryCount: Error counting entries")
            0
        }
    }

    override suspend fun getThisMonthEntryCount(uid: String): Int {
        return try {
            val now = LocalDate.now()
            val yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val startDate = "$yearMonth-01"
            val endDate = "$yearMonth-${now.lengthOfMonth()}"

            Timber.d("getThisMonthEntryCount: Counting entries from $startDate to $endDate")

            val snapshot = getUserJournalCollection(uid)
                .whereGreaterThanOrEqualTo(FIELD_DATE, startDate)
                .whereLessThanOrEqualTo(FIELD_DATE, endDate)
                .get()
                .await()

            val count = snapshot.size()
            Timber.d("getThisMonthEntryCount: Found $count entries this month")
            count
        } catch (e: Exception) {
            Timber.e(e, "getThisMonthEntryCount: Error counting month entries")
            0
        }
    }
}
