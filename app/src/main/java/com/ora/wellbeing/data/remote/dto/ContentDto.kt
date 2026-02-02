package com.ora.wellbeing.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ora.wellbeing.data.local.entities.*
import java.time.LocalDateTime

data class ContentDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("duration_minutes")
    val durationMinutes: Int,

    @SerializedName("level")
    val level: String,

    @SerializedName("video_url")
    val videoUrl: String?,

    @SerializedName("audio_url")
    val audioUrl: String?,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,

    @SerializedName("instructor_name")
    val instructorName: String?,

    @SerializedName("tags")
    val tags: List<String>,

    @SerializedName("is_flash_session")
    val isFlashSession: Boolean,

    @SerializedName("equipment")
    val equipment: List<String>,

    @SerializedName("benefits")
    val benefits: List<String>,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("is_premium")
    val isPremium: Boolean = false,

    @SerializedName("download_size")
    val downloadSize: Long?
)

data class ContentSyncRequest(
    @SerializedName("last_sync_timestamp")
    val lastSyncTimestamp: String?,

    @SerializedName("user_preferences")
    val userPreferences: UserPreferencesDto?
)

data class ContentSyncResponse(
    @SerializedName("content")
    val content: List<ContentDto>,

    @SerializedName("deleted_content_ids")
    val deletedContentIds: List<String>,

    @SerializedName("sync_timestamp")
    val syncTimestamp: String
)

data class UserPreferencesDto(
    @SerializedName("preferred_types")
    val preferredTypes: List<String>,

    @SerializedName("preferred_duration")
    val preferredDuration: Int,

    @SerializedName("experience_level")
    val experienceLevel: String
)

// Extension functions for conversion
fun ContentDto.toEntity(): Content {
    return Content(
        id = id,
        title = title,
        description = description,
        type = ContentType.valueOf(type),
        category = Category.valueOf(category),
        durationMinutes = durationMinutes,
        level = ExperienceLevel.valueOf(level),
        videoUrl = videoUrl,
        audioUrl = audioUrl,
        thumbnailUrl = thumbnailUrl,
        instructorName = instructorName,
        tags = tags,
        isFlashSession = isFlashSession,
        equipment = equipment,
        benefits = benefits,
        createdAt = LocalDateTime.parse(createdAt),
        isOfflineAvailable = false, // Will be determined by download status
        downloadSize = downloadSize
    )
}

fun Content.toDto(): ContentDto {
    return ContentDto(
        id = id,
        title = title,
        description = description,
        type = type.name,
        category = category.name,
        durationMinutes = durationMinutes,
        level = level.name,
        videoUrl = videoUrl,
        audioUrl = audioUrl,
        thumbnailUrl = thumbnailUrl,
        instructorName = instructorName,
        tags = tags,
        isFlashSession = isFlashSession,
        equipment = equipment,
        benefits = benefits,
        createdAt = createdAt.toString(),
        updatedAt = createdAt.toString(),
        downloadSize = downloadSize
    )
}