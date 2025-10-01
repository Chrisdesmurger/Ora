package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "user_favorites")
data class UserFavorite(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)