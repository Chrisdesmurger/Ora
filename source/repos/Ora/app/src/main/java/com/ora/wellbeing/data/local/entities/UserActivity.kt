package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_activities")
data class UserActivity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String,
    val sessionType: SessionType,
    val startedAt: LocalDateTime,
    val completedAt: LocalDateTime? = null,
    val durationMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val rating: Int? = null, // 1-5 stars
    val notes: String? = null,
    val streak: Int = 0
)

enum class SessionType {
    PRACTICE, FLASH_SESSION, GUIDED_MEDITATION, BREATHING_EXERCISE
}