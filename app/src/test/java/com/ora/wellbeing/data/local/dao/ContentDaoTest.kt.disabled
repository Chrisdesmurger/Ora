package com.ora.wellbeing.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ora.wellbeing.data.local.database.OraDatabase
import com.ora.wellbeing.data.local.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for ContentDao
 */
class ContentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: OraDatabase
    private lateinit var contentDao: ContentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            OraDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        contentDao = database.contentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveContent() = runTest {
        // Given
        val content = createTestContent("content1")

        // When
        contentDao.insertContent(content)
        val retrieved = contentDao.getContentById("content1")

        // Then
        assertNotNull(retrieved)
        assertEquals(content.id, retrieved.id)
        assertEquals(content.title, retrieved.title)
    }

    @Test
    fun insertMultipleContent() = runTest {
        // Given
        val contentList = listOf(
            createTestContent("content1", "Meditation"),
            createTestContent("content2", "Yoga"),
            createTestContent("content3", "Pilates")
        )

        // When
        contentDao.insertAllContent(contentList)
        val allContent = contentDao.getAllContentFlow().first()

        // Then
        assertEquals(3, allContent.size)
    }

    @Test
    fun searchContent() = runTest {
        // Given
        val contentList = listOf(
            createTestContent("content1", "Morning Meditation"),
            createTestContent("content2", "Evening Yoga"),
            createTestContent("content3", "Meditation for Sleep")
        )
        contentDao.insertAllContent(contentList)

        // When
        val searchResults = contentDao.searchContentFlow("meditation").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.all { it.title.contains("Meditation", ignoreCase = true) })
    }

    @Test
    fun filterContentByType() = runTest {
        // Given
        val contentList = listOf(
            createTestContent("content1", type = ContentType.YOGA),
            createTestContent("content2", type = ContentType.MEDITATION),
            createTestContent("content3", type = ContentType.YOGA)
        )
        contentDao.insertAllContent(contentList)

        // When
        val yogaContent = contentDao.getContentByTypeFlow(ContentType.YOGA).first()

        // Then
        assertEquals(2, yogaContent.size)
        assertTrue(yogaContent.all { it.type == ContentType.YOGA })
    }

    @Test
    fun deleteContent() = runTest {
        // Given
        val content = createTestContent("content1")
        contentDao.insertContent(content)

        // When
        contentDao.deleteContentById("content1")
        val retrieved = contentDao.getContentById("content1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun getContentCount() = runTest {
        // Given
        val contentList = listOf(
            createTestContent("content1"),
            createTestContent("content2"),
            createTestContent("content3")
        )
        contentDao.insertAllContent(contentList)

        // When
        val count = contentDao.getContentCount()

        // Then
        assertEquals(3, count)
    }

    private fun createTestContent(
        id: String,
        title: String = "Test Content",
        type: ContentType = ContentType.MEDITATION
    ): Content {
        return Content(
            id = id,
            title = title,
            description = "Test description",
            type = type,
            category = Category.MINDFULNESS,
            durationMinutes = 10,
            level = ExperienceLevel.BEGINNER,
            videoUrl = null,
            audioUrl = null,
            thumbnailUrl = null,
            instructorName = "Test Instructor",
            tags = listOf("test", "sample"),
            isFlashSession = false,
            equipment = emptyList(),
            benefits = emptyList(),
            createdAt = LocalDateTime.now(),
            isOfflineAvailable = false,
            downloadSize = null
        )
    }
}
