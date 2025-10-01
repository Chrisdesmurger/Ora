package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String? = null,
    val preferredTimeSlot: TimeSlot = TimeSlot.MORNING,
    val experienceLevel: ExperienceLevel = ExperienceLevel.BEGINNER,
    val goals: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastActiveAt: LocalDateTime = LocalDateTime.now(),
    val isOnboardingCompleted: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false
)

enum class TimeSlot {
    MORNING, DAY, EVENING
}

enum class ExperienceLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}