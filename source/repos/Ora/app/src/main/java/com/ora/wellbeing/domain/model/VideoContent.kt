package com.ora.wellbeing.domain.model

data class VideoContent(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: String,
    val category: ContentCategory,
    val isNew: Boolean = false,
    val isFeatured: Boolean = false
)

enum class ContentCategory(val displayName: String) {
    YOGA("Yoga"),
    PILATES("Pilates"),
    MEDITATION("Méditation"),
    BREATHING("Respiration")
}

data class ContentFilter(
    val category: ContentCategory? = null,
    val isNew: Boolean = false,
    val duration: DurationFilter? = null
)

enum class DurationFilter(val displayName: String, val maxMinutes: Int) {
    SHORT("Courte (< 10 min)", 10),
    MEDIUM("Moyenne (10-30 min)", 30),
    LONG("Longue (> 30 min)", Int.MAX_VALUE)
}