package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.MassagePreferenceDao
import com.ora.wellbeing.data.local.entities.MassagePreferenceEntity
import com.ora.wellbeing.feature.practice.player.specialized.massage.BodyZone
import com.ora.wellbeing.feature.practice.player.specialized.massage.PressureLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing massage preferences
 *
 * Provides methods to:
 * - Get/set custom durations per zone
 * - Get/set preferred pressure levels
 * - Track favorite zones
 * - Manage haptic and voice settings
 */
@Singleton
class MassagePreferenceRepository @Inject constructor(
    private val massagePreferenceDao: MassagePreferenceDao
) {

    /**
     * Get all preferences for a user
     */
    fun getAllPreferences(userId: String): Flow<List<MassagePreferenceEntity>> {
        return massagePreferenceDao.getAllForUser(userId)
    }

    /**
     * Get preference for a specific zone
     */
    suspend fun getZonePreference(userId: String, zoneId: String): MassagePreferenceEntity? {
        return massagePreferenceDao.getByZone(userId, zoneId)
    }

    /**
     * Get preference for a specific zone as Flow
     */
    fun getZonePreferenceFlow(userId: String, zoneId: String): Flow<MassagePreferenceEntity?> {
        return massagePreferenceDao.getByZoneFlow(userId, zoneId)
    }

    /**
     * Get favorite zones
     */
    fun getFavoriteZones(userId: String): Flow<List<MassagePreferenceEntity>> {
        return massagePreferenceDao.getFavoriteZones(userId)
    }

    /**
     * Get most massaged zones (for recommendations)
     */
    fun getMostMassagedZones(userId: String): Flow<List<MassagePreferenceEntity>> {
        return massagePreferenceDao.getMostMassagedZones(userId)
    }

    /**
     * Initialize default preferences for all zones
     */
    suspend fun initializeDefaultPreferences(userId: String, zones: List<BodyZone>) {
        val preferences = zones.map { zone ->
            MassagePreferenceEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                zoneId = zone.id,
                customDurationMs = zone.duration,
                preferredPressureLevel = zone.pressureRecommended.name,
                preferredRepetitions = 3,
                pauseBetweenZonesMs = 5000L,
                isFavoriteZone = false,
                hapticFeedbackEnabled = true,
                voiceInstructionsEnabled = true
            )
        }
        massagePreferenceDao.insertAll(preferences)
    }

    /**
     * Save or update a zone preference
     */
    suspend fun savePreference(preference: MassagePreferenceEntity) {
        massagePreferenceDao.insert(preference)
    }

    /**
     * Update zone duration
     */
    suspend fun updateZoneDuration(userId: String, zoneId: String, durationMs: Long) {
        val existing = massagePreferenceDao.getByZone(userId, zoneId)
        if (existing != null) {
            massagePreferenceDao.updateZoneDuration(userId, zoneId, durationMs)
        } else {
            // Create new preference with default values
            val newPref = MassagePreferenceEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                zoneId = zoneId,
                customDurationMs = durationMs,
                preferredPressureLevel = PressureLevel.MEDIUM.name
            )
            massagePreferenceDao.insert(newPref)
        }
    }

    /**
     * Update zone pressure level
     */
    suspend fun updateZonePressureLevel(userId: String, zoneId: String, pressureLevel: PressureLevel) {
        val existing = massagePreferenceDao.getByZone(userId, zoneId)
        if (existing != null) {
            massagePreferenceDao.updateZonePressureLevel(userId, zoneId, pressureLevel.name)
        } else {
            val newPref = MassagePreferenceEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                zoneId = zoneId,
                customDurationMs = 60000L, // Default 1 minute
                preferredPressureLevel = pressureLevel.name
            )
            massagePreferenceDao.insert(newPref)
        }
    }

    /**
     * Update zone repetitions
     */
    suspend fun updateZoneRepetitions(userId: String, zoneId: String, repetitions: Int) {
        massagePreferenceDao.updateZoneRepetitions(userId, zoneId, repetitions)
    }

    /**
     * Update pause between zones
     */
    suspend fun updatePauseBetweenZones(userId: String, zoneId: String, pauseMs: Long) {
        massagePreferenceDao.updatePauseBetweenZones(userId, zoneId, pauseMs)
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(userId: String, zoneId: String) {
        val existing = massagePreferenceDao.getByZone(userId, zoneId)
        if (existing != null) {
            massagePreferenceDao.updateFavoriteStatus(userId, zoneId, !existing.isFavoriteZone)
        }
    }

    /**
     * Update haptic feedback setting
     */
    suspend fun updateHapticFeedback(userId: String, zoneId: String, enabled: Boolean) {
        massagePreferenceDao.updateHapticFeedback(userId, zoneId, enabled)
    }

    /**
     * Update voice instructions setting
     */
    suspend fun updateVoiceInstructions(userId: String, zoneId: String, enabled: Boolean) {
        massagePreferenceDao.updateVoiceInstructions(userId, zoneId, enabled)
    }

    /**
     * Update all zones haptic feedback
     */
    suspend fun updateAllHapticFeedback(userId: String, enabled: Boolean) {
        massagePreferenceDao.updateAllHapticFeedback(userId, enabled)
    }

    /**
     * Update all zones voice instructions
     */
    suspend fun updateAllVoiceInstructions(userId: String, enabled: Boolean) {
        massagePreferenceDao.updateAllVoiceInstructions(userId, enabled)
    }

    /**
     * Record that a zone was massaged (for analytics)
     */
    suspend fun recordZoneMassaged(userId: String, zoneId: String) {
        massagePreferenceDao.recordZoneMassaged(userId, zoneId)
    }

    /**
     * Get the most massaged zone ID
     */
    suspend fun getMostMassagedZoneId(userId: String): String? {
        return massagePreferenceDao.getMostMassagedZoneId(userId)
    }

    /**
     * Get the least recently massaged zone (for suggestions)
     */
    suspend fun getLeastRecentlyMassagedZoneId(userId: String): String? {
        return massagePreferenceDao.getLeastRecentlyMassagedZoneId(userId)
    }

    /**
     * Delete all preferences for a user
     */
    suspend fun deleteAllForUser(userId: String) {
        massagePreferenceDao.deleteAllForUser(userId)
    }

    /**
     * Apply user preferences to body zones
     */
    fun applyPreferencesToZones(
        zones: List<BodyZone>,
        preferences: List<MassagePreferenceEntity>
    ): List<BodyZone> {
        val prefMap = preferences.associateBy { it.zoneId }
        return zones.map { zone ->
            val pref = prefMap[zone.id]
            if (pref != null) {
                zone.copy(
                    duration = pref.customDurationMs,
                    pressureRecommended = PressureLevel.valueOf(pref.preferredPressureLevel)
                )
            } else {
                zone
            }
        }
    }
}
