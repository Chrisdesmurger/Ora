package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.JournalDao
import com.ora.wellbeing.data.local.entities.JournalEntry
import com.ora.wellbeing.data.local.entities.Mood
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class JournalRepositoryTest {

    @Mock
    private lateinit var journalDao: JournalDao

    private lateinit var journalRepository: JournalRepository

    private val testUserId = "test_user_123"
    private val testDate = LocalDate.now()
    private val testEntry = JournalEntry(
        id = "entry_123",
        userId = testUserId,
        date = testDate,
        gratitude1 = "Ma famille",
        gratitude2 = "Ma santé",
        gratitude3 = "Cette belle journée",
        mood = Mood.HAPPY,
        dayStory = "Une journée productive et relaxante",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    @Before
    fun setup() {
        journalRepository = JournalRepository(journalDao)
    }

    @Test
    fun `getJournalEntriesFlow returns flow from dao`() = runTest {
        // Given
        val entries = listOf(testEntry)
        whenever(journalDao.getJournalEntriesFlow(testUserId)).thenReturn(flowOf(entries))

        // When
        val flow = journalRepository.getJournalEntriesFlow(testUserId)

        // Then
        flow.collect { result ->
            assertEquals(entries, result)
        }
        verify(journalDao).getJournalEntriesFlow(testUserId)
    }

    @Test
    fun `getRecentJournalEntries returns entries from dao`() = runTest {
        // Given
        val entries = listOf(testEntry)
        whenever(journalDao.getRecentJournalEntries(testUserId, 10)).thenReturn(entries)

        // When
        val result = journalRepository.getRecentJournalEntries(testUserId, 10)

        // Then
        assertEquals(entries, result)
        verify(journalDao).getRecentJournalEntries(testUserId, 10)
    }

    @Test
    fun `getJournalEntryByDate returns entry from dao`() = runTest {
        // Given
        whenever(journalDao.getJournalEntryByDate(testUserId, testDate)).thenReturn(testEntry)

        // When
        val result = journalRepository.getJournalEntryByDate(testUserId, testDate)

        // Then
        assertEquals(testEntry, result)
        verify(journalDao).getJournalEntryByDate(testUserId, testDate)
    }

    @Test
    fun `getJournalEntryByDateFlow returns flow from dao`() = runTest {
        // Given
        whenever(journalDao.getJournalEntryByDateFlow(testUserId, testDate)).thenReturn(flowOf(testEntry))

        // When
        val flow = journalRepository.getJournalEntryByDateFlow(testUserId, testDate)

        // Then
        flow.collect { result ->
            assertEquals(testEntry, result)
        }
        verify(journalDao).getJournalEntryByDateFlow(testUserId, testDate)
    }

    @Test
    fun `getJournalEntriesInRange returns entries from dao`() = runTest {
        // Given
        val startDate = testDate.minusDays(7)
        val endDate = testDate
        val entries = listOf(testEntry)
        whenever(journalDao.getJournalEntriesInRange(testUserId, startDate, endDate)).thenReturn(entries)

        // When
        val result = journalRepository.getJournalEntriesInRange(testUserId, startDate, endDate)

        // Then
        assertEquals(entries, result)
        verify(journalDao).getJournalEntriesInRange(testUserId, startDate, endDate)
    }

    @Test
    fun `saveJournalEntry creates new entry when none exists`() = runTest {
        // Given
        whenever(journalDao.getJournalEntryByDate(testUserId, testDate)).thenReturn(null)
        val entryCaptor = argumentCaptor<JournalEntry>()

        // When
        val result = journalRepository.saveJournalEntry(
            userId = testUserId,
            date = testDate,
            gratitude1 = "Test 1",
            gratitude2 = "Test 2",
            gratitude3 = "Test 3",
            mood = Mood.GRATEFUL,
            dayStory = "Test story"
        )

        // Then
        verify(journalDao).insertJournalEntry(entryCaptor.capture())
        val capturedEntry = entryCaptor.firstValue
        assertEquals(testUserId, capturedEntry.userId)
        assertEquals(testDate, capturedEntry.date)
        assertEquals("Test 1", capturedEntry.gratitude1)
        assertEquals("Test 2", capturedEntry.gratitude2)
        assertEquals("Test 3", capturedEntry.gratitude3)
        assertEquals(Mood.GRATEFUL, capturedEntry.mood)
        assertEquals("Test story", capturedEntry.dayStory)
        assertNotNull(capturedEntry.id)
    }

    @Test
    fun `saveJournalEntry updates existing entry when one exists`() = runTest {
        // Given
        whenever(journalDao.getJournalEntryByDate(testUserId, testDate)).thenReturn(testEntry)
        val entryCaptor = argumentCaptor<JournalEntry>()

        // When
        val result = journalRepository.saveJournalEntry(
            userId = testUserId,
            date = testDate,
            gratitude1 = "Updated 1",
            gratitude2 = "Updated 2",
            gratitude3 = "Updated 3",
            mood = Mood.PEACEFUL,
            dayStory = "Updated story"
        )

        // Then
        verify(journalDao).insertJournalEntry(entryCaptor.capture())
        val capturedEntry = entryCaptor.firstValue
        assertEquals(testEntry.id, capturedEntry.id) // Same ID
        assertEquals("Updated 1", capturedEntry.gratitude1)
        assertEquals("Updated 2", capturedEntry.gratitude2)
        assertEquals("Updated 3", capturedEntry.gratitude3)
        assertEquals(Mood.PEACEFUL, capturedEntry.mood)
        assertEquals("Updated story", capturedEntry.dayStory)
    }

    @Test
    fun `updateJournalEntry updates entry with new timestamp`() = runTest {
        // Given
        val entryCaptor = argumentCaptor<JournalEntry>()

        // When
        journalRepository.updateJournalEntry(testEntry)

        // Then
        verify(journalDao).updateJournalEntry(entryCaptor.capture())
        val capturedEntry = entryCaptor.firstValue
        assertEquals(testEntry.id, capturedEntry.id)
        // updatedAt should be different (more recent)
        assertTrue(capturedEntry.updatedAt.isAfter(testEntry.updatedAt) ||
                  capturedEntry.updatedAt.isEqual(testEntry.updatedAt))
    }

    @Test
    fun `deleteJournalEntry calls dao deleteJournalEntryByDate`() = runTest {
        // When
        journalRepository.deleteJournalEntry(testUserId, testDate)

        // Then
        verify(journalDao).deleteJournalEntryByDate(testUserId, testDate)
    }

    @Test
    fun `deleteAllUserEntries calls dao deleteAllUserEntries`() = runTest {
        // When
        journalRepository.deleteAllUserEntries(testUserId)

        // Then
        verify(journalDao).deleteAllUserEntries(testUserId)
    }

    @Test
    fun `hasEntryForToday returns true when entry exists`() = runTest {
        // Given
        val today = LocalDate.now()
        whenever(journalDao.getJournalEntryByDate(testUserId, today)).thenReturn(testEntry)

        // When
        val result = journalRepository.hasEntryForToday(testUserId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasEntryForToday returns false when no entry exists`() = runTest {
        // Given
        val today = LocalDate.now()
        whenever(journalDao.getJournalEntryByDate(testUserId, today)).thenReturn(null)

        // When
        val result = journalRepository.hasEntryForToday(testUserId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getWeeklyMoodStats returns correct mood counts`() = runTest {
        // Given
        val startOfWeek = LocalDate.now().minusDays(7)
        val entries = listOf(
            testEntry.copy(mood = Mood.HAPPY),
            testEntry.copy(mood = Mood.HAPPY),
            testEntry.copy(mood = Mood.GRATEFUL),
            testEntry.copy(mood = Mood.PEACEFUL)
        )
        whenever(journalDao.getJournalEntriesInRange(testUserId, startOfWeek, LocalDate.now()))
            .thenReturn(entries)

        // When
        val result = journalRepository.getWeeklyMoodStats(testUserId)

        // Then
        assertEquals(2, result[Mood.HAPPY])
        assertEquals(1, result[Mood.GRATEFUL])
        assertEquals(1, result[Mood.PEACEFUL])
    }

    @Test
    fun `getCurrentStreak returns correct streak count`() = runTest {
        // Given
        val today = LocalDate.now()
        val entries = listOf(
            testEntry.copy(date = today),
            testEntry.copy(date = today.minusDays(1)),
            testEntry.copy(date = today.minusDays(2)),
            testEntry.copy(date = today.minusDays(4)) // Gap here breaks streak
        )
        whenever(journalDao.getRecentJournalEntries(testUserId, 365)).thenReturn(entries)

        // When
        val result = journalRepository.getCurrentStreak(testUserId)

        // Then
        assertEquals(3, result) // Should be 3 consecutive days
    }

    @Test
    fun `getCurrentStreak returns zero when no entries`() = runTest {
        // Given
        whenever(journalDao.getRecentJournalEntries(testUserId, 365)).thenReturn(emptyList())

        // When
        val result = journalRepository.getCurrentStreak(testUserId)

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `getTotalEntriesCount returns count from dao`() = runTest {
        // Given
        whenever(journalDao.getTotalEntriesCount(testUserId)).thenReturn(42)

        // When
        val result = journalRepository.getTotalEntriesCount(testUserId)

        // Then
        assertEquals(42, result)
        verify(journalDao).getTotalEntriesCount(testUserId)
    }

    @Test
    fun `getEntriesCountSince returns count from dao`() = runTest {
        // Given
        val since = LocalDate.now().minusDays(30)
        whenever(journalDao.getEntriesCountSince(testUserId, since)).thenReturn(15)

        // When
        val result = journalRepository.getEntriesCountSince(testUserId, since)

        // Then
        assertEquals(15, result)
        verify(journalDao).getEntriesCountSince(testUserId, since)
    }

    @Test
    fun `getRecentMoods returns moods from dao`() = runTest {
        // Given
        val moods = listOf(Mood.HAPPY, Mood.GRATEFUL, Mood.PEACEFUL)
        whenever(journalDao.getRecentMoods(testUserId, 7)).thenReturn(moods)

        // When
        val result = journalRepository.getRecentMoods(testUserId, 7)

        // Then
        assertEquals(moods, result)
        verify(journalDao).getRecentMoods(testUserId, 7)
    }
}