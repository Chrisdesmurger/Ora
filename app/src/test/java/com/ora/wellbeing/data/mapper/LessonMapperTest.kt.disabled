package com.ora.wellbeing.data.mapper

import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.firestore.LessonDocument
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for LessonMapper
 *
 * Tests the conversion between Firestore LessonDocument (snake_case)
 * and Android ContentItem (camelCase)
 */
class LessonMapperTest {

    private lateinit var sampleLessonDocument: LessonDocument
    private val testTimestamp = Timestamp(Date())

    @Before
    fun setup() {
        // Create a sample LessonDocument with all fields populated
        sampleLessonDocument = LessonDocument().apply {
            title = "Morning Meditation"
            description = "A gentle 10-minute meditation to start your day"
            type = "video"
            program_id = "prog-123"
            order = 0
            duration_sec = 600 // 10 minutes
            tags = listOf("meditation", "morning", "beginner")
            transcript = "Welcome to this morning meditation..."
            status = "ready"
            storage_path_original = "lessons/lesson-001/original.mp4"
            renditions = mapOf(
                "high" to mapOf(
                    "path" to "gs://ora-bucket/lessons/lesson-001/high.mp4",
                    "width" to 1920,
                    "height" to 1080,
                    "bitrate_kbps" to 5000
                ),
                "medium" to mapOf(
                    "path" to "gs://ora-bucket/lessons/lesson-001/medium.mp4",
                    "width" to 1280,
                    "height" to 720
                ),
                "low" to mapOf(
                    "path" to "gs://ora-bucket/lessons/lesson-001/low.mp4",
                    "width" to 854,
                    "height" to 480
                )
            )
            audio_variants = mapOf(
                "high" to mapOf(
                    "path" to "gs://ora-bucket/lessons/lesson-001/audio-high.m4a",
                    "bitrate_kbps" to 320
                ),
                "medium" to mapOf(
                    "path" to "gs://ora-bucket/lessons/lesson-001/audio-medium.m4a",
                    "bitrate_kbps" to 192
                )
            )
            codec = "h264"
            size_bytes = 104857600L // 100 MB
            thumbnail_url = "https://cdn.ora.com/thumbnails/lesson-001.jpg"
            mime_type = "video/mp4"
            created_at = testTimestamp
            updated_at = testTimestamp
            author_id = "teacher-456"
            scheduled_publish_at = testTimestamp
        }
    }

    @Test
    fun `fromFirestore maps all basic fields correctly`() {
        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("lesson-001", contentItem.id)
        assertEquals("Morning Meditation", contentItem.title)
        assertEquals("A gentle 10-minute meditation to start your day", contentItem.description)
        assertEquals(10, contentItem.durationMinutes)
        assertEquals("10 min", contentItem.duration)
        assertEquals(testTimestamp, contentItem.createdAt)
        assertEquals(testTimestamp, contentItem.updatedAt)
    }

    @Test
    fun `fromFirestore converts status to isActive correctly`() {
        // Test "ready" status
        sampleLessonDocument.status = "ready"
        var contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)
        assertTrue(contentItem.isActive)

        // Test "draft" status
        sampleLessonDocument.status = "draft"
        contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)
        assertFalse(contentItem.isActive)

        // Test "processing" status
        sampleLessonDocument.status = "processing"
        contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)
        assertFalse(contentItem.isActive)
    }

    @Test
    fun `extractBestVideoUrl selects high quality first`() {
        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("gs://ora-bucket/lessons/lesson-001/high.mp4", contentItem.videoUrl)
    }

    @Test
    fun `extractBestVideoUrl falls back to medium when high not available`() {
        sampleLessonDocument.renditions = mapOf(
            "medium" to mapOf("path" to "gs://ora-bucket/lessons/lesson-001/medium.mp4"),
            "low" to mapOf("path" to "gs://ora-bucket/lessons/lesson-001/low.mp4")
        )

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("gs://ora-bucket/lessons/lesson-001/medium.mp4", contentItem.videoUrl)
    }

    @Test
    fun `extractBestVideoUrl falls back to low when only low available`() {
        sampleLessonDocument.renditions = mapOf(
            "low" to mapOf("path" to "gs://ora-bucket/lessons/lesson-001/low.mp4")
        )

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("gs://ora-bucket/lessons/lesson-001/low.mp4", contentItem.videoUrl)
    }

    @Test
    fun `extractBestVideoUrl returns null when no renditions`() {
        sampleLessonDocument.renditions = null

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertNull(contentItem.videoUrl)
    }

    @Test
    fun `extractBestAudioUrl selects high quality first`() {
        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("gs://ora-bucket/lessons/lesson-001/audio-high.m4a", contentItem.audioUrl)
    }

    @Test
    fun `extractBestAudioUrl returns null when no audio variants`() {
        sampleLessonDocument.audio_variants = null

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertNull(contentItem.audioUrl)
    }

    @Test
    fun `mapLessonTypeToCategory maps meditation tag correctly`() {
        sampleLessonDocument.tags = listOf("meditation", "morning")

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("Méditation", contentItem.category)
    }

    @Test
    fun `mapLessonTypeToCategory maps yoga tag correctly`() {
        sampleLessonDocument.tags = listOf("yoga", "beginner")

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("Yoga", contentItem.category)
    }

    @Test
    fun `mapLessonTypeToCategory maps pilates tag correctly`() {
        sampleLessonDocument.tags = listOf("pilates", "core")

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("Pilates", contentItem.category)
    }

    @Test
    fun `mapLessonTypeToCategory defaults to Bien-être when no recognizable tags`() {
        sampleLessonDocument.tags = listOf("relaxation", "wellness")

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("Bien-être", contentItem.category)
    }

    @Test
    fun `formatDuration handles hours and minutes correctly`() {
        // Test 90 minutes (1h 30 min)
        sampleLessonDocument.duration_sec = 5400 // 90 minutes

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("1h 30 min", contentItem.duration)
    }

    @Test
    fun `formatDuration handles exact hours correctly`() {
        // Test 60 minutes (1h)
        sampleLessonDocument.duration_sec = 3600

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("1h", contentItem.duration)
    }

    @Test
    fun `formatDuration handles minutes only correctly`() {
        // Test 45 minutes
        sampleLessonDocument.duration_sec = 2700

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("45 min", contentItem.duration)
    }

    @Test
    fun `formatDuration returns empty string for null duration`() {
        sampleLessonDocument.duration_sec = null

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals("", contentItem.duration)
    }

    @Test
    fun `fromFirestore handles missing optional fields gracefully`() {
        val minimalLesson = LessonDocument().apply {
            title = "Minimal Lesson"
            type = "video"
            program_id = "prog-123"
            order = 0
            status = "ready"
            tags = emptyList()
        }

        val contentItem = LessonMapper.fromFirestore("lesson-002", minimalLesson)

        assertNotNull(contentItem)
        assertEquals("Minimal Lesson", contentItem.title)
        assertEquals("", contentItem.description) // Default empty string
        assertEquals(0, contentItem.durationMinutes)
        assertNull(contentItem.videoUrl)
        assertNull(contentItem.audioUrl)
        assertEquals("Bien-être", contentItem.category) // Default category
    }

    @Test
    fun `fromFirestore preserves tags correctly`() {
        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertEquals(3, contentItem.tags.size)
        assertTrue(contentItem.tags.contains("meditation"))
        assertTrue(contentItem.tags.contains("morning"))
        assertTrue(contentItem.tags.contains("beginner"))
    }

    @Test
    fun `toFirestore converts ContentItem back correctly`() {
        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)
        val backToFirestore = LessonMapper.toFirestore(contentItem)

        assertEquals("Morning Meditation", backToFirestore.title)
        assertEquals("A gentle 10-minute meditation to start your day", backToFirestore.description)
        assertEquals(600, backToFirestore.duration_sec) // 10 minutes * 60
        assertEquals("ready", backToFirestore.status)
    }

    @Test
    fun `fromFirestore marks lesson as new when created within 7 days`() {
        val recentTimestamp = Timestamp(Date(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000))) // 3 days ago
        sampleLessonDocument.created_at = recentTimestamp

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertTrue(contentItem.isNew)
    }

    @Test
    fun `fromFirestore does not mark lesson as new when created more than 7 days ago`() {
        val oldTimestamp = Timestamp(Date(System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000))) // 10 days ago
        sampleLessonDocument.created_at = oldTimestamp

        val contentItem = LessonMapper.fromFirestore("lesson-001", sampleLessonDocument)

        assertFalse(contentItem.isNew)
    }
}
