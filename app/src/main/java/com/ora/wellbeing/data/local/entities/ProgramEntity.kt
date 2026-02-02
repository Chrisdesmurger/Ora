package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * ProgramEntity - Room database entity for programs
 *
 * Represents a meditation/yoga program stored locally for offline access.
 * Synced from Firestore "programs" collection via ProgramMapper.
 *
 * Note: Named ProgramEntity to avoid conflict with data.model.Program (Firestore model)
 */
@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey
    val id: String,

    // Basic Information
    val title: String,
    val description: String,
    val category: String, // French: "Méditation", "Yoga", "Pleine Conscience", "Bien-être"
    val level: String, // French: "Débutant", "Intermédiaire", "Avancé"

    // Duration
    val durationDays: Int,

    // Media
    val thumbnailUrl: String? = null,
    val instructor: String? = null,

    // Status
    val isActive: Boolean = true, // Corresponds to status == "published" in Firestore
    val isPremiumOnly: Boolean = false,

    // Statistics
    val participantCount: Int = 0,
    val rating: Float = 0.0f,

    // Lessons
    val lessonIds: List<String> = emptyList(), // IDs of lessons in this program, in order

    // Metadata
    val tags: List<String> = emptyList(),

    // Sync tracking
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)
