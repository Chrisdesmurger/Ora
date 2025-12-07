package com.ora.wellbeing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ora.wellbeing.data.local.entities.MassagePreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for massage preferences
 *
 * Manages user preferences for each body zone including
 * custom durations, pressure levels, and personalized settings.
 */
@Dao
interface MassagePreferenceDao {

    // ============================================
    // INSERT OPERATIONS
    // ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: MassagePreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preferences: List<MassagePreferenceEntity>)

    // ============================================
    // UPDATE OPERATIONS
    // ============================================

    @Update
    suspend fun update(preference: MassagePreferenceEntity)

    @Query("""
        UPDATE massage_preferences
        SET customDurationMs = :durationMs, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateZoneDuration(userId: String, zoneId: String, durationMs: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET preferredPressureLevel = :pressureLevel, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateZonePressureLevel(userId: String, zoneId: String, pressureLevel: String, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET preferredRepetitions = :repetitions, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateZoneRepetitions(userId: String, zoneId: String, repetitions: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET pauseBetweenZonesMs = :pauseMs, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updatePauseBetweenZones(userId: String, zoneId: String, pauseMs: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET isFavoriteZone = :isFavorite, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateFavoriteStatus(userId: String, zoneId: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET hapticFeedbackEnabled = :enabled, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateHapticFeedback(userId: String, zoneId: String, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET voiceInstructionsEnabled = :enabled, updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun updateVoiceInstructions(userId: String, zoneId: String, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET lastMassagedAt = :timestamp,
            totalMassageCount = totalMassageCount + 1,
            updatedAt = :updatedAt
        WHERE userId = :userId AND zoneId = :zoneId
    """)
    suspend fun recordZoneMassaged(userId: String, zoneId: String, timestamp: Long = System.currentTimeMillis(), updatedAt: Long = System.currentTimeMillis())

    // ============================================
    // DELETE OPERATIONS
    // ============================================

    @Delete
    suspend fun delete(preference: MassagePreferenceEntity)

    @Query("DELETE FROM massage_preferences WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM massage_preferences WHERE userId = :userId AND zoneId = :zoneId")
    suspend fun deleteByZone(userId: String, zoneId: String)

    // ============================================
    // QUERY OPERATIONS
    // ============================================

    @Query("SELECT * FROM massage_preferences WHERE id = :id")
    suspend fun getById(id: String): MassagePreferenceEntity?

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId AND zoneId = :zoneId")
    suspend fun getByZone(userId: String, zoneId: String): MassagePreferenceEntity?

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId AND zoneId = :zoneId")
    fun getByZoneFlow(userId: String, zoneId: String): Flow<MassagePreferenceEntity?>

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId ORDER BY zoneId")
    fun getAllForUser(userId: String): Flow<List<MassagePreferenceEntity>>

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId AND isFavoriteZone = 1 ORDER BY totalMassageCount DESC")
    fun getFavoriteZones(userId: String): Flow<List<MassagePreferenceEntity>>

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId ORDER BY totalMassageCount DESC")
    fun getMostMassagedZones(userId: String): Flow<List<MassagePreferenceEntity>>

    @Query("SELECT * FROM massage_preferences WHERE userId = :userId ORDER BY lastMassagedAt DESC LIMIT :limit")
    fun getRecentlyMassagedZones(userId: String, limit: Int): Flow<List<MassagePreferenceEntity>>

    // ============================================
    // GLOBAL PREFERENCES
    // ============================================

    @Query("""
        UPDATE massage_preferences
        SET hapticFeedbackEnabled = :enabled, updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateAllHapticFeedback(userId: String, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE massage_preferences
        SET voiceInstructionsEnabled = :enabled, updatedAt = :updatedAt
        WHERE userId = :userId
    """)
    suspend fun updateAllVoiceInstructions(userId: String, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())

    // ============================================
    // ANALYTICS QUERIES
    // ============================================

    @Query("SELECT SUM(totalMassageCount) FROM massage_preferences WHERE userId = :userId")
    suspend fun getTotalMassageCount(userId: String): Int?

    @Query("SELECT zoneId FROM massage_preferences WHERE userId = :userId ORDER BY totalMassageCount DESC LIMIT 1")
    suspend fun getMostMassagedZoneId(userId: String): String?

    @Query("SELECT zoneId FROM massage_preferences WHERE userId = :userId AND lastMassagedAt IS NOT NULL ORDER BY lastMassagedAt ASC LIMIT 1")
    suspend fun getLeastRecentlyMassagedZoneId(userId: String): String?
}
