package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.Program
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for program catalog (read-only)
 * Collection: programs/{programId}
 *
 * Offline-first: Flow returns cache if network error
 */
interface ProgramRepository {

    /**
     * Observes all active programs in real-time
     * Returns programs ordered by rating DESC
     *
     * @return Reactive Flow of all active programs
     */
    fun getAllPrograms(): Flow<List<Program>>

    /**
     * Observes programs filtered by category in real-time
     * Returns programs ordered by rating DESC
     *
     * @param category Program category (Méditation, Yoga, Bien-être, Défis, Sommeil)
     * @return Reactive Flow of programs in the specified category
     */
    fun getProgramsByCategory(category: String): Flow<List<Program>>

    /**
     * Observes a single program in real-time
     *
     * @param programId Program ID
     * @return Reactive Flow of the program (null if not found)
     */
    fun getProgram(programId: String): Flow<Program?>

    /**
     * Observes popular programs in real-time
     * Returns programs ordered by participant count DESC
     *
     * @param limit Maximum number of programs to return
     * @return Reactive Flow of popular programs
     */
    fun getPopularPrograms(limit: Int = 10): Flow<List<Program>>

    /**
     * Observes programs filtered by level in real-time
     *
     * @param level Program level (Débutant, Intermédiaire, Avancé, Tous niveaux)
     * @return Reactive Flow of programs with the specified level
     */
    fun getProgramsByLevel(level: String): Flow<List<Program>>

    /**
     * Gets total count of active programs
     *
     * @return Total number of active programs
     */
    suspend fun getTotalProgramCount(): Int
}
