package com.ora.wellbeing.core.domain.ambient

/**
 * Type de piste ambiante
 */
enum class AmbientType(val displayName: String, val emoji: String) {
    OCEAN("Océan", "🌊"),
    RAIN("Pluie", "🌧️"),
    FOREST("Forêt", "🌲"),
    BIRDS("Oiseaux", "🐦"),
    FIREPLACE("Feu de cheminée", "🔥"),
    NONE("Aucun", "🔇")
}

/**
 * Modèle de piste d'ambiance
 */
data class AmbientTrack(
    val id: String,
    val name: String,
    val type: AmbientType,
    val url: String,
    val loop: Boolean = true,
    val defaultVolume: Float = 0.3f
)
