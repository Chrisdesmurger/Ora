package com.ora.wellbeing.data.mapper

import com.ora.wellbeing.data.model.Program
import com.ora.wellbeing.data.model.firestore.ProgramDocument
import timber.log.Timber

/**
 * ProgramMapper - Converts between Firestore ProgramDocument and Android Program
 *
 * This mapper handles the conversion between:
 * - Backend schema (snake_case fields from OraWebApp)
 * - Android schema (camelCase fields for Ora app)
 *
 * Key Conversions:
 * - snake_case → camelCase (duration_days → duration)
 * - difficulty → level (with French translation)
 * - lessons (string[]) → sessions (will be populated with lesson details)
 * - status ("published") → isActive (boolean)
 * - cover_image_url → thumbnailUrl
 */
object ProgramMapper {

    /**
     * Converts Firestore ProgramDocument to Android Program
     *
     * @param id Firestore document ID
     * @param doc ProgramDocument from Firestore (snake_case)
     * @return Program for Android app (camelCase)
     */
    fun fromFirestore(id: String, doc: ProgramDocument): Program {
        Timber.d("Mapping program from Firestore: id=$id, title=${doc.title}, status=${doc.status}")

        return Program().apply {
            this.id = id
            this.title = doc.title
            this.description = doc.description
            this.category = mapCategoryToFrench(doc.category)
            this.duration = doc.duration_days
            this.level = mapDifficultyToFrench(doc.difficulty)
            this.participantCount = doc.participant_count
            this.rating = doc.rating
            this.thumbnailUrl = doc.cover_image_url
            this.instructor = extractInstructorFromTags(doc.tags)
            this.isPremiumOnly = false // TODO: Determine from settings
            this.sessions = emptyList() // Will be populated with lessons via repository
            this.isActive = doc.status == "published"
            this.createdAt = doc.created_at
            this.updatedAt = doc.updated_at
        }
    }

    /**
     * Maps backend category to French display name
     *
     * @param category Backend category ("meditation", "yoga", etc.)
     * @return French category name
     */
    private fun mapCategoryToFrench(category: String): String {
        return when (category.lowercase()) {
            "meditation" -> "Méditation"
            "yoga" -> "Yoga"
            "mindfulness" -> "Pleine Conscience"
            "wellness" -> "Bien-être"
            else -> {
                Timber.w("Unknown category: $category, defaulting to Bien-être")
                "Bien-être"
            }
        }
    }

    /**
     * Maps backend difficulty to French level
     *
     * @param difficulty Backend difficulty ("beginner", "intermediate", "advanced")
     * @return French level string
     */
    private fun mapDifficultyToFrench(difficulty: String): String {
        return when (difficulty.lowercase()) {
            "beginner" -> "Débutant"
            "intermediate" -> "Intermédiaire"
            "advanced" -> "Avancé"
            else -> {
                Timber.w("Unknown difficulty: $difficulty, defaulting to Tous niveaux")
                "Tous niveaux"
            }
        }
    }

    /**
     * Maps French level back to backend difficulty
     *
     * @param level French level string
     * @return Backend difficulty
     */
    private fun mapFrenchToDifficulty(level: String): String {
        return when (level) {
            "Débutant" -> "beginner"
            "Intermédiaire" -> "intermediate"
            "Avancé" -> "advanced"
            else -> "beginner"
        }
    }

    /**
     * Maps French category back to backend category
     *
     * @param category French category name
     * @return Backend category
     */
    private fun mapFrenchToCategory(category: String): String {
        return when (category) {
            "Méditation" -> "meditation"
            "Yoga" -> "yoga"
            "Pleine Conscience" -> "mindfulness"
            "Bien-être" -> "wellness"
            else -> "wellness"
        }
    }

    /**
     * Extracts instructor name from tags
     * Looks for tags like "instructor:Marie Dupont"
     *
     * @param tags List of tags
     * @return Instructor name or null
     */
    private fun extractInstructorFromTags(tags: List<String>): String? {
        val instructorTag = tags.find { it.startsWith("instructor:", ignoreCase = true) }
        return instructorTag?.substringAfter("instructor:", "")?.takeIf { it.isNotBlank() }
    }

    /**
     * Converts Android Program back to Firestore ProgramDocument
     * Note: This is primarily for future use when we allow editing from Android
     *
     * @param program Program from Android app
     * @return ProgramDocument for Firestore
     */
    fun toFirestore(program: Program): ProgramDocument {
        return ProgramDocument().apply {
            this.title = program.title
            this.description = program.description
            this.category = mapFrenchToCategory(program.category)
            this.difficulty = mapFrenchToDifficulty(program.level)
            this.duration_days = program.duration
            this.lessons = emptyList() // Lessons managed separately via lesson IDs
            this.cover_image_url = program.thumbnailUrl
            this.status = if (program.isActive) "published" else "draft"
            this.tags = emptyList() // TODO: Extract from program metadata
            this.author_id = "" // Set by backend when creating
            this.participant_count = program.participantCount
            this.rating = program.rating
        }
    }

    /**
     * Creates a Program with populated lesson IDs
     * This is used to associate lessons with the program
     *
     * @param program Base program
     * @param lessonIds List of lesson IDs in order
     * @return Program with lesson IDs as sessions
     */
    fun withLessonIds(program: Program, lessonIds: List<String>): Program {
        // Since Program is not a data class, we manually update sessions
        program.sessions = lessonIds.map { lessonId ->
            mapOf(
                "id" to lessonId,
                "day" to (lessonIds.indexOf(lessonId) + 1),
                "title" to "", // Will be populated when lessons are fetched
                "duration" to "", // Will be populated when lessons are fetched
                "isCompleted" to false
            )
        }
        return program
    }

    /**
     * Formats the program duration for display
     *
     * @param durationDays Duration in days
     * @return Formatted string (e.g., "7 jours", "1 semaine", "3 semaines")
     */
    fun formatDuration(durationDays: Int): String {
        return when {
            durationDays == 1 -> "1 jour"
            durationDays < 7 -> "$durationDays jours"
            durationDays == 7 -> "1 semaine"
            durationDays % 7 == 0 -> "${durationDays / 7} semaines"
            else -> "$durationDays jours"
        }
    }
}
