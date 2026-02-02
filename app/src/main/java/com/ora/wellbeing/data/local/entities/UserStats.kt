package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val userId: String,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val streakDays: Int = 0,
    val favoriteCategory: Category? = null,
    val lastSessionDate: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)