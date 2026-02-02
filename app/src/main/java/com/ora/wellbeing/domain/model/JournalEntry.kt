package com.ora.wellbeing.domain.model

import java.time.LocalDate

data class JournalEntry(
    val id: String,
    val date: LocalDate,
    val mood: Mood?,
    val story: String,
    val gratitudes: List<String>,
    val accomplishments: List<String>,
    val improvements: List<String>,
    val learnings: String,
    val trackers: Map<String, Boolean> = emptyMap()
)

enum class Mood(val emoji: String, val label: String) {
    HAPPY("😊", "Heureux"),
    GOOD("😌", "Bien"),
    NEUTRAL("😐", "Neutre"),
    SAD("😔", "Triste")
}

data class Gratitude(
    val id: String,
    val text: String,
    val color: String,
    val date: LocalDate
)

data class HabitTracker(
    val id: String,
    val name: String,
    val color: String,
    val completedDates: Set<LocalDate>
)