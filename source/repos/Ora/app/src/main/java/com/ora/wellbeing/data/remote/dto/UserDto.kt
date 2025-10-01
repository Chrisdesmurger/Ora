package com.ora.wellbeing.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ora.wellbeing.data.local.entities.*
import java.time.LocalDateTime

data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("preferred_time_slot")
    val preferredTimeSlot: String,

    @SerializedName("experience_level")
    val experienceLevel: String,

    @SerializedName("goals")
    val goals: List<String>,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("last_active_at")
    val lastActiveAt: String,

    @SerializedName("is_onboarding_completed")
    val isOnboardingCompleted: Boolean,

    @SerializedName("notifications_enabled")
    val notificationsEnabled: Boolean,

    @SerializedName("dark_mode_enabled")
    val darkModeEnabled: Boolean
)

// Extension functions for conversion
fun UserDto.toEntity(): User {
    return User(
        id = id,
        name = name,
        email = email,
        preferredTimeSlot = TimeSlot.valueOf(preferredTimeSlot),
        experienceLevel = ExperienceLevel.valueOf(experienceLevel),
        goals = goals,
        createdAt = LocalDateTime.parse(createdAt),
        lastActiveAt = LocalDateTime.parse(lastActiveAt),
        isOnboardingCompleted = isOnboardingCompleted,
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        preferredTimeSlot = preferredTimeSlot.name,
        experienceLevel = experienceLevel.name,
        goals = goals,
        createdAt = createdAt.toString(),
        lastActiveAt = lastActiveAt.toString(),
        isOnboardingCompleted = isOnboardingCompleted,
        notificationsEnabled = notificationsEnabled,
        darkModeEnabled = darkModeEnabled
    )
}