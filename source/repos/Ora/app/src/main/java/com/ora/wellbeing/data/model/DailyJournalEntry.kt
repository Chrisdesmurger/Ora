package com.ora.wellbeing.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Daily Journal Entry - Comprehensive daily journaling model
 * Firestore collection: "users/{uid}/dailyJournal/{date}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Uses Firestore Timestamp for automatic server-side timestamp management
 *
 * @property uid Firebase Auth UID (user who owns this entry)
 * @property date Entry date in ISO format (yyyy-MM-dd), used as document ID
 * @property mood Mood of the day: "happy", "neutral", "sad", "frustrated"
 * @property shortNote Brief summary of the day
 * @property dailyStory Free text story about the day
 * @property gratitudes List of gratitude items (max 3)
 * @property accomplishments List of accomplishments
 * @property improvements List of areas to improve (max 3)
 * @property learnings What was learned today
 * @property remindMeTomorrow Checkbox to show reminder tomorrow
 * @property createdAt Firestore Timestamp when entry was created
 * @property updatedAt Firestore Timestamp when entry was last updated
 */
@IgnoreExtraProperties
class DailyJournalEntry() {
    var uid: String = ""
    var date: String = ""
    var mood: String = "" // "happy", "neutral", "sad", "frustrated"
    var shortNote: String = ""
    var dailyStory: String = ""
    var gratitudes: List<String> = emptyList()
    var accomplishments: List<String> = emptyList()
    var improvements: List<String> = emptyList()
    var learnings: String = ""
    var remindMeTomorrow: Boolean = false

    @ServerTimestamp
    var createdAt: Timestamp? = null

    @ServerTimestamp
    var updatedAt: Timestamp? = null

    // Full constructor for easy creation
    constructor(
        uid: String,
        date: String,
        mood: String = "",
        shortNote: String = "",
        dailyStory: String = "",
        gratitudes: List<String> = emptyList(),
        accomplishments: List<String> = emptyList(),
        improvements: List<String> = emptyList(),
        learnings: String = "",
        remindMeTomorrow: Boolean = false,
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this() {
        this.uid = uid
        this.date = date
        this.mood = mood
        this.shortNote = shortNote
        this.dailyStory = dailyStory
        this.gratitudes = gratitudes
        this.accomplishments = accomplishments
        this.improvements = improvements
        this.learnings = learnings
        this.remindMeTomorrow = remindMeTomorrow
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    companion object {
        /**
         * Creates a new daily journal entry for today
         */
        fun createForToday(
            uid: String,
            mood: String = "",
            shortNote: String = "",
            dailyStory: String = "",
            gratitudes: List<String> = emptyList(),
            accomplishments: List<String> = emptyList(),
            improvements: List<String> = emptyList(),
            learnings: String = "",
            remindMeTomorrow: Boolean = false
        ): DailyJournalEntry {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            return DailyJournalEntry(
                uid = uid,
                date = today,
                mood = mood,
                shortNote = shortNote.take(200), // Max 200 chars for short note
                dailyStory = dailyStory.take(2000), // Max 2000 chars for story
                gratitudes = gratitudes.take(3), // Max 3 gratitudes
                accomplishments = accomplishments,
                improvements = improvements.take(3), // Max 3 improvements
                learnings = learnings.take(1000), // Max 1000 chars for learnings
                remindMeTomorrow = remindMeTomorrow
            )
        }

        /**
         * Creates a new daily journal entry for a specific date
         */
        fun createForDate(
            uid: String,
            date: String,
            mood: String = "",
            shortNote: String = "",
            dailyStory: String = "",
            gratitudes: List<String> = emptyList(),
            accomplishments: List<String> = emptyList(),
            improvements: List<String> = emptyList(),
            learnings: String = "",
            remindMeTomorrow: Boolean = false
        ): DailyJournalEntry {
            return DailyJournalEntry(
                uid = uid,
                date = date,
                mood = mood,
                shortNote = shortNote.take(200),
                dailyStory = dailyStory.take(2000),
                gratitudes = gratitudes.take(3),
                accomplishments = accomplishments,
                improvements = improvements.take(3),
                learnings = learnings.take(1000),
                remindMeTomorrow = remindMeTomorrow
            )
        }
    }

    /**
     * Copy function for immutability pattern
     */
    @Exclude
    fun copy(
        uid: String = this.uid,
        date: String = this.date,
        mood: String = this.mood,
        shortNote: String = this.shortNote,
        dailyStory: String = this.dailyStory,
        gratitudes: List<String> = this.gratitudes,
        accomplishments: List<String> = this.accomplishments,
        improvements: List<String> = this.improvements,
        learnings: String = this.learnings,
        remindMeTomorrow: Boolean = this.remindMeTomorrow,
        createdAt: Timestamp? = this.createdAt,
        updatedAt: Timestamp? = this.updatedAt
    ): DailyJournalEntry {
        return DailyJournalEntry(
            uid = uid,
            date = date,
            mood = mood,
            shortNote = shortNote,
            dailyStory = dailyStory,
            gratitudes = gratitudes,
            accomplishments = accomplishments,
            improvements = improvements,
            learnings = learnings,
            remindMeTomorrow = remindMeTomorrow,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Formats the date for display (e.g., "12 octobre 2025")
     */
    @Exclude
    fun getFormattedDate(): String {
        return try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH))
        } catch (e: Exception) {
            date
        }
    }

    /**
     * Checks if this entry is for today
     */
    @Exclude
    fun isToday(): Boolean {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return date == today
    }

    /**
     * Gets the mood emoji representation
     */
    @Exclude
    fun getMoodEmoji(): String {
        return when (mood) {
            "happy" -> "😊"
            "neutral" -> "😐"
            "sad" -> "😕"
            "frustrated" -> "😠"
            else -> ""
        }
    }

    /**
     * Gets the mood label in French
     */
    @Exclude
    fun getMoodLabel(): String {
        return when (mood) {
            "happy" -> "Joyeux"
            "neutral" -> "Neutre"
            "sad" -> "Triste"
            "frustrated" -> "Frustré"
            else -> ""
        }
    }

    /**
     * Checks if the entry is complete (has minimum required fields)
     */
    @Exclude
    fun isComplete(): Boolean {
        return mood.isNotEmpty() && (shortNote.isNotEmpty() || dailyStory.isNotEmpty())
    }
}

/**
 * Enum for mood types
 */
enum class MoodType(val value: String, val emoji: String, val label: String) {
    HAPPY("happy", "😊", "Joyeux"),
    NEUTRAL("neutral", "😐", "Neutre"),
    SAD("sad", "😕", "Triste"),
    FRUSTRATED("frustrated", "😠", "Frustré");

    companion object {
        fun fromValue(value: String): MoodType? {
            return values().find { it.value == value }
        }
    }
}
