package com.ora.wellbeing.core.data.ambient

import com.ora.wellbeing.core.domain.ambient.AmbientTrack
import com.ora.wellbeing.core.domain.ambient.AmbientType
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour les pistes d'ambiance
 */
@Singleton
class AmbientRepository @Inject constructor() {

    /**
     * Mock data - pistes d'ambiance
     */
    private val mockTracks = listOf(
        AmbientTrack(
            id = "ocean_1",
            name = "Vagues océaniques",
            type = AmbientType.OCEAN,
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            loop = true,
            defaultVolume = 0.3f
        ),
        AmbientTrack(
            id = "rain_1",
            name = "Pluie douce",
            type = AmbientType.RAIN,
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            loop = true,
            defaultVolume = 0.25f
        ),
        AmbientTrack(
            id = "forest_1",
            name = "Forêt paisible",
            type = AmbientType.FOREST,
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
            loop = true,
            defaultVolume = 0.3f
        ),
        AmbientTrack(
            id = "birds_1",
            name = "Chants d'oiseaux",
            type = AmbientType.BIRDS,
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
            loop = true,
            defaultVolume = 0.35f
        ),
        AmbientTrack(
            id = "fireplace_1",
            name = "Feu de cheminée",
            type = AmbientType.FIREPLACE,
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
            loop = true,
            defaultVolume = 0.3f
        )
    )

    /**
     * Liste toutes les pistes d'ambiance disponibles
     */
    suspend fun list(): Result<List<AmbientTrack>> {
        return try {
            delay(100)
            Result.success(mockTracks)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération des pistes d'ambiance")
            Result.failure(e)
        }
    }

    /**
     * Récupère une piste par ID
     */
    suspend fun getById(id: String): Result<AmbientTrack> {
        return try {
            delay(50)
            val track = mockTracks.find { it.id == id }
            if (track != null) {
                Result.success(track)
            } else {
                Result.failure(Exception("Piste d'ambiance non trouvée"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération de la piste $id")
            Result.failure(e)
        }
    }

    /**
     * Récupère une piste par type
     */
    suspend fun getByType(type: AmbientType): Result<AmbientTrack?> {
        return try {
            delay(50)
            val track = mockTracks.find { it.type == type }
            Result.success(track)
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors de la récupération de la piste de type $type")
            Result.failure(e)
        }
    }
}
