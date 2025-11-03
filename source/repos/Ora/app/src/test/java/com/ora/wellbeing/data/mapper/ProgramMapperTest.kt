package com.ora.wellbeing.data.mapper

import com.google.firebase.Timestamp
import com.ora.wellbeing.data.model.firestore.ProgramDocument
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for ProgramMapper
 *
 * Tests the conversion between Firestore ProgramDocument (snake_case)
 * and Android Program (camelCase)
 */
class ProgramMapperTest {

    private lateinit var sampleProgramDocument: ProgramDocument
    private val testTimestamp = Timestamp(Date())

    @Before
    fun setup() {
        // Create a sample ProgramDocument with all fields populated
        sampleProgramDocument = ProgramDocument().apply {
            title = "7-Day Meditation Challenge"
            description = "Build a consistent meditation practice in just 7 days"
            category = "meditation"
            difficulty = "beginner"
            duration_days = 7
            lessons = listOf("lesson-001", "lesson-002", "lesson-003", "lesson-004", "lesson-005", "lesson-006", "lesson-007")
            cover_image_url = "https://cdn.ora.com/covers/prog-123.jpg"
            cover_storage_path = "programs/prog-123/cover.jpg"
            status = "published"
            tags = listOf("meditation", "beginner", "morning", "instructor:Marie Dupont")
            author_id = "teacher-456"
            created_at = testTimestamp
            updated_at = testTimestamp
            scheduled_publish_at = null
            scheduled_archive_at = null
            auto_publish_enabled = false
            participant_count = 150
            rating = 4.5f
        }
    }

    @Test
    fun `fromFirestore maps all basic fields correctly`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("prog-123", program.id)
        assertEquals("7-Day Meditation Challenge", program.title)
        assertEquals("Build a consistent meditation practice in just 7 days", program.description)
        assertEquals(7, program.duration)
        assertEquals(150, program.participantCount)
        assertEquals(4.5f, program.rating, 0.01f)
        assertEquals("https://cdn.ora.com/covers/prog-123.jpg", program.thumbnailUrl)
        assertEquals(testTimestamp, program.createdAt)
        assertEquals(testTimestamp, program.updatedAt)
    }

    @Test
    fun `fromFirestore converts category to French correctly`() {
        // Test meditation
        sampleProgramDocument.category = "meditation"
        var program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Méditation", program.category)

        // Test yoga
        sampleProgramDocument.category = "yoga"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Yoga", program.category)

        // Test mindfulness
        sampleProgramDocument.category = "mindfulness"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Pleine Conscience", program.category)

        // Test wellness
        sampleProgramDocument.category = "wellness"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Bien-être", program.category)
    }

    @Test
    fun `fromFirestore defaults unknown category to Bien-être`() {
        sampleProgramDocument.category = "unknown-category"

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Bien-être", program.category)
    }

    @Test
    fun `fromFirestore converts difficulty to French level correctly`() {
        // Test beginner
        sampleProgramDocument.difficulty = "beginner"
        var program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Débutant", program.level)

        // Test intermediate
        sampleProgramDocument.difficulty = "intermediate"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Intermédiaire", program.level)

        // Test advanced
        sampleProgramDocument.difficulty = "advanced"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertEquals("Avancé", program.level)
    }

    @Test
    fun `fromFirestore defaults unknown difficulty to Tous niveaux`() {
        sampleProgramDocument.difficulty = "expert"

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Tous niveaux", program.level)
    }

    @Test
    fun `fromFirestore converts status to isActive correctly`() {
        // Test published status
        sampleProgramDocument.status = "published"
        var program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertTrue(program.isActive)

        // Test draft status
        sampleProgramDocument.status = "draft"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertFalse(program.isActive)

        // Test archived status
        sampleProgramDocument.status = "archived"
        program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        assertFalse(program.isActive)
    }

    @Test
    fun `fromFirestore initializes sessions as empty list`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertNotNull(program.sessions)
        assertTrue(program.sessions.isEmpty())
    }

    @Test
    fun `fromFirestore extracts instructor from tags`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Marie Dupont", program.instructor)
    }

    @Test
    fun `fromFirestore returns null instructor when tag not present`() {
        sampleProgramDocument.tags = listOf("meditation", "beginner")

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertNull(program.instructor)
    }

    @Test
    fun `fromFirestore handles case-insensitive instructor tag`() {
        sampleProgramDocument.tags = listOf("INSTRUCTOR:Jean Martin")

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Jean Martin", program.instructor)
    }

    @Test
    fun `toFirestore converts Program back correctly`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        val backToFirestore = ProgramMapper.toFirestore(program)

        assertEquals("7-Day Meditation Challenge", backToFirestore.title)
        assertEquals("Build a consistent meditation practice in just 7 days", backToFirestore.description)
        assertEquals("meditation", backToFirestore.category)
        assertEquals("beginner", backToFirestore.difficulty)
        assertEquals(7, backToFirestore.duration_days)
        assertEquals("published", backToFirestore.status)
        assertEquals(150, backToFirestore.participant_count)
        assertEquals(4.5f, backToFirestore.rating, 0.01f)
    }

    @Test
    fun `withLessonIds creates sessions map from lesson IDs`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        val lessonIds = listOf("lesson-001", "lesson-002", "lesson-003")

        val programWithLessons = ProgramMapper.withLessonIds(program, lessonIds)

        assertEquals(3, programWithLessons.sessions.size)
        assertEquals("lesson-001", programWithLessons.sessions[0]["id"])
        assertEquals(1, programWithLessons.sessions[0]["day"])
        assertEquals("lesson-002", programWithLessons.sessions[1]["id"])
        assertEquals(2, programWithLessons.sessions[1]["day"])
        assertEquals("lesson-003", programWithLessons.sessions[2]["id"])
        assertEquals(3, programWithLessons.sessions[2]["day"])
    }

    @Test
    fun `withLessonIds initializes isCompleted as false`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        val lessonIds = listOf("lesson-001")

        val programWithLessons = ProgramMapper.withLessonIds(program, lessonIds)

        assertFalse(programWithLessons.sessions[0]["isCompleted"] as Boolean)
    }

    @Test
    fun `formatDuration formats single day correctly`() {
        val duration = ProgramMapper.formatDuration(1)

        assertEquals("1 jour", duration)
    }

    @Test
    fun `formatDuration formats multiple days correctly`() {
        val duration = ProgramMapper.formatDuration(5)

        assertEquals("5 jours", duration)
    }

    @Test
    fun `formatDuration formats one week correctly`() {
        val duration = ProgramMapper.formatDuration(7)

        assertEquals("1 semaine", duration)
    }

    @Test
    fun `formatDuration formats multiple weeks correctly`() {
        val duration = ProgramMapper.formatDuration(14)

        assertEquals("2 semaines", duration)
    }

    @Test
    fun `formatDuration formats non-week-aligned days correctly`() {
        val duration = ProgramMapper.formatDuration(10)

        assertEquals("10 jours", duration)
    }

    @Test
    fun `fromFirestore handles missing optional fields gracefully`() {
        val minimalProgram = ProgramDocument().apply {
            title = "Minimal Program"
            description = "A simple program"
            category = "meditation"
            difficulty = "beginner"
            duration_days = 1
            lessons = emptyList()
            status = "published"
            tags = emptyList()
            author_id = "teacher-789"
        }

        val program = ProgramMapper.fromFirestore("prog-456", minimalProgram)

        assertNotNull(program)
        assertEquals("Minimal Program", program.title)
        assertNull(program.thumbnailUrl)
        assertNull(program.instructor)
        assertEquals(0, program.participantCount)
        assertEquals(0.0f, program.rating, 0.01f)
    }

    @Test
    fun `fromFirestore handles case variations in category`() {
        sampleProgramDocument.category = "MEDITATION"

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Méditation", program.category)
    }

    @Test
    fun `fromFirestore handles case variations in difficulty`() {
        sampleProgramDocument.difficulty = "BEGINNER"

        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)

        assertEquals("Débutant", program.level)
    }

    @Test
    fun `toFirestore converts French category back to English`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        // Change to French
        val modifiedProgram = program.copy(category = "Yoga")

        val backToFirestore = ProgramMapper.toFirestore(modifiedProgram)

        assertEquals("yoga", backToFirestore.category)
    }

    @Test
    fun `toFirestore converts French level back to English difficulty`() {
        val program = ProgramMapper.fromFirestore("prog-123", sampleProgramDocument)
        val modifiedProgram = program.copy(level = "Intermédiaire")

        val backToFirestore = ProgramMapper.toFirestore(modifiedProgram)

        assertEquals("intermediate", backToFirestore.difficulty)
    }
}
