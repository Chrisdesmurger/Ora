package com.ora.wellbeing.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Gratitude Entry - Model for daily gratitude journal entries
 * Firestore collection: "gratitudes/{uid}/entries/{date}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Uses Firestore Timestamp for automatic server-side timestamp management
 *
 * @property uid Firebase Auth UID (user who owns this entry)
 * @property date Entry date in ISO format (yyyy-MM-dd), used as document ID
 * @property gratitudes List of gratitude items (max 3)
 * @property mood Optional mood indicator with emoji + label
 * @property notes Optional personal notes (max 500 chars)
 * @property createdAt Firestore Timestamp when entry was created
 * @property updatedAt Firestore Timestamp when entry was last updated
 */
@IgnoreExtraProperties
class GratitudeEntry() {
    var uid: String = ""

    var date: String = ""

    var gratitudes: List<String> = emptyList()

    var mood: String? = null

    var notes: String? = null

    @ServerTimestamp
    var createdAt: Timestamp? = null

    @ServerTimestamp
    var updatedAt: Timestamp? = null

    // Constructor for easy creation
    constructor(
        uid: String,
        date: String,
        gratitudes: List<String> = emptyList(),
        mood: String? = null,
        notes: String? = null,
        createdAt: Timestamp? = null,
        updatedAt: Timestamp? = null
    ) : this() {
        this.uid = uid
        this.date = date
        this.gratitudes = gratitudes
        this.mood = mood
        this.notes = notes
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    companion object {
        /**
         * Creates a new gratitude entry for today
         */
        fun createForToday(uid: String, gratitudes: List<String>, mood: String? = null, notes: String? = null): GratitudeEntry {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            return GratitudeEntry(
                uid = uid,
                date = today,
                gratitudes = gratitudes.take(3), // Ensure max 3
                mood = mood,
                notes = notes?.take(500) // Ensure max 500 chars
                // createdAt and updatedAt will be set by @ServerTimestamp
            )
        }

        /**
         * Creates a new gratitude entry for a specific date
         */
        fun createForDate(uid: String, date: String, gratitudes: List<String>, mood: String? = null, notes: String? = null): GratitudeEntry {
            return GratitudeEntry(
                uid = uid,
                date = date,
                gratitudes = gratitudes.take(3),
                mood = mood,
                notes = notes?.take(500)
                // createdAt and updatedAt will be set by @ServerTimestamp
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
        gratitudes: List<String> = this.gratitudes,
        mood: String? = this.mood,
        notes: String? = this.notes,
        createdAt: Timestamp? = this.createdAt,
        updatedAt: Timestamp? = this.updatedAt
    ): GratitudeEntry {
        return GratitudeEntry(
            uid = uid,
            date = date,
            gratitudes = gratitudes,
            mood = mood,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Formats the date for display (e.g., "5 octobre 2025")
     */
    @Exclude
    fun getFormattedDate(): String {
        return try {
            val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
            localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.FRENCH))
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
     * Updates this entry with new data
     */
    @Exclude
    fun update(gratitudes: List<String>, mood: String? = null, notes: String? = null): GratitudeEntry {
        return copy(
            gratitudes = gratitudes.take(3),
            mood = mood,
            notes = notes?.take(500)
            // updatedAt will be set by @ServerTimestamp when saved to Firestore
        )
    }
}
