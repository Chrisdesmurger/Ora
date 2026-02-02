package com.ora.wellbeing.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

/**
 * UserProgram - Firestore data model for user program enrollment and progress tracking
 * Collection: "user_programs/{uid}/enrolled/{programId}"
 *
 * IMPORTANT: Uses regular class (not data class) for Firestore compatibility
 * Uses Firestore Timestamp for automatic server-side timestamp management
 */
@IgnoreExtraProperties
class UserProgram() {
    var id: String = ""

    var uid: String = ""

    @get:PropertyName("programId")
    @set:PropertyName("programId")
    var programId: String = ""

    @get:PropertyName("currentDay")
    @set:PropertyName("currentDay")
    var currentDay: Int = 1

    @get:PropertyName("totalDays")
    @set:PropertyName("totalDays")
    var totalDays: Int = 0

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false

    @get:PropertyName("isPremiumOnly")
    @set:PropertyName("isPremiumOnly")
    var isPremiumOnly: Boolean = false

    @get:PropertyName("programTitle")
    @set:PropertyName("programTitle")
    var programTitle: String? = null

    @get:PropertyName("programCategory")
    @set:PropertyName("programCategory")
    var programCategory: String? = null

    @ServerTimestamp
    @get:PropertyName("startedAt")
    @set:PropertyName("startedAt")
    var startedAt: Timestamp? = null

    @ServerTimestamp
    @get:PropertyName("lastSessionAt")
    @set:PropertyName("lastSessionAt")
    var lastSessionAt: Timestamp? = null

    @get:PropertyName("completedAt")
    @set:PropertyName("completedAt")
    var completedAt: Timestamp? = null

    @ServerTimestamp
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Timestamp? = null

    @ServerTimestamp
    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Timestamp? = null

    companion object {
        /**
         * Creates a new user program enrollment
         */
        fun createEnrollment(uid: String, programId: String, totalDays: Int, programTitle: String? = null, programCategory: String? = null): UserProgram {
            return UserProgram().apply {
                this.uid = uid
                this.programId = programId
                this.currentDay = 1
                this.totalDays = totalDays
                this.programTitle = programTitle
                this.programCategory = programCategory
                this.isCompleted = false
                // Timestamps will be set by @ServerTimestamp
            }
        }
    }

    /**
     * Calculates progress percentage based on current day and total days
     */
    @Exclude
    fun calculateProgress(): Int {
        if (totalDays == 0) return 0
        return ((currentDay - 1) * 100 / totalDays).coerceIn(0, 100)
    }

    /**
     * Gets the next session day (currentDay + 1)
     */
    @Exclude
    fun getNextSessionDay(): Int {
        return if (currentDay < totalDays) currentDay + 1 else currentDay
    }

    /**
     * Checks if the user is active today (practiced within last 24 hours)
     */
    @Exclude
    fun isActiveToday(): Boolean {
        if (lastSessionAt == null) return false
        val now = Timestamp.now()
        val daysSinceLastSession = TimeUnit.MILLISECONDS.toDays(now.toDate().time - lastSessionAt!!.toDate().time)
        return daysSinceLastSession == 0L
    }

    /**
     * Gets days since enrollment
     */
    @Exclude
    fun getDaysSinceEnrollment(): Int {
        if (startedAt == null) return 0
        val now = Timestamp.now()
        return TimeUnit.MILLISECONDS.toDays(now.toDate().time - startedAt!!.toDate().time).toInt()
    }

    /**
     * Gets formatted progress (e.g., "Jour 5/21")
     */
    @Exclude
    fun getFormattedProgress(): String {
        return "Jour $currentDay/$totalDays"
    }
}
