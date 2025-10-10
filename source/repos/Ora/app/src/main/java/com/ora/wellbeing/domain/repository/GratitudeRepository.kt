package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.GratitudeEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for gratitude journal entries
 * Collection: gratitudes/{uid}/entries/{date}
 *
 * Privacy by design: All operations verify that request.auth.uid == uid
 * Offline-first: Flow returns cache if network error
 */
interface GratitudeRepository {

    /**
     * Observes today's gratitude entry in real-time
     * Returns Flow<GratitudeEntry?> which emits:
     * - null if today's entry doesn't exist
     * - GratitudeEntry if today's entry exists
     * - Cached data if offline
     *
     * @param uid Firebase Auth UID
     * @return Reactive Flow of today's gratitude entry
     */
    fun getTodayEntry(uid: String): Flow<GratitudeEntry?>

    /**
     * Observes recent gratitude entries in real-time
     * Returns entries ordered by date DESC (most recent first)
     *
     * @param uid Firebase Auth UID
     * @param limit Maximum number of entries to return
     * @return Reactive Flow of recent gratitude entries
     */
    fun getRecentEntries(uid: String, limit: Int = 10): Flow<List<GratitudeEntry>>

    /**
     * Observes gratitude entries within a date range
     *
     * @param uid Firebase Auth UID
     * @param startDate Start date in ISO format (yyyy-MM-dd)
     * @param endDate End date in ISO format (yyyy-MM-dd)
     * @return Reactive Flow of gratitude entries in the date range
     */
    fun getEntriesByDateRange(uid: String, startDate: String, endDate: String): Flow<List<GratitudeEntry>>

    /**
     * Creates a new gratitude entry
     * Document ID will be the entry's date (yyyy-MM-dd)
     *
     * @param entry Gratitude entry to create (uid must match auth.uid)
     * @return Result.success if created, Result.failure if error
     */
    suspend fun createEntry(entry: GratitudeEntry): Result<Unit>

    /**
     * Updates an existing gratitude entry
     *
     * @param entry Updated gratitude entry
     * @return Result.success if updated, Result.failure if error
     */
    suspend fun updateEntry(entry: GratitudeEntry): Result<Unit>

    /**
     * Deletes a gratitude entry
     *
     * @param uid Firebase Auth UID
     * @param date Entry date in ISO format (yyyy-MM-dd)
     * @return Result.success if deleted, Result.failure if error
     */
    suspend fun deleteEntry(uid: String, date: String): Result<Unit>

    /**
     * Calculates gratitude streak (consecutive days with entries)
     *
     * @param uid Firebase Auth UID
     * @return Current gratitude streak in days
     */
    suspend fun calculateStreak(uid: String): Int

    /**
     * Gets total count of gratitude entries for a user
     *
     * @param uid Firebase Auth UID
     * @return Total number of gratitude entries
     */
    suspend fun getTotalEntryCount(uid: String): Int
}
