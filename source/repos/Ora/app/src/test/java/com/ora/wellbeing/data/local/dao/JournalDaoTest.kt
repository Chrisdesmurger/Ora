package com.ora.wellbeing.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ora.wellbeing.data.local.database.OraDatabase
import com.ora.wellbeing.data.local.entities.JournalEntry
import com.ora.wellbeing.data.local.entities.Mood
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for JournalDao
 */
class JournalDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: OraDatabase
    private lateinit var journalDao: JournalDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            OraDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        journalDao = database.journalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveJournalEntry() = runTest {
        // Given
        val entry = createTestEntry("user1", LocalDate.now())

        // When
        journalDao.insertJournalEntry(entry)
        val retrieved = journalDao.getJournalEntryByDate("user1", LocalDate.now())

        // Then
        assertNotNull(retrieved)
        assertEquals(entry.userId, retrieved.userId)
        assertEquals(entry.gratitude1, retrieved.gratitude1)
    }

    @Test
    fun getUserJournalEntries() = runTest {
        // Given
        val entries = listOf(
            createTestEntry("user1", LocalDate.now()),
            createTestEntry("user1", LocalDate.now().minusDays(1)),
            createTestEntry("user1", LocalDate.now().minusDays(2))
        )
        entries.forEach { journalDao.insertJournalEntry(it) }

        // When
        val userEntries = journalDao.getJournalEntriesFlow("user1").first()

        // Then
        assertEquals(3, userEntries.size)
    }

    @Test
    fun getTotalEntriesCount() = runTest {
        // Given
        val entries = listOf(
            createTestEntry("user1", LocalDate.now()),
            createTestEntry("user1", LocalDate.now().minusDays(1)),
            createTestEntry("user1", LocalDate.now().minusDays(2))
        )
        entries.forEach { journalDao.insertJournalEntry(it) }

        // When
        val count = journalDao.getTotalEntriesCount("user1")

        // Then
        assertEquals(3, count)
    }

    @Test
    fun deleteJournalEntry() = runTest {
        // Given
        val entry = createTestEntry("user1", LocalDate.now())
        journalDao.insertJournalEntry(entry)

        // When
        journalDao.deleteJournalEntryByDate("user1", LocalDate.now())
        val retrieved = journalDao.getJournalEntryByDate("user1", LocalDate.now())

        // Then
        assertNull(retrieved)
    }

    @Test
    fun getEntriesCountSince() = runTest {
        // Given
        val entries = listOf(
            createTestEntry("user1", LocalDate.now()),
            createTestEntry("user1", LocalDate.now().minusDays(1)),
            createTestEntry("user1", LocalDate.now().minusDays(5))
        )
        entries.forEach { journalDao.insertJournalEntry(it) }

        // When
        val count = journalDao.getEntriesCountSince("user1", LocalDate.now().minusDays(3))

        // Then
        assertEquals(2, count)
    }

    private fun createTestEntry(
        userId: String,
        date: LocalDate
    ): JournalEntry {
        return JournalEntry(
            id = "${userId}_${date}",
            userId = userId,
            date = date,
            gratitude1 = "Test gratitude 1",
            gratitude2 = "Test gratitude 2",
            gratitude3 = "Test gratitude 3",
            mood = Mood.HAPPY,
            dayStory = "Test day story",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
