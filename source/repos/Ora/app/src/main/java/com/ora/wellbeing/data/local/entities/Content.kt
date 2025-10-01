package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "content")
data class Content(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: ContentType,
    val category: Category,
    val durationMinutes: Int,
    val level: ExperienceLevel,
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val thumbnailUrl: String? = null,
    val instructorName: String? = null,
    val tags: List<String> = emptyList(),
    val isFlashSession: Boolean = false,
    val equipment: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isOfflineAvailable: Boolean = false,
    val downloadSize: Long? = null
)

enum class ContentType {
    YOGA, PILATES, MEDITATION, BREATHING, SELF_MASSAGE, BEAUTY_TIPS
}

enum class Category {
    MORNING_ROUTINE, DAY_BOOST, EVENING_WIND_DOWN,
    STRESS_RELIEF, FLEXIBILITY, STRENGTH,
    MINDFULNESS, RELAXATION, ENERGY_BOOST
}