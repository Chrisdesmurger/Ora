package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.JournalEntry
import com.ora.wellbeing.data.local.entities.Mood
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentJournalEntries(userId: String, limit: Int = 10): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date = :date")
    suspend fun getJournalEntryByDate(userId: String, date: LocalDate): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date = :date")
    fun getJournalEntryByDateFlow(userId: String, date: LocalDate): Flow<JournalEntry?>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getJournalEntriesInRange(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<JournalEntry>

    @Query("SELECT mood FROM journal_entries WHERE userId = :userId ORDER BY date DESC LIMIT :days")
    suspend fun getRecentMoods(userId: String, days: Int = 7): List<Mood>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId")
    suspend fun getTotalEntriesCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM journal_entries WHERE userId = :userId AND date >= :since")
    suspend fun getEntriesCountSince(userId: String, since: LocalDate): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Update
    suspend fun updateJournalEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteJournalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE userId = :userId AND date = :date")
    suspend fun deleteJournalEntryByDate(userId: String, date: LocalDate)

    @Query("DELETE FROM journal_entries WHERE userId = :userId")
    suspend fun deleteAllUserEntries(userId: String)
}