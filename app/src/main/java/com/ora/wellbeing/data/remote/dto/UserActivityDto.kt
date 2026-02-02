package com.ora.wellbeing.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ora.wellbeing.data.local.entities.*
import java.time.LocalDateTime

data class UserActivityDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("content_id")
    val contentId: String,

    @SerializedName("session_type")
    val sessionType: String,

    @SerializedName("started_at")
    val startedAt: String,

    @SerializedName("completed_at")
    val completedAt: String?,

    @SerializedName("duration_minutes")
    val durationMinutes: Int?,

    @SerializedName("is_completed")
    val isCompleted: Boolean,

    @SerializedName("rating")
    val rating: Int?,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("streak")
    val streak: Int
)

data class UserStatsDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("total_sessions_completed")
    val totalSessionsCompleted: Int,

    @SerializedName("total_minutes_spent")
    val totalMinutesSpent: Int,

    @SerializedName("current_streak")
    val currentStreak: Int,

    @SerializedName("longest_streak")
    val longestStreak: Int,

    @SerializedName("last_session_date")
    val lastSessionDate: String?,

    @SerializedName("favorite_content_type")
    val favoriteContentType: String?,

    @SerializedName("favorite_time_slot")
    val favoriteTimeSlot: String?,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class ActivitySyncRequest(
    @SerializedName("activities")
    val activities: List<UserActivityDto>,

    @SerializedName("user_stats")
    val userStats: UserStatsDto?,

    @SerializedName("last_sync_timestamp")
    val lastSyncTimestamp: String?
)

data class ActivitySyncResponse(
    @SerializedName("activities")
    val activities: List<UserActivityDto>,

    @SerializedName("user_stats")
    val userStats: UserStatsDto?,

    @SerializedName("sync_timestamp")
    val syncTimestamp: String,

    @SerializedName("conflicts")
    val conflicts: List<ActivityConflictDto>
)

data class ActivityConflictDto(
    @SerializedName("activity_id")
    val activityId: String,

    @SerializedName("local_activity")
    val localActivity: UserActivityDto,

    @SerializedName("remote_activity")
    val remoteActivity: UserActivityDto,

    @SerializedName("conflict_type")
    val conflictType: String
)

// Extension functions for conversion
fun UserActivityDto.toEntity(): UserActivity {
    return UserActivity(
        id = id,
        userId = userId,
        contentId = contentId,
        sessionType = SessionType.valueOf(sessionType),
        startedAt = LocalDateTime.parse(startedAt),
        completedAt = completedAt?.let { LocalDateTime.parse(it) },
        durationMinutes = durationMinutes,
        isCompleted = isCompleted,
        rating = rating,
        notes = notes,
        streak = streak
    )
}

fun UserActivity.toDto(): UserActivityDto {
    return UserActivityDto(
        id = id,
        userId = userId,
        contentId = contentId,
        sessionType = sessionType.name,
        startedAt = startedAt.toString(),
        completedAt = completedAt?.toString(),
        durationMinutes = durationMinutes,
        isCompleted = isCompleted,
        rating = rating,
        notes = notes,
        streak = streak
    )
}

fun UserStatsDto.toEntity(): UserStats {
    return UserStats(
        userId = userId,
        totalSessions = totalSessionsCompleted,
        totalMinutes = totalMinutesSpent,
        streakDays = currentStreak,
        favoriteCategory = favoriteContentType?.let { Category.valueOf(it) },
        lastSessionDate = lastSessionDate?.let { LocalDateTime.parse(it) },
        updatedAt = LocalDateTime.parse(updatedAt)
    )
}

fun UserStats.toDto(): UserStatsDto {
    return UserStatsDto(
        userId = userId,
        totalSessionsCompleted = totalSessions,
        totalMinutesSpent = totalMinutes,
        currentStreak = streakDays,
        longestStreak = streakDays, // Assuming same as current for now
        lastSessionDate = lastSessionDate?.toString(),
        favoriteContentType = favoriteCategory?.name,
        favoriteTimeSlot = null, // Not available in our entity
        updatedAt = updatedAt.toString()
    )
}