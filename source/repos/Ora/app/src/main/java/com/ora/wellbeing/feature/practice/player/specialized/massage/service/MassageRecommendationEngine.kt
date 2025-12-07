package com.ora.wellbeing.feature.practice.player.specialized.massage.service

import com.ora.wellbeing.data.repository.MassageHistoryRepository
import com.ora.wellbeing.data.repository.MassagePreferenceRepository
import com.ora.wellbeing.feature.practice.player.specialized.massage.BodyZone
import com.ora.wellbeing.feature.practice.player.specialized.massage.PressureLevel
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine for generating massage recommendations based on user history
 *
 * Provides:
 * - Suggested routines based on past sessions
 * - Zone recommendations based on usage patterns
 * - Time-based suggestions (morning vs evening)
 * - Personalized circuit recommendations
 */
@Singleton
class MassageRecommendationEngine @Inject constructor(
    private val historyRepository: MassageHistoryRepository,
    private val preferenceRepository: MassagePreferenceRepository
) {

    /**
     * Recommendation data class
     */
    data class MassageRecommendation(
        val type: RecommendationType,
        val title: String,
        val description: String,
        val suggestedZones: List<String>,
        val suggestedDurationMs: Long,
        val suggestedPressureLevel: PressureLevel,
        val confidence: Float // 0.0 - 1.0
    )

    enum class RecommendationType {
        QUICK_RELIEF,       // Fast session for tension relief
        FULL_BODY,          // Complete body massage
        FOCUS_AREA,         // Focus on specific areas needing attention
        MORNING_ROUTINE,    // Energizing morning routine
        EVENING_ROUTINE,    // Relaxing evening routine
        NEGLECTED_ZONES,    // Zones not massaged recently
        FAVORITE_ZONES,     // User's favorite zones
        REPEAT_LAST         // Repeat last successful session
    }

    /**
     * Get personalized recommendations for a user
     */
    suspend fun getRecommendations(
        userId: String,
        availableZones: List<BodyZone>,
        limit: Int = 5
    ): List<MassageRecommendation> {
        val recommendations = mutableListOf<MassageRecommendation>()

        // Get user data
        val recentSessions = historyRepository.getRecentSessions(userId, 10).first()
        val preferences = preferenceRepository.getAllPreferences(userId).first()
        val zoneUsageStats = historyRepository.getZoneUsageStats(userId)
        val mostUsedPressure = historyRepository.getMostUsedPressureLevel(userId)

        // Time-based recommendation
        recommendations.add(getTimeBasedRecommendation(availableZones, mostUsedPressure))

        // Neglected zones recommendation
        val neglectedZonesRec = getNeglectedZonesRecommendation(
            userId,
            availableZones,
            preferences.associate { it.zoneId to it.lastMassagedAt },
            mostUsedPressure
        )
        if (neglectedZonesRec != null) {
            recommendations.add(neglectedZonesRec)
        }

        // Favorite zones recommendation
        val favoriteZones = preferences.filter { it.isFavoriteZone }.map { it.zoneId }
        if (favoriteZones.isNotEmpty()) {
            recommendations.add(
                MassageRecommendation(
                    type = RecommendationType.FAVORITE_ZONES,
                    title = "Vos zones preferees",
                    description = "Une session focalisee sur vos zones favorites",
                    suggestedZones = favoriteZones,
                    suggestedDurationMs = favoriteZones.size * 60000L,
                    suggestedPressureLevel = mostUsedPressure ?: PressureLevel.MEDIUM,
                    confidence = 0.9f
                )
            )
        }

        // Quick relief recommendation
        recommendations.add(getQuickReliefRecommendation(availableZones, zoneUsageStats))

        // Full body recommendation
        recommendations.add(
            MassageRecommendation(
                type = RecommendationType.FULL_BODY,
                title = "Massage complet",
                description = "Une session complete pour tout le corps",
                suggestedZones = availableZones.map { it.id },
                suggestedDurationMs = availableZones.sumOf { it.duration },
                suggestedPressureLevel = PressureLevel.MEDIUM,
                confidence = 0.7f
            )
        )

        // Repeat last successful session
        val lastSession = recentSessions.firstOrNull { it.isCompleted && (it.rating ?: 0) >= 4 }
        if (lastSession != null) {
            try {
                val lastZones: List<String> = kotlinx.serialization.json.Json.decodeFromString(lastSession.completedZoneIds)
                recommendations.add(
                    MassageRecommendation(
                        type = RecommendationType.REPEAT_LAST,
                        title = "Repeter votre derniere session",
                        description = "Reproduire votre session reussie",
                        suggestedZones = lastZones,
                        suggestedDurationMs = lastSession.totalDurationMs,
                        suggestedPressureLevel = try {
                            PressureLevel.valueOf(lastSession.averagePressureLevel)
                        } catch (_: Exception) {
                            PressureLevel.MEDIUM
                        },
                        confidence = 0.85f
                    )
                )
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }

        return recommendations
            .sortedByDescending { it.confidence }
            .take(limit)
    }

    /**
     * Get time-based recommendation (morning vs evening)
     */
    private fun getTimeBasedRecommendation(
        availableZones: List<BodyZone>,
        preferredPressure: PressureLevel?
    ): MassageRecommendation {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return if (hour < 12) {
            // Morning routine - energizing
            MassageRecommendation(
                type = RecommendationType.MORNING_ROUTINE,
                title = "Routine matinale",
                description = "Reveillez votre corps avec cette routine energisante",
                suggestedZones = listOf("neck", "shoulders", "arms", "hands"),
                suggestedDurationMs = 5 * 60000L, // 5 minutes
                suggestedPressureLevel = PressureLevel.MEDIUM,
                confidence = 0.75f
            )
        } else if (hour >= 18) {
            // Evening routine - relaxing
            MassageRecommendation(
                type = RecommendationType.EVENING_ROUTINE,
                title = "Routine du soir",
                description = "Detendez-vous apres votre journee",
                suggestedZones = listOf("neck", "shoulders", "back"),
                suggestedDurationMs = 10 * 60000L, // 10 minutes
                suggestedPressureLevel = PressureLevel.LOW,
                confidence = 0.8f
            )
        } else {
            // Afternoon - quick relief
            MassageRecommendation(
                type = RecommendationType.QUICK_RELIEF,
                title = "Pause detente",
                description = "Une pause bien-etre au milieu de la journee",
                suggestedZones = listOf("neck", "shoulders"),
                suggestedDurationMs = 3 * 60000L, // 3 minutes
                suggestedPressureLevel = preferredPressure ?: PressureLevel.MEDIUM,
                confidence = 0.7f
            )
        }
    }

    /**
     * Get recommendation for neglected zones
     */
    private suspend fun getNeglectedZonesRecommendation(
        userId: String,
        availableZones: List<BodyZone>,
        lastMassagedTimes: Map<String, Long?>,
        preferredPressure: PressureLevel?
    ): MassageRecommendation? {
        val now = System.currentTimeMillis()
        val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000L)

        // Find zones not massaged in the last week
        val neglectedZones = availableZones.filter { zone ->
            val lastMassaged = lastMassagedTimes[zone.id]
            lastMassaged == null || lastMassaged < oneWeekAgo
        }

        if (neglectedZones.isEmpty()) return null

        return MassageRecommendation(
            type = RecommendationType.NEGLECTED_ZONES,
            title = "Zones a ne pas oublier",
            description = "Ces zones n'ont pas ete massees depuis longtemps",
            suggestedZones = neglectedZones.map { it.id },
            suggestedDurationMs = neglectedZones.size * 60000L,
            suggestedPressureLevel = preferredPressure ?: PressureLevel.MEDIUM,
            confidence = 0.65f
        )
    }

    /**
     * Get quick relief recommendation based on most common tension areas
     */
    private fun getQuickReliefRecommendation(
        availableZones: List<BodyZone>,
        zoneUsageStats: Map<String, Int>
    ): MassageRecommendation {
        // Prioritize commonly used zones (likely tension areas)
        val prioritizedZones = if (zoneUsageStats.isNotEmpty()) {
            zoneUsageStats.entries
                .sortedByDescending { it.value }
                .take(2)
                .map { it.key }
        } else {
            // Default to neck and shoulders
            listOf("neck", "shoulders")
        }

        return MassageRecommendation(
            type = RecommendationType.QUICK_RELIEF,
            title = "Soulagement rapide",
            description = "Ciblez vos zones de tension habituelles",
            suggestedZones = prioritizedZones,
            suggestedDurationMs = prioritizedZones.size * 45000L, // 45 seconds per zone
            suggestedPressureLevel = PressureLevel.MEDIUM,
            confidence = 0.72f
        )
    }

    /**
     * Generate circuit routine based on recommendations
     */
    suspend fun generateCircuitRoutine(
        userId: String,
        recommendation: MassageRecommendation,
        allZones: List<BodyZone>
    ): List<BodyZone> {
        // Filter and order zones based on recommendation
        val zoneMap = allZones.associateBy { it.id }
        val preferences = preferenceRepository.getAllPreferences(userId).first()
        val prefMap = preferences.associateBy { it.zoneId }

        return recommendation.suggestedZones.mapNotNull { zoneId ->
            val zone = zoneMap[zoneId] ?: return@mapNotNull null
            val pref = prefMap[zoneId]

            // Apply user preferences if available
            if (pref != null) {
                zone.copy(
                    duration = pref.customDurationMs,
                    pressureRecommended = try {
                        PressureLevel.valueOf(pref.preferredPressureLevel)
                    } catch (_: Exception) {
                        zone.pressureRecommended
                    }
                )
            } else {
                zone
            }
        }
    }

    /**
     * Get zone suggestion based on current time and last session
     */
    suspend fun getSingleZoneSuggestion(
        userId: String,
        availableZones: List<BodyZone>
    ): BodyZone? {
        // Try to get least recently massaged zone
        val leastRecentZoneId = preferenceRepository.getLeastRecentlyMassagedZoneId(userId)

        if (leastRecentZoneId != null) {
            return availableZones.find { it.id == leastRecentZoneId }
        }

        // Fallback: return neck (most common tension area)
        return availableZones.find { it.id == "neck" } ?: availableZones.firstOrNull()
    }
}
