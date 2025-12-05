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
    val previewImageUrl: String? = null,
    val instructorName: String? = null,
    val tags: List<String> = emptyList(),
    val isFlashSession: Boolean = false,
    val equipment: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isOfflineAvailable: Boolean = false,
    val downloadSize: Long? = null,

    // NEW FIELDS - Phase 2.2: Firestore sync support
    val programId: String? = null, // ID of program this lesson belongs to
    val order: Int = 0, // Order/position within program (0-indexed)
    val status: String = "ready", // Firestore status: draft|uploading|processing|ready|failed
    val updatedAt: Long = System.currentTimeMillis() // Timestamp for sync tracking
)

enum class ContentType {
    YOGA, PILATES, MEDITATION, BREATHING, SELF_MASSAGE, BEAUTY_TIPS
}

enum class Category {
    MORNING_ROUTINE, DAY_BOOST, EVENING_WIND_DOWN,
    STRESS_RELIEF, FLEXIBILITY, STRENGTH,
    MINDFULNESS, RELAXATION, ENERGY_BOOST
}