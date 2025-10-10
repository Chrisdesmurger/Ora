package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.UserProgram
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user program enrollments and progress tracking
 * Collection: user_programs/{uid}/enrolled/{programId}
 *
 * Privacy by design: All operations verify that request.auth.uid == uid
 * Offline-first: Flow returns cache if network error
 */
interface UserProgramRepository {

    /**
     * Observes all enrolled programs for a user in real-time
     * Returns programs ordered by startedAt DESC (most recent first)
     *
     * @param uid Firebase Auth UID
     * @return Reactive Flow of enrolled programs
     */
    fun getEnrolledPrograms(uid: String): Flow<List<UserProgram>>

    /**
     * Observes active (in-progress) programs for a user in real-time
     * Active = not completed
     * Returns programs ordered by lastSessionAt DESC (most recently active first)
     *
     * @param uid Firebase Auth UID
     * @return Reactive Flow of active programs
     */
    fun getActivePrograms(uid: String): Flow<List<UserProgram>>

    /**
     * Observes completed programs for a user in real-time
     * Returns programs ordered by completedAt DESC (most recently completed first)
     *
     * @param uid Firebase Auth UID
     * @return Reactive Flow of completed programs
     */
    fun getCompletedPrograms(uid: String): Flow<List<UserProgram>>

    /**
     * Observes a specific user program enrollment in real-time
     *
     * @param uid Firebase Auth UID
     * @param programId Program ID
     * @return Reactive Flow of the user program (null if not enrolled)
     */
    fun getUserProgram(uid: String, programId: String): Flow<UserProgram?>

    /**
     * Enrolls a user in a program
     * Creates initial UserProgram with currentDay=1, progressPercentage=0
     *
     * @param uid Firebase Auth UID
     * @param programId Program ID
     * @param totalDays Total days in the program
     * @return Result.success if enrolled, Result.failure if error or already enrolled
     */
    suspend fun enrollInProgram(uid: String, programId: String, totalDays: Int): Result<Unit>

    /**
     * Updates progress after completing a session
     * Increments currentDay, updates lastSessionAt, adds to completedSessions
     * Recalculates progressPercentage
     * Marks as completed if all sessions done
     *
     * @param uid Firebase Auth UID
     * @param programId Program ID
     * @param currentDay New current day
     * @param completedSessionId Content ID that was completed
     * @return Result.success if updated, Result.failure if error
     */
    suspend fun updateProgress(uid: String, programId: String, currentDay: Int, completedSessionId: String): Result<Unit>

    /**
     * Marks a program as completed
     * Sets isCompleted=true, completedAt=now, progressPercentage=100
     *
     * @param uid Firebase Auth UID
     * @param programId Program ID
     * @return Result.success if completed, Result.failure if error
     */
    suspend fun completeProgram(uid: String, programId: String): Result<Unit>

    /**
     * Unenrolls a user from a program (deletes the enrollment)
     *
     * @param uid Firebase Auth UID
     * @param programId Program ID
     * @return Result.success if unenrolled, Result.failure if error
     */
    suspend fun unenrollFromProgram(uid: String, programId: String): Result<Unit>

    /**
     * Gets total count of enrolled programs for a user
     *
     * @param uid Firebase Auth UID
     * @return Total number of enrolled programs
     */
    suspend fun getEnrolledProgramCount(uid: String): Int

    /**
     * Gets total count of completed programs for a user
     *
     * @param uid Firebase Auth UID
     * @return Total number of completed programs
     */
    suspend fun getCompletedProgramCount(uid: String): Int
}
