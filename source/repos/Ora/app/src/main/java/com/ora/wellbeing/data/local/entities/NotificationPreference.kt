package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notification_preferences")
data class NotificationPreference(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: NotificationType,
    val isEnabled: Boolean = true,
    val scheduledTime: String? = null, // Format HH:mm
    val frequency: NotificationFrequency = NotificationFrequency.DAILY,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    DAILY_REMINDER,
    SESSION_REMINDER,
    STREAK_MOTIVATION,
    NEW_CONTENT,
    WELLBEING_TIP
}

enum class NotificationFrequency {
    DAILY,
    WEEKLY,
    CUSTOM,
    DISABLED
}