package com.ora.wellbeing.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.ora.wellbeing.data.local.entities.JournalEntry
import com.ora.wellbeing.data.local.entities.Mood
import java.time.LocalDate
import java.time.LocalDateTime

data class JournalEntryDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("gratitude_1")
    val gratitude1: String,

    @SerializedName("gratitude_2")
    val gratitude2: String,

    @SerializedName("gratitude_3")
    val gratitude3: String,

    @SerializedName("mood")
    val mood: String,

    @SerializedName("day_story")
    val dayStory: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

data class JournalSyncRequest(
    @SerializedName("entries")
    val entries: List<JournalEntryDto>,

    @SerializedName("last_sync_timestamp")
    val lastSyncTimestamp: String?
)

data class JournalSyncResponse(
    @SerializedName("entries")
    val entries: List<JournalEntryDto>,

    @SerializedName("sync_timestamp")
    val syncTimestamp: String,

    @SerializedName("conflicts")
    val conflicts: List<JournalConflictDto>
)

data class JournalConflictDto(
    @SerializedName("entry_id")
    val entryId: String,

    @SerializedName("local_entry")
    val localEntry: JournalEntryDto,

    @SerializedName("remote_entry")
    val remoteEntry: JournalEntryDto,

    @SerializedName("conflict_type")
    val conflictType: String // "UPDATE_CONFLICT", "DELETE_CONFLICT"
)

// Extension functions for conversion
fun JournalEntryDto.toEntity(): JournalEntry {
    return JournalEntry(
        id = id,
        userId = userId,
        date = LocalDate.parse(date),
        gratitude1 = gratitude1,
        gratitude2 = gratitude2,
        gratitude3 = gratitude3,
        mood = Mood.valueOf(mood),
        dayStory = dayStory,
        createdAt = LocalDateTime.parse(createdAt),
        updatedAt = LocalDateTime.parse(updatedAt)
    )
}

fun JournalEntry.toDto(): JournalEntryDto {
    return JournalEntryDto(
        id = id,
        userId = userId,
        date = date.toString(),
        gratitude1 = gratitude1,
        gratitude2 = gratitude2,
        gratitude3 = gratitude3,
        mood = mood.name,
        dayStory = dayStory,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}