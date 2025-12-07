package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.MassageProgressDao
import com.ora.wellbeing.data.local.dao.MassageSessionDao
import com.ora.wellbeing.data.local.entities.MassageProgressEntity
import com.ora.wellbeing.data.local.entities.MassageSessionEntity
import com.ora.wellbeing.feature.practice.player.specialized.massage.BodyZone
import com.ora.wellbeing.feature.practice.player.specialized.massage.MassagePlayerState
import com.ora.wellbeing.feature.practice.player.specialized.massage.PressureLevel
import com.ora.wellbeing.feature.practice.player.specialized.massage.ZoneState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing massage session history and progress
 *
 * Provides methods to:
 * - Save and retrieve session history
 * - Save and resume session progress
 * - Get analytics and recommendations
 */
@Singleton
class MassageHistoryRepository @Inject constructor(
    private val massageSessionDao: MassageSessionDao,
    private val massageProgressDao: MassageProgressDao
) {

    // ============================================
    // SESSION HISTORY
    // ============================================

    /**
     * Save a completed session
     */
    suspend fun saveSession(session: MassageSessionEntity) {
        massageSessionDao.insert(session)
    }

    /**
     * Create and save a session from current state
     */
    suspend fun saveSessionFromState(
        userId: String,
        practiceId: String?,
        state: MassagePlayerState,
        isCompleted: Boolean,
        usedCircuitMode: Boolean = false,
        usedVoiceInstructions: Boolean = false,
        rating: Int? = null,
        notes: String? = null
    ): String {
        val sessionId = UUID.randomUUID().toString()
        val completedZones = state.bodyZones.filter { it.state == ZoneState.COMPLETED }

        val session = MassageSessionEntity(
            id = sessionId,
            userId = userId,
            practiceId = practiceId,
            startedAt = state.sessionStartTime,
            completedAt = if (isCompleted) System.currentTimeMillis() else null,
            totalDurationMs = state.sessionDuration,
            zonesCompleted = completedZones.size,
            totalZones = state.bodyZones.size,
            completedZoneIds = Json.encodeToString(completedZones.map { it.id }),
            averagePressureLevel = state.pressureLevel.name,
            rating = rating,
            notes = notes,
            isCompleted = isCompleted,
            usedCircuitMode = usedCircuitMode,
            usedVoiceInstructions = usedVoiceInstructions
        )

        massageSessionDao.insert(session)
        return sessionId
    }

    /**
     * Get all sessions for a user
     */
    fun getAllSessions(userId: String): Flow<List<MassageSessionEntity>> {
        return massageSessionDao.getAllForUser(userId)
    }

    /**
     * Get recent sessions
     */
    fun getRecentSessions(userId: String, limit: Int = 10): Flow<List<MassageSessionEntity>> {
        return massageSessionDao.getRecentForUser(userId, limit)
    }

    /**
     * Get completed sessions
     */
    fun getCompletedSessions(userId: String): Flow<List<MassageSessionEntity>> {
        return massageSessionDao.getCompletedForUser(userId)
    }

    /**
     * Get sessions for a specific practice
     */
    fun getSessionsForPractice(userId: String, practiceId: String): Flow<List<MassageSessionEntity>> {
        return massageSessionDao.getForPractice(userId, practiceId)
    }

    /**
     * Get session by ID
     */
    suspend fun getSessionById(sessionId: String): MassageSessionEntity? {
        return massageSessionDao.getById(sessionId)
    }

    /**
     * Update session rating
     */
    suspend fun updateSessionRating(sessionId: String, rating: Int, notes: String? = null) {
        val session = massageSessionDao.getById(sessionId)
        if (session != null) {
            massageSessionDao.update(session.copy(rating = rating, notes = notes))
        }
    }

    /**
     * Delete session
     */
    suspend fun deleteSession(sessionId: String) {
        massageSessionDao.deleteById(sessionId)
    }

    // ============================================
    // SESSION PROGRESS (RESUME)
    // ============================================

    /**
     * Save progress for later resume
     */
    suspend fun saveProgress(
        userId: String,
        practiceId: String,
        state: MassagePlayerState,
        circuitModeActive: Boolean = false,
        voiceInstructionsActive: Boolean = false
    ): String {
        val progressId = UUID.randomUUID().toString()

        val completedZoneIds = state.bodyZones
            .filter { it.state == ZoneState.COMPLETED }
            .map { it.id }

        val zoneStates = state.bodyZones.associate { it.id to it.state.name }

        val progress = MassageProgressEntity(
            id = progressId,
            userId = userId,
            practiceId = practiceId,
            currentZoneIndex = state.currentZoneIndex,
            zoneTimeRemainingMs = state.zoneTimer,
            zoneRepetitionsRemaining = state.zoneRepetitions,
            completedZoneIds = Json.encodeToString(completedZoneIds),
            zoneStates = Json.encodeToString(zoneStates),
            currentPressureLevel = state.pressureLevel.name,
            mediaPositionMs = state.playerState.currentPosition,
            showBodyMap = state.showBodyMap,
            circuitModeActive = circuitModeActive,
            voiceInstructionsActive = voiceInstructionsActive,
            sessionDurationMs = state.sessionDuration,
            sessionStartedAt = state.sessionStartTime,
            pausedAt = System.currentTimeMillis()
        )

        // Delete any existing progress for this practice
        massageProgressDao.deleteForPractice(userId, practiceId)
        massageProgressDao.insert(progress)

        return progressId
    }

    /**
     * Get saved progress for a practice
     */
    suspend fun getProgress(userId: String, practiceId: String): MassageProgressEntity? {
        return massageProgressDao.getLatestForPractice(userId, practiceId)
    }

    /**
     * Get saved progress as Flow
     */
    fun getProgressFlow(userId: String, practiceId: String): Flow<MassageProgressEntity?> {
        return massageProgressDao.getLatestForPracticeFlow(userId, practiceId)
    }

    /**
     * Check if there's saved progress
     */
    suspend fun hasProgress(userId: String, practiceId: String): Boolean {
        return massageProgressDao.hasProgressForPractice(userId, practiceId)
    }

    /**
     * Get all saved progress for a user
     */
    fun getAllProgress(userId: String): Flow<List<MassageProgressEntity>> {
        return massageProgressDao.getAllForUser(userId)
    }

    /**
     * Delete progress (after resuming or completing)
     */
    suspend fun deleteProgress(userId: String, practiceId: String) {
        massageProgressDao.deleteForPractice(userId, practiceId)
    }

    /**
     * Delete progress by ID
     */
    suspend fun deleteProgressById(progressId: String) {
        massageProgressDao.deleteById(progressId)
    }

    /**
     * Clean up old progress (older than 7 days)
     */
    suspend fun cleanupOldProgress(daysOld: Int = 7) {
        val threshold = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        massageProgressDao.deleteProgressOlderThan(threshold)
    }

    /**
     * Restore state from progress
     */
    fun restoreStateFromProgress(
        progress: MassageProgressEntity,
        defaultZones: List<BodyZone>
    ): Pair<List<BodyZone>, MassagePlayerState> {
        // Parse saved zone states
        val savedStates: Map<String, String> = try {
            Json.decodeFromString(progress.zoneStates)
        } catch (_: Exception) {
            emptyMap()
        }

        // Restore zone states
        val restoredZones = defaultZones.map { zone ->
            val stateStr = savedStates[zone.id]
            val state = if (stateStr != null) {
                try {
                    ZoneState.valueOf(stateStr)
                } catch (_: Exception) {
                    ZoneState.PENDING
                }
            } else {
                ZoneState.PENDING
            }
            zone.copy(state = state)
        }

        // Create restored state
        val restoredState = MassagePlayerState(
            bodyZones = restoredZones,
            currentZoneIndex = progress.currentZoneIndex,
            zoneTimer = progress.zoneTimeRemainingMs,
            zoneRepetitions = progress.zoneRepetitionsRemaining,
            pressureLevel = try {
                PressureLevel.valueOf(progress.currentPressureLevel)
            } catch (_: Exception) {
                PressureLevel.MEDIUM
            },
            showBodyMap = progress.showBodyMap,
            sessionStartTime = progress.sessionStartedAt,
            sessionDuration = progress.sessionDurationMs
        )

        return Pair(restoredZones, restoredState)
    }

    // ============================================
    // ANALYTICS
    // ============================================

    /**
     * Get total session count
     */
    suspend fun getTotalSessionCount(userId: String): Int {
        return massageSessionDao.getTotalSessionCount(userId)
    }

    /**
     * Get completed session count
     */
    suspend fun getCompletedSessionCount(userId: String): Int {
        return massageSessionDao.getCompletedSessionCount(userId)
    }

    /**
     * Get total massage time in milliseconds
     */
    suspend fun getTotalMassageTime(userId: String): Long {
        return massageSessionDao.getTotalMassageTime(userId) ?: 0L
    }

    /**
     * Get average session duration
     */
    suspend fun getAverageSessionDuration(userId: String): Long {
        return massageSessionDao.getAverageSessionDuration(userId)?.toLong() ?: 0L
    }

    /**
     * Get average rating
     */
    suspend fun getAverageRating(userId: String): Double {
        return massageSessionDao.getAverageRating(userId) ?: 0.0
    }

    /**
     * Get most used pressure level
     */
    suspend fun getMostUsedPressureLevel(userId: String): PressureLevel? {
        val levelStr = massageSessionDao.getMostUsedPressureLevel(userId)
        return if (levelStr != null) {
            try {
                PressureLevel.valueOf(levelStr)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Get session count today
     */
    suspend fun getSessionCountToday(userId: String): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return massageSessionDao.getSessionCountToday(userId, calendar.timeInMillis)
    }

    /**
     * Get session count this week
     */
    suspend fun getSessionCountThisWeek(userId: String): Int {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return massageSessionDao.getSessionCountThisWeek(userId, calendar.timeInMillis)
    }

    /**
     * Get sessions in date range
     */
    fun getSessionsInDateRange(
        userId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<MassageSessionEntity>> {
        return massageSessionDao.getSessionsInDateRange(userId, startTime, endTime)
    }

    /**
     * Get zone usage statistics from recent sessions
     */
    suspend fun getZoneUsageStats(userId: String): Map<String, Int> {
        val recentZoneHistories = massageSessionDao.getRecentZoneHistory(userId)
        val zoneCounts = mutableMapOf<String, Int>()

        recentZoneHistories.forEach { zoneIdsJson ->
            try {
                val zoneIds: List<String> = Json.decodeFromString(zoneIdsJson)
                zoneIds.forEach { zoneId ->
                    zoneCounts[zoneId] = (zoneCounts[zoneId] ?: 0) + 1
                }
            } catch (_: Exception) {
                // Ignore parsing errors
            }
        }

        return zoneCounts
    }
}
