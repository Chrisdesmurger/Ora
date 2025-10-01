package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val gratitude1: String,
    val gratitude2: String,
    val gratitude3: String,
    val mood: Mood,
    val dayStory: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Mood(val emoji: String, val label: String) {
    VERY_SAD("😢", "Très triste"),
    SAD("😔", "Triste"),
    NEUTRAL("😐", "Neutre"),
    HAPPY("😊", "Heureux"),
    VERY_HAPPY("😄", "Très heureux"),
    EXCITED("🤩", "Enthousiaste"),
    PEACEFUL("😌", "Paisible"),
    GRATEFUL("🙏", "Reconnaissant")
}