package com.ora.wellbeing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ora.wellbeing.data.local.entities.MassageProgressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for massage session progress
 *
 * Enables pause/resume functionality for massage sessions,
 * allowing users to continue where they left off.
 */
@Dao
interface MassageProgressDao {

    // ============================================
    // INSERT OPERATIONS
    // ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: MassageProgressEntity)

    // ============================================
    // UPDATE OPERATIONS
    // ============================================

    @Update
    suspend fun update(progress: MassageProgressEntity)

    @Query("""
        UPDATE massage_progress
        SET currentZoneIndex = :zoneIndex,
            zoneTimeRemainingMs = :timeRemainingMs,
            zoneRepetitionsRemaining = :repetitionsRemaining,
            completedZoneIds = :completedZoneIds,
            zoneStates = :zoneStates,
            mediaPositionMs = :mediaPositionMs,
            sessionDurationMs = :sessionDurationMs,
            pausedAt = :pausedAt,
            updatedAt = :updatedAt
        WHERE id = :progressId
    """)
    suspend fun updateProgress(
        progressId: String,
        zoneIndex: Int,
        timeRemainingMs: Long,
        repetitionsRemaining: Int,
        completedZoneIds: String,
        zoneStates: String,
        mediaPositionMs: Long,
        sessionDurationMs: Long,
        pausedAt: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE massage_progress
        SET currentPressureLevel = :pressureLevel,
            updatedAt = :updatedAt
        WHERE id = :progressId
    """)
    suspend fun updatePressureLevel(
        progressId: String,
        pressureLevel: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE massage_progress
        SET circuitModeActive = :active,
            updatedAt = :updatedAt
        WHERE id = :progressId
    """)
    suspend fun updateCircuitMode(
        progressId: String,
        active: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE massage_progress
        SET voiceInstructionsActive = :active,
            updatedAt = :updatedAt
        WHERE id = :progressId
    """)
    suspend fun updateVoiceInstructions(
        progressId: String,
        active: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE massage_progress
        SET showBodyMap = :show,
            updatedAt = :updatedAt
        WHERE id = :progressId
    """)
    suspend fun updateBodyMapVisibility(
        progressId: String,
        show: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    // ============================================
    // DELETE OPERATIONS
    // ============================================

    @Delete
    suspend fun delete(progress: MassageProgressEntity)

    @Query("DELETE FROM massage_progress WHERE id = :progressId")
    suspend fun deleteById(progressId: String)

    @Query("DELETE FROM massage_progress WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM massage_progress WHERE userId = :userId AND practiceId = :practiceId")
    suspend fun deleteForPractice(userId: String, practiceId: String)

    @Query("DELETE FROM massage_progress WHERE pausedAt < :threshold")
    suspend fun deleteOldProgress(threshold: Long)

    // ============================================
    // QUERY OPERATIONS
    // ============================================

    @Query("SELECT * FROM massage_progress WHERE id = :progressId")
    suspend fun getById(progressId: String): MassageProgressEntity?

    @Query("SELECT * FROM massage_progress WHERE userId = :userId AND practiceId = :practiceId ORDER BY pausedAt DESC LIMIT 1")
    suspend fun getLatestForPractice(userId: String, practiceId: String): MassageProgressEntity?

    @Query("SELECT * FROM massage_progress WHERE userId = :userId AND practiceId = :practiceId ORDER BY pausedAt DESC LIMIT 1")
    fun getLatestForPracticeFlow(userId: String, practiceId: String): Flow<MassageProgressEntity?>

    @Query("SELECT * FROM massage_progress WHERE userId = :userId ORDER BY pausedAt DESC")
    fun getAllForUser(userId: String): Flow<List<MassageProgressEntity>>

    @Query("SELECT * FROM massage_progress WHERE userId = :userId ORDER BY pausedAt DESC LIMIT 1")
    suspend fun getLatestForUser(userId: String): MassageProgressEntity?

    @Query("SELECT * FROM massage_progress WHERE userId = :userId ORDER BY pausedAt DESC LIMIT :limit")
    fun getRecentForUser(userId: String, limit: Int): Flow<List<MassageProgressEntity>>

    // ============================================
    // EXISTS QUERIES
    // ============================================

    @Query("SELECT EXISTS(SELECT 1 FROM massage_progress WHERE userId = :userId AND practiceId = :practiceId)")
    suspend fun hasProgressForPractice(userId: String, practiceId: String): Boolean

    @Query("SELECT COUNT(*) FROM massage_progress WHERE userId = :userId")
    suspend fun getProgressCount(userId: String): Int

    // ============================================
    // CLEANUP OPERATIONS
    // ============================================

    /**
     * Delete progress entries older than a specified number of days
     * @param daysOld Number of days after which to delete progress
     */
    @Query("""
        DELETE FROM massage_progress
        WHERE pausedAt < :threshold
    """)
    suspend fun deleteProgressOlderThan(threshold: Long)
}
