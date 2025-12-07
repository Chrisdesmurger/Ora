package com.ora.wellbeing.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ora.wellbeing.data.local.entities.MassageSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for massage session history
 *
 * Provides methods to track and analyze massage sessions
 * for recommendations and progress tracking.
 */
@Dao
interface MassageSessionDao {

    // ============================================
    // INSERT OPERATIONS
    // ============================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: MassageSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<MassageSessionEntity>)

    // ============================================
    // UPDATE OPERATIONS
    // ============================================

    @Update
    suspend fun update(session: MassageSessionEntity)

    // ============================================
    // DELETE OPERATIONS
    // ============================================

    @Delete
    suspend fun delete(session: MassageSessionEntity)

    @Query("DELETE FROM massage_sessions WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM massage_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)

    // ============================================
    // QUERY OPERATIONS
    // ============================================

    @Query("SELECT * FROM massage_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: String): MassageSessionEntity?

    @Query("SELECT * FROM massage_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getAllForUser(userId: String): Flow<List<MassageSessionEntity>>

    @Query("SELECT * FROM massage_sessions WHERE userId = :userId ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentForUser(userId: String, limit: Int): Flow<List<MassageSessionEntity>>

    @Query("SELECT * FROM massage_sessions WHERE userId = :userId AND isCompleted = 1 ORDER BY startedAt DESC")
    fun getCompletedForUser(userId: String): Flow<List<MassageSessionEntity>>

    @Query("SELECT * FROM massage_sessions WHERE userId = :userId AND practiceId = :practiceId ORDER BY startedAt DESC")
    fun getForPractice(userId: String, practiceId: String): Flow<List<MassageSessionEntity>>

    // ============================================
    // ANALYTICS QUERIES
    // ============================================

    @Query("SELECT COUNT(*) FROM massage_sessions WHERE userId = :userId")
    suspend fun getTotalSessionCount(userId: String): Int

    @Query("SELECT COUNT(*) FROM massage_sessions WHERE userId = :userId AND isCompleted = 1")
    suspend fun getCompletedSessionCount(userId: String): Int

    @Query("SELECT SUM(totalDurationMs) FROM massage_sessions WHERE userId = :userId")
    suspend fun getTotalMassageTime(userId: String): Long?

    @Query("SELECT AVG(totalDurationMs) FROM massage_sessions WHERE userId = :userId AND isCompleted = 1")
    suspend fun getAverageSessionDuration(userId: String): Double?

    @Query("SELECT AVG(rating) FROM massage_sessions WHERE userId = :userId AND rating IS NOT NULL")
    suspend fun getAverageRating(userId: String): Double?

    @Query("""
        SELECT completedZoneIds FROM massage_sessions
        WHERE userId = :userId
        ORDER BY startedAt DESC
        LIMIT 10
    """)
    suspend fun getRecentZoneHistory(userId: String): List<String>

    @Query("""
        SELECT * FROM massage_sessions
        WHERE userId = :userId
        AND startedAt >= :startTime
        AND startedAt <= :endTime
        ORDER BY startedAt DESC
    """)
    fun getSessionsInDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<MassageSessionEntity>>

    @Query("""
        SELECT COUNT(*) FROM massage_sessions
        WHERE userId = :userId
        AND startedAt >= :startOfDay
    """)
    suspend fun getSessionCountToday(userId: String, startOfDay: Long): Int

    @Query("""
        SELECT COUNT(*) FROM massage_sessions
        WHERE userId = :userId
        AND startedAt >= :startOfWeek
    """)
    suspend fun getSessionCountThisWeek(userId: String, startOfWeek: Long): Int

    // ============================================
    // PRESSURE LEVEL ANALYSIS
    // ============================================

    /**
     * Get the most used pressure level by counting occurrences
     * Uses a subquery to find the pressure level with the highest count
     */
    @Query("""
        SELECT averagePressureLevel FROM massage_sessions
        WHERE userId = :userId
        GROUP BY averagePressureLevel
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getMostUsedPressureLevel(userId: String): String?
}
