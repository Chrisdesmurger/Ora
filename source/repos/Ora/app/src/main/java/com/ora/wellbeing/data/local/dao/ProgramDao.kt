package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.ProgramEntity
import kotlinx.coroutines.flow.Flow

/**
 * ProgramDao - Room DAO for programs
 *
 * Provides database access methods for ProgramEntity with reactive Flow support.
 * All queries return Flow for automatic UI updates when data changes.
 */
@Dao
interface ProgramDao {

    /**
     * Get all active programs
     * @return Flow of all programs where isActive = true, sorted by title
     */
    @Query("SELECT * FROM programs WHERE isActive = 1 ORDER BY title ASC")
    fun getAllProgramsFlow(): Flow<List<ProgramEntity>>

    /**
     * Get all programs (including inactive)
     * @return Flow of all programs
     */
    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    fun getAllProgramsIncludingInactiveFlow(): Flow<List<ProgramEntity>>

    /**
     * Get program by ID
     * @param programId Program ID
     * @return Flow of single program or null if not found
     */
    @Query("SELECT * FROM programs WHERE id = :programId")
    fun getProgramByIdFlow(programId: String): Flow<ProgramEntity?>

    /**
     * Get programs by category
     * @param category Category in French (e.g., "Méditation", "Yoga")
     * @return Flow of programs in the specified category
     */
    @Query("SELECT * FROM programs WHERE category = :category AND isActive = 1 ORDER BY title ASC")
    fun getProgramsByCategoryFlow(category: String): Flow<List<ProgramEntity>>

    /**
     * Get programs by level
     * @param level Level in French (e.g., "Débutant", "Intermédiaire")
     * @return Flow of programs at the specified level
     */
    @Query("SELECT * FROM programs WHERE level = :level AND isActive = 1 ORDER BY title ASC")
    fun getProgramsByLevelFlow(level: String): Flow<List<ProgramEntity>>

    /**
     * Get popular programs (participant count >= 100)
     * @return Flow of popular programs
     */
    @Query("SELECT * FROM programs WHERE isActive = 1 AND participantCount >= 100 ORDER BY participantCount DESC")
    fun getPopularProgramsFlow(): Flow<List<ProgramEntity>>

    /**
     * Get programs by duration range
     * @param minDays Minimum duration in days
     * @param maxDays Maximum duration in days
     * @return Flow of programs within duration range
     */
    @Query("SELECT * FROM programs WHERE isActive = 1 AND durationDays BETWEEN :minDays AND :maxDays ORDER BY durationDays ASC")
    fun getProgramsByDurationRangeFlow(minDays: Int, maxDays: Int): Flow<List<ProgramEntity>>

    /**
     * Search programs by title or description
     * @param query Search query
     * @return Flow of matching programs
     */
    @Query("SELECT * FROM programs WHERE isActive = 1 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY title ASC")
    fun searchProgramsFlow(query: String): Flow<List<ProgramEntity>>

    /**
     * Insert a single program
     * @param program Program to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity)

    /**
     * Insert multiple programs
     * @param programs List of programs to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrograms(programs: List<ProgramEntity>)

    /**
     * Update a program
     * @param program Program to update
     */
    @Update
    suspend fun updateProgram(program: ProgramEntity)

    /**
     * Delete a program
     * @param program Program to delete
     */
    @Delete
    suspend fun deleteProgram(program: ProgramEntity)

    /**
     * Delete all programs
     * Used for cache clearing
     */
    @Query("DELETE FROM programs")
    suspend fun deleteAllPrograms()

    /**
     * Get count of programs
     * @return Total number of programs
     */
    @Query("SELECT COUNT(*) FROM programs")
    suspend fun getProgramCount(): Int

    /**
     * Get programs that need sync (not synced in last 24 hours)
     * @param timestamp Timestamp to compare against (current time - 24 hours)
     * @return List of programs needing sync
     */
    @Query("SELECT * FROM programs WHERE lastSyncedAt < :timestamp")
    suspend fun getProgramsNeedingSync(timestamp: Long): List<ProgramEntity>
}
