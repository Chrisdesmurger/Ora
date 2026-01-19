package com.ora.wellbeing.core.data.practice

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ora.wellbeing.core.domain.practice.Discipline
import com.ora.wellbeing.core.domain.practice.DownloadInfo
import com.ora.wellbeing.core.domain.practice.DownloadState
import com.ora.wellbeing.core.domain.practice.Level
import com.ora.wellbeing.core.domain.practice.MediaType
import com.ora.wellbeing.core.domain.practice.Practice
import com.ora.wellbeing.data.local.dao.ContentDao
import com.ora.wellbeing.data.model.ContentItem
import com.ora.wellbeing.data.model.firestore.LessonDocument
import com.ora.wellbeing.data.model.firestore.YogaPoseDocument
import com.ora.wellbeing.data.mapper.YogaPoseMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour les pratiques (séances)
 * Now connected to Room/Firestore for real data
 */
@Singleton
class PracticeRepository @Inject constructor(
    private val contentDao: ContentDao,
    private val firebaseStorage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) {

    private val _downloadStates = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    val downloadStates: Flow<Map<String, DownloadInfo>> = _downloadStates.asStateFlow()

    /**
     * Mock data - À remplacer par Firestore
     */
    private val mockPractices = listOf(
        Practice(
            id = "yoga_1",
            title = "Yoga matinal énergisant",
            discipline = Discipline.YOGA,
            level = Level.BEGINNER,
            durationMin = 15,
            description = "Commencez votre journée avec une séance de yoga douce qui réveille le corps et l'esprit. Parfait pour les débutants.",
            mediaType = MediaType.VIDEO,
            mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://picsum.photos/seed/yoga1/400/300",
            tags = listOf("matin", "énergie", "douceur"),
            similarIds = listOf("yoga_2", "yoga_3"),
            instructor = "Sophie Martin",
            benefits = listOf("Éveille le corps", "Améliore la souplesse", "Booste l'énergie")
        ),
        Practice(
            id = "meditation_1",
            title = "Méditation guidée - Calme intérieur",
            discipline = Discipline.MEDITATION,
            level = Level.BEGINNER,
            durationMin = 10,
            description = "Une méditation guidée pour retrouver votre calme intérieur et apaiser l'esprit. Idéale avant le coucher.",
            mediaType = MediaType.AUDIO,
            mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            thumbnailUrl = "https://picsum.photos/seed/meditation1/400/300",
            tags = listOf("soir", "calme", "sommeil"),
            similarIds = listOf("meditation_2", "respiration_1"),
            instructor = "Marc Dubois",
            benefits = listOf("Réduit le stress", "Améliore le sommeil", "Calme l'esprit")
        ),
        Practice(
            id = "respiration_1",
            title = "Respiration 4-7-8 pour l'anxiété",
            discipline = Discipline.RESPIRATION,
            level = Level.BEGINNER,
            durationMin = 5,
            description = "Technique de respiration puissante pour calmer l'anxiété instantanément. Inspirez 4 secondes, retenez 7, expirez 8.",
            mediaType = MediaType.AUDIO,
            mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            thumbnailUrl = "https://picsum.photos/seed/breath1/400/300",
            tags = listOf("anxiété", "calme", "rapide"),
            similarIds = listOf("meditation_1", "respiration_2"),
            instructor = "Claire Moreau",
            benefits = listOf("Calme l'anxiété", "Régule le rythme cardiaque", "Instant")
        ),
        Practice(
            id = "pilates_1",
            title = "Pilates Core Strength",
            discipline = Discipline.PILATES,
            level = Level.INTERMEDIATE,
            durationMin = 20,
            description = "Renforcez votre sangle abdominale avec cette séance de Pilates ciblée. Niveau intermédiaire.",
            mediaType = MediaType.VIDEO,
            mediaUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://picsum.photos/seed/pilates1/400/300",
            tags = listOf("abdos", "force", "core"),
            similarIds = listOf("pilates_2", "yoga_1"),
            instructor = "Laura Bernard",
            benefits = listOf("Renforce le core", "Améliore la posture", "Tonifie")
        )
    )

    /**
     * Récupère une pratique par ID
     * Si l'ID n'existe pas, retourne une pratique de fallback basée sur le type de contenu
     */
    suspend fun getById(id: String): Result<Practice> {
        return try {
            Timber.d("Loading practice from Room: id=$id")

            // Load from Room database (offline-first)
            val content = contentDao.getContentById(id)

            if (content != null) {
                Timber.d("Found content in Room: title=${content.title}")

                // Get signed download URL from Firebase Storage
                val mediaUrl = getSignedDownloadUrl(content.audioUrl, content.videoUrl)

                // Get multilingual descriptions from Firestore (for i18n)
                val i18nDescriptions = getMultilingualDescriptions(id)

                val practice = content.toPractice(
                    signedMediaUrl = mediaUrl,
                    descriptionFr = i18nDescriptions.fr,
                    descriptionEn = i18nDescriptions.en,
                    descriptionEs = i18nDescriptions.es
                )
                Result.success(practice)
            } else {
                // Fallback: try mock data or create fallback
                Timber.w("Content not found in Room for ID: $id, trying mock data")
                val mockPractice = mockPractices.find { it.id == id }
                if (mockPractice != null) {
                    Result.success(mockPractice)
                } else {
                    Timber.w("Practice not found in Room or mock data: $id, using fallback")
                    val fallbackPractice = createFallbackPractice(id)
                    Result.success(fallbackPractice)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading practice $id")
            Result.failure(e)
        }
    }

    /**
     * Data class for multilingual descriptions
     */
    private data class I18nDescriptions(
        val fr: String? = null,
        val en: String? = null,
        val es: String? = null
    )

    /**
     * Fetch multilingual descriptions from Firestore lessons collection
     */
    private suspend fun getMultilingualDescriptions(id: String): I18nDescriptions {
        return try {
            val doc = firestore.collection("lessons").document(id).get().await()
            if (doc.exists()) {
                I18nDescriptions(
                    fr = doc.getString("description_fr"),
                    en = doc.getString("description_en"),
                    es = doc.getString("description_es")
                )
            } else {
                Timber.d("Lesson document not found in Firestore for id: $id")
                I18nDescriptions()
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to fetch multilingual descriptions for $id")
            I18nDescriptions()
        }
    }

    /**
     * Fetch yoga poses for a lesson from Firestore
     *
     * @param lessonId The lesson document ID
     * @return List of YogaPoseDocument, empty if not a yoga lesson or no poses defined
     */
    suspend fun getYogaPoses(lessonId: String): List<YogaPoseDocument> {
        return try {
            Timber.d("Fetching yoga poses for lesson: $lessonId")

            val doc = firestore.collection("lessons").document(lessonId).get().await()

            if (!doc.exists()) {
                Timber.d("Lesson document not found: $lessonId")
                return emptyList()
            }

            // Get raw yoga_poses array from Firestore
            @Suppress("UNCHECKED_CAST")
            val rawPoses = doc.get("yoga_poses") as? List<Map<String, Any>>

            if (rawPoses.isNullOrEmpty()) {
                Timber.d("No yoga poses found for lesson: $lessonId")
                return emptyList()
            }

            // Convert raw maps to YogaPoseDocument objects
            val poses = YogaPoseMapper.fromFirestoreList(rawPoses)
            Timber.d("Loaded ${poses.size} yoga poses for lesson: $lessonId")

            poses
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch yoga poses for lesson: $lessonId")
            emptyList()
        }
    }

    /**
     * Gets signed download URL from Firebase Storage for a storage path
     *
     * Converts Firebase Storage paths (e.g., "media/lessons/ABC/audio/high.m4a")
     * to signed download URLs that allow temporary access without authentication.
     * These URLs expire after a period and are regenerated on each request.
     *
     * @param audioUrl Audio storage path (may be null or a path or an HTTP URL)
     * @param videoUrl Video storage path (may be null or a path or an HTTP URL)
     * @return Signed download URL or fallback URL
     */
    private suspend fun getSignedDownloadUrl(audioUrl: String?, videoUrl: String?): String {
        try {
            // Determine which URL to use (prefer audioUrl for audio lessons, videoUrl for video lessons)
            val storagePath = audioUrl ?: videoUrl

            if (storagePath.isNullOrBlank()) {
                Timber.w("No storage path available, using fallback")
                return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            }

            // If it's already an HTTP URL, return it as-is (for mock data or already resolved URLs)
            if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
                Timber.d("URL already resolved: $storagePath")
                return storagePath
            }

            // Get signed download URL from Firebase Storage
            Timber.d("Getting signed download URL for path: $storagePath")
            val storageRef = firebaseStorage.reference.child(storagePath)
            val downloadUrl = storageRef.downloadUrl.await()
            val signedUrl = downloadUrl.toString()

            Timber.d("Got signed download URL: $signedUrl")
            return signedUrl

        } catch (e: Exception) {
            Timber.e(e, "Failed to get signed download URL, using fallback")
            // Fallback to a working test URL
            return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        }
    }

    /**
     * Crée une pratique de fallback pour les IDs inconnus
     */
    private fun createFallbackPractice(id: String): Practice {
        // Déterminer le type basé sur l'ID
        val (discipline, mediaType) = when {
            id.contains("yoga", ignoreCase = true) -> Discipline.YOGA to MediaType.VIDEO
            id.contains("pilates", ignoreCase = true) -> Discipline.PILATES to MediaType.VIDEO
            id.contains("meditation", ignoreCase = true) -> Discipline.MEDITATION to MediaType.AUDIO
            id.contains("respiration", ignoreCase = true) -> Discipline.RESPIRATION to MediaType.AUDIO
            id.contains("wellness", ignoreCase = true) -> Discipline.WELLNESS to MediaType.AUDIO
            else -> Discipline.MEDITATION to MediaType.AUDIO // Par défaut
        }

        return Practice(
            id = id,
            title = when (discipline) {
                Discipline.YOGA -> "Séance de Yoga"
                Discipline.PILATES -> "Séance de Pilates"
                Discipline.MEDITATION -> "Méditation guidée"
                Discipline.RESPIRATION -> "Exercice de respiration"
                Discipline.WELLNESS -> "Bien-être"
            },
            discipline = discipline,
            level = Level.BEGINNER,
            durationMin = 15,
            description = "Découvrez cette séance apaisante pour votre corps et votre esprit. " +
                    "Prenez un moment pour vous et laissez-vous guider.",
            mediaType = mediaType,
            mediaUrl = if (mediaType == MediaType.VIDEO) {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            } else {
                "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            },
            thumbnailUrl = "https://picsum.photos/seed/$id/400/300",
            tags = listOf("détente", "bien-être"),
            similarIds = mockPractices.take(3).map { it.id },
            instructor = "L'équipe Ora",
            benefits = listOf("Relaxation", "Bien-être", "Harmonie")
        )
    }

    /**
     * Récupère les pratiques similaires
     */
    suspend fun getSimilar(practiceId: String): Result<List<Practice>> {
        return try {
            delay(200)
            val practice = mockPractices.find { it.id == practiceId }
            val similar = practice?.similarIds?.mapNotNull { id ->
                mockPractices.find { it.id == id }
            } ?: emptyList()
            Result.success(similar)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération des pratiques similaires")
            Result.failure(e)
        }
    }

    /**
     * Démarre le téléchargement d'une pratique
     */
    suspend fun startDownload(practiceId: String): Result<Unit> {
        return try {
            val current = _downloadStates.value.toMutableMap()
            current[practiceId] = DownloadInfo(
                practiceId = practiceId,
                state = DownloadState.QUEUED,
                progress = 0f
            )
            _downloadStates.value = current

            // Simulate download progress
            // TODO: Implémenter avec Media3 DownloadManager
            Timber.d("Démarrage du téléchargement de $practiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors du démarrage du téléchargement")
            Result.failure(e)
        }
    }

    /**
     * Annule le téléchargement d'une pratique
     */
    suspend fun cancelDownload(practiceId: String): Result<Unit> {
        return try {
            val current = _downloadStates.value.toMutableMap()
            current.remove(practiceId)
            _downloadStates.value = current
            Timber.d("Téléchargement annulé: $practiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de l'annulation du téléchargement")
            Result.failure(e)
        }
    }

    /**
     * Supprime le téléchargement d'une pratique
     */
    suspend fun deleteDownload(practiceId: String): Result<Unit> {
        return try {
            val current = _downloadStates.value.toMutableMap()
            current.remove(practiceId)
            _downloadStates.value = current
            Timber.d("Téléchargement supprimé: $practiceId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la suppression du téléchargement")
            Result.failure(e)
        }
    }

    /**
     * Récupère l'état de téléchargement d'une pratique
     */
    fun getDownloadState(practiceId: String): Flow<DownloadInfo?> {
        return MutableStateFlow(_downloadStates.value[practiceId]).asStateFlow()
    }

    /**
     * Converts Room Content entity to Practice domain model
     *
     * @param signedMediaUrl Signed download URL from Firebase Storage (obtained via getSignedDownloadUrl)
     * @param descriptionFr French description from Firestore (i18n)
     * @param descriptionEn English description from Firestore (i18n)
     * @param descriptionEs Spanish description from Firestore (i18n)
     */
    private fun com.ora.wellbeing.data.local.entities.Content.toPractice(
        signedMediaUrl: String,
        descriptionFr: String? = null,
        descriptionEn: String? = null,
        descriptionEs: String? = null
    ): Practice {
        // Determine media type based on ContentType
        val mediaType = when (type) {
            com.ora.wellbeing.data.local.entities.ContentType.YOGA -> MediaType.VIDEO
            com.ora.wellbeing.data.local.entities.ContentType.PILATES -> MediaType.VIDEO
            com.ora.wellbeing.data.local.entities.ContentType.MEDITATION -> MediaType.AUDIO
            com.ora.wellbeing.data.local.entities.ContentType.BREATHING -> MediaType.AUDIO
            com.ora.wellbeing.data.local.entities.ContentType.SELF_MASSAGE -> MediaType.VIDEO
            com.ora.wellbeing.data.local.entities.ContentType.BEAUTY_TIPS -> MediaType.VIDEO
        }

        // Determine discipline from category
        val discipline = when (category) {
            com.ora.wellbeing.data.local.entities.Category.FLEXIBILITY -> Discipline.YOGA
            com.ora.wellbeing.data.local.entities.Category.STRENGTH -> Discipline.PILATES
            com.ora.wellbeing.data.local.entities.Category.MINDFULNESS -> Discipline.MEDITATION
            com.ora.wellbeing.data.local.entities.Category.MORNING_ROUTINE -> Discipline.MEDITATION
            com.ora.wellbeing.data.local.entities.Category.STRESS_RELIEF -> Discipline.RESPIRATION
            com.ora.wellbeing.data.local.entities.Category.DAY_BOOST -> Discipline.WELLNESS
            com.ora.wellbeing.data.local.entities.Category.EVENING_WIND_DOWN -> Discipline.WELLNESS
            com.ora.wellbeing.data.local.entities.Category.RELAXATION -> Discipline.WELLNESS
            com.ora.wellbeing.data.local.entities.Category.ENERGY_BOOST -> Discipline.WELLNESS
        }

        // Determine level
        val practiceLevel = when (level) {
            com.ora.wellbeing.data.local.entities.ExperienceLevel.BEGINNER -> Level.BEGINNER
            com.ora.wellbeing.data.local.entities.ExperienceLevel.INTERMEDIATE -> Level.INTERMEDIATE
            com.ora.wellbeing.data.local.entities.ExperienceLevel.ADVANCED -> Level.ADVANCED
        }

        return Practice(
            id = id,
            title = title,
            discipline = discipline,
            level = practiceLevel,
            durationMin = durationMinutes,
            description = description,
            mediaType = mediaType,
            mediaUrl = signedMediaUrl, // Use signed download URL from Firebase Storage
            thumbnailUrl = thumbnailUrl ?: "",
            tags = tags,
            descriptionFr = descriptionFr,
            descriptionEn = descriptionEn,
            descriptionEs = descriptionEs,
            similarIds = emptyList(), // TODO: Implement similar content recommendations
            downloadable = isOfflineAvailable,
            instructor = instructorName,
            benefits = benefits
        )
    }
}
