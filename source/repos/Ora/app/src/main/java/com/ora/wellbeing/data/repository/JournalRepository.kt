package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.JournalDao
import com.ora.wellbeing.data.local.entities.JournalEntry
import com.ora.wellbeing.data.local.entities.Mood
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao
) {

    fun getJournalEntriesFlow(userId: String): Flow<List<JournalEntry>> =
        journalDao.getJournalEntriesFlow(userId)

    suspend fun getRecentJournalEntries(userId: String, limit: Int = 10): List<JournalEntry> =
        journalDao.getRecentJournalEntries(userId, limit)

    suspend fun getJournalEntryByDate(userId: String, date: LocalDate): JournalEntry? =
        journalDao.getJournalEntryByDate(userId, date)

    fun getJournalEntryByDateFlow(userId: String, date: LocalDate): Flow<JournalEntry?> =
        journalDao.getJournalEntryByDateFlow(userId, date)

    suspend fun getJournalEntriesInRange(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<JournalEntry> =
        journalDao.getJournalEntriesInRange(userId, startDate, endDate)

    suspend fun getRecentMoods(userId: String, days: Int = 7): List<Mood> =
        journalDao.getRecentMoods(userId, days)

    suspend fun getTotalEntriesCount(userId: String): Int =
        journalDao.getTotalEntriesCount(userId)

    suspend fun getEntriesCountSince(userId: String, since: LocalDate): Int =
        journalDao.getEntriesCountSince(userId, since)

    suspend fun saveJournalEntry(
        userId: String,
        date: LocalDate,
        gratitude1: String,
        gratitude2: String,
        gratitude3: String,
        mood: Mood,
        dayStory: String? = null
    ): JournalEntry {
        val existingEntry = journalDao.getJournalEntryByDate(userId, date)

        val entry = if (existingEntry != null) {
            existingEntry.copy(
                gratitude1 = gratitude1,
                gratitude2 = gratitude2,
                gratitude3 = gratitude3,
                mood = mood,
                dayStory = dayStory,
                updatedAt = LocalDateTime.now()
            )
        } else {
            JournalEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                gratitude1 = gratitude1,
                gratitude2 = gratitude2,
                gratitude3 = gratitude3,
                mood = mood,
                dayStory = dayStory
            )
        }

        journalDao.insertJournalEntry(entry)
        return entry
    }

    suspend fun updateJournalEntry(entry: JournalEntry) {
        val updatedEntry = entry.copy(updatedAt = LocalDateTime.now())
        journalDao.updateJournalEntry(updatedEntry)
    }

    suspend fun deleteJournalEntry(userId: String, date: LocalDate) {
        journalDao.deleteJournalEntryByDate(userId, date)
    }

    suspend fun deleteAllUserEntries(userId: String) {
        journalDao.deleteAllUserEntries(userId)
    }

    suspend fun hasEntryForToday(userId: String): Boolean {
        return getJournalEntryByDate(userId, LocalDate.now()) != null
    }

    suspend fun getWeeklyMoodStats(userId: String): Map<Mood, Int> {
        val startOfWeek = LocalDate.now().minusDays(7)
        val entries = getJournalEntriesInRange(userId, startOfWeek, LocalDate.now())
        return entries.groupingBy { it.mood }.eachCount()
    }

    suspend fun getCurrentStreak(userId: String): Int {
        val allEntries = getRecentJournalEntries(userId, 365) // Check last year
        if (allEntries.isEmpty()) return 0

        var streak = 0
        var currentDate = LocalDate.now()

        for (entry in allEntries.sortedByDescending { it.date }) {
            if (entry.date == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }

        return streak
    }
}