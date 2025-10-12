package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.DailyJournalEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for daily journal entries
 * Collection: users/{uid}/dailyJournal/{date}
 *
 * Privacy by design: All operations verify that request.auth.uid == uid
 */
interface DailyJournalRepository {

    /**
     * Saves or updates a daily journal entry
     * Document ID will be the entry's date (yyyy-MM-dd)
     *
     * @param entry Daily journal entry to save (uid must match auth.uid)
     * @return Result.success if saved, Result.failure if error
     */
    suspend fun saveDailyEntry(entry: DailyJournalEntry): Result<Unit>

    /**
     * Gets a journal entry for a specific date
     *
     * @param uid Firebase Auth UID
     * @param date Entry date in ISO format (yyyy-MM-dd)
     * @return Result with entry or null if not found
     */
    suspend fun getEntryByDate(uid: String, date: String): Result<DailyJournalEntry?>

    /**
     * Observes today's journal entry in real-time
     *
     * @param uid Firebase Auth UID
     * @return Reactive Flow of today's journal entry
     */
    fun getTodayEntry(uid: String): Flow<DailyJournalEntry?>

    /**
     * Observes all journal entries for a specific month
     * Returns entries ordered by date DESC
     *
     * @param uid Firebase Auth UID
     * @param yearMonth Year and month in format "yyyy-MM"
     * @return Reactive Flow of journal entries for the month
     */
    fun observeEntriesForMonth(uid: String, yearMonth: String): Flow<List<DailyJournalEntry>>

    /**
     * Gets recent journal entries
     *
     * @param uid Firebase Auth UID
     * @param limit Maximum number of entries to return
     * @return Result with list of recent entries
     */
    suspend fun getRecentEntries(uid: String, limit: Int = 10): Result<List<DailyJournalEntry>>

    /**
     * Deletes a journal entry
     *
     * @param uid Firebase Auth UID
     * @param date Entry date in ISO format (yyyy-MM-dd)
     * @return Result.success if deleted, Result.failure if error
     */
    suspend fun deleteEntry(uid: String, date: String): Result<Unit>

    /**
     * Gets all entries with a specific mood
     *
     * @param uid Firebase Auth UID
     * @param mood Mood type ("happy", "neutral", "sad", "frustrated")
     * @param limit Maximum number of entries to return
     * @return Result with list of entries matching the mood
     */
    suspend fun getEntriesByMood(uid: String, mood: String, limit: Int = 20): Result<List<DailyJournalEntry>>

    /**
     * Gets total count of journal entries for a user
     *
     * @param uid Firebase Auth UID
     * @return Total number of journal entries
     */
    suspend fun getTotalEntryCount(uid: String): Int

    /**
     * Gets count of entries for the current month
     *
     * @param uid Firebase Auth UID
     * @return Number of entries this month
     */
    suspend fun getThisMonthEntryCount(uid: String): Int
}
