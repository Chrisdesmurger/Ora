package com.ora.wellbeing.core.data.practice

import com.ora.wellbeing.core.domain.practice.Discipline
import com.ora.wellbeing.core.domain.practice.DownloadInfo
import com.ora.wellbeing.core.domain.practice.DownloadState
import com.ora.wellbeing.core.domain.practice.Level
import com.ora.wellbeing.core.domain.practice.MediaType
import com.ora.wellbeing.core.domain.practice.Practice
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour les pratiques (séances)
 * TODO: Connecter à Firestore pour les vraies données
 */
@Singleton
class PracticeRepository @Inject constructor() {

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
            delay(300) // Simulate network delay
            val practice = mockPractices.find { it.id == id }
            if (practice != null) {
                Result.success(practice)
            } else {
                // Fallback: retourner une pratique générique selon l'ID
                Timber.w("Pratique non trouvée pour ID: $id, utilisation du fallback")
                val fallbackPractice = createFallbackPractice(id)
                Result.success(fallbackPractice)
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération de la pratique $id")
            Result.failure(e)
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
}
