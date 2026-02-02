package com.ora.wellbeing.data.repository

import com.ora.wellbeing.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

interface OraRepository {
    suspend fun getFeaturedVideo(): VideoContent?
    suspend fun getVideosByCategory(category: ContentCategory): List<VideoContent>
    suspend fun getAllVideos(): List<VideoContent>
    suspend fun getTodayGratitudes(): List<Gratitude>
    suspend fun saveGratitude(gratitude: Gratitude)
    suspend fun getTodayJournalEntry(): JournalEntry?
    suspend fun saveJournalEntry(entry: JournalEntry)
    suspend fun getHabitTrackers(): List<HabitTracker>
    suspend fun toggleHabit(habitId: String, date: LocalDate)
    fun getJournalEntries(): Flow<List<JournalEntry>>
}

@Singleton
class OraRepositoryImpl @Inject constructor() : OraRepository {

    // Mock data - Dans une vraie app, cela viendrait d'une base de données ou API
    private val mockVideos = listOf(
        VideoContent(
            id = "morning_yoga",
            title = "Morning Yoga Flow",
            description = "Une séance douce pour commencer la journée",
            thumbnailUrl = "https://picsum.photos/400/300?random=1",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            duration = "15 min",
            category = ContentCategory.YOGA,
            isFeatured = true
        ),
        VideoContent(
            id = "meditation_1",
            title = "Méditation du matin",
            description = "Commencez votre journée en douceur",
            thumbnailUrl = "https://picsum.photos/400/300?random=2",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            duration = "10 min",
            category = ContentCategory.MEDITATION,
            isNew = true
        ),
        VideoContent(
            id = "meditation_2",
            title = "Pleine conscience",
            description = "Méditation pour l'attention",
            thumbnailUrl = "https://picsum.photos/400/300?random=3",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            duration = "15 min",
            category = ContentCategory.MEDITATION
        ),
        VideoContent(
            id = "meditation_3",
            title = "Méditation du soir",
            description = "Détendez-vous avant le coucher",
            thumbnailUrl = "https://picsum.photos/400/300?random=4",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            duration = "20 min",
            category = ContentCategory.MEDITATION
        ),
        VideoContent(
            id = "meditation_4",
            title = "Gestion du stress",
            description = "Techniques anti-stress",
            thumbnailUrl = "https://picsum.photos/400/300?random=5",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            duration = "12 min",
            category = ContentCategory.MEDITATION,
            isNew = true
        ),
        VideoContent(
            id = "yoga_1",
            title = "Yoga matinal",
            description = "Réveil en douceur",
            thumbnailUrl = "https://picsum.photos/400/300?random=6",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            duration = "25 min",
            category = ContentCategory.YOGA
        ),
        VideoContent(
            id = "yoga_2",
            title = "Hatha Yoga",
            description = "Postures traditionnelles",
            thumbnailUrl = "https://picsum.photos/400/300?random=7",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            duration = "30 min",
            category = ContentCategory.YOGA,
            isNew = true
        ),
        VideoContent(
            id = "yoga_3",
            title = "Yoga réparateur",
            description = "Détente profonde",
            thumbnailUrl = "https://picsum.photos/400/300?random=8",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4",
            duration = "45 min",
            category = ContentCategory.YOGA
        ),
        VideoContent(
            id = "yoga_4",
            title = "Vinyasa Flow",
            description = "Enchaînements fluides",
            thumbnailUrl = "https://picsum.photos/400/300?random=9",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            duration = "35 min",
            category = ContentCategory.YOGA
        )
    )

    override suspend fun getFeaturedVideo(): VideoContent? {
        return mockVideos.firstOrNull { it.isFeatured }
    }

    override suspend fun getVideosByCategory(category: ContentCategory): List<VideoContent> {
        return mockVideos.filter { it.category == category }
    }

    override suspend fun getAllVideos(): List<VideoContent> {
        return mockVideos
    }

    override suspend fun getTodayGratitudes(): List<Gratitude> {
        return listOf(
            Gratitude(
                id = "1",
                text = "Ma famille qui me soutient",
                color = "#E8F5E8",
                date = LocalDate.now()
            ),
            Gratitude(
                id = "2",
                text = "Le soleil de ce matin",
                color = "#FFF3E0",
                date = LocalDate.now()
            ),
            Gratitude(
                id = "3",
                text = "Mon café délicieux",
                color = "#E3F2FD",
                date = LocalDate.now()
            )
        )
    }

    override suspend fun saveGratitude(gratitude: Gratitude) {
        // TODO: Implémenter la sauvegarde
    }

    override suspend fun getTodayJournalEntry(): JournalEntry? {
        return JournalEntry(
            id = "today",
            date = LocalDate.now(),
            mood = null,
            story = "",
            gratitudes = emptyList(),
            accomplishments = emptyList(),
            improvements = emptyList(),
            learnings = ""
        )
    }

    override suspend fun saveJournalEntry(entry: JournalEntry) {
        // TODO: Implémenter la sauvegarde
    }

    override suspend fun getHabitTrackers(): List<HabitTracker> {
        return listOf(
            HabitTracker(
                id = "meditation",
                name = "Méditation",
                color = "#C4A8CB",
                completedDates = setOf(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(3),
                    LocalDate.now().minusDays(5)
                )
            ),
            HabitTracker(
                id = "exercise",
                name = "Exercice",
                color = "#9DB5A6",
                completedDates = setOf(
                    LocalDate.now().minusDays(2),
                    LocalDate.now().minusDays(4)
                )
            ),
            HabitTracker(
                id = "reading",
                name = "Lecture",
                color = "#F2C2A7",
                completedDates = setOf(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(2),
                    LocalDate.now().minusDays(6)
                )
            )
        )
    }

    override suspend fun toggleHabit(habitId: String, date: LocalDate) {
        // TODO: Implémenter le toggle des habits
    }

    override fun getJournalEntries(): Flow<List<JournalEntry>> {
        return flowOf(emptyList()) // TODO: Implémenter
    }
}