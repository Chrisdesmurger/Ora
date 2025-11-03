package com.ora.wellbeing.data.repository.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ora.wellbeing.data.local.dao.ProgramDao
import com.ora.wellbeing.data.local.entities.ProgramEntity
import com.ora.wellbeing.data.mapper.ProgramMapper
import com.ora.wellbeing.data.model.Program
import com.ora.wellbeing.data.model.firestore.ProgramDocument
import com.ora.wellbeing.domain.repository.ProgramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of ProgramRepository
 *
 * Architecture:
 * - Room database (ProgramEntity) as single source of truth
 * - Firestore "programs" collection (ProgramDocument with snake_case) for cloud sync
 * - ProgramMapper converts Firestore ↔ Android (snake_case ↔ camelCase)
 * - Smart caching with 1-hour sync interval
 * - Immediate offline access with background sync
 *
 * Sync Strategy:
 * 1. Return Room Flow (always emits cached data immediately)
 * 2. On first collect, trigger sync if cache is stale
 * 3. Firestore updates are written to Room
 * 4. Room Flow automatically emits updated data
 */
@Singleton
class ProgramRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val programDao: ProgramDao
) : ProgramRepository {

    companion object {
        private const val COLLECTION_PROGRAMS = "programs"
        private const val SYNC_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
    }

    /**
     * Get all published programs with offline-first approach
     *
     * Returns Room Flow that emits cached data immediately.
     * Triggers background sync if cache is stale.
     */
    override fun getAllPrograms(): Flow<List<Program>> {
        Timber.d("getAllPrograms: Returning offline-first Flow")

        return programDao.getAllProgramsFlow()
            .map { entities -> entities.map { it.toDomainModel() } }
            .onStart {
                // Trigger sync in background if needed
                val entities = programDao.getProgramCount()
                if (entities == 0 || shouldSyncByTimestamp()) {
                    Timber.d("getAllPrograms: Triggering background sync")
                    syncAllProgramsFromFirestore()
                }
            }
    }

    /**
     * Get programs by category with offline-first approach
     */
    override fun getProgramsByCategory(category: String): Flow<List<Program>> {
        require(category.isNotBlank()) { "Category cannot be blank" }
        Timber.d("getProgramsByCategory: Returning offline-first Flow for category=$category")

        return programDao.getProgramsByCategoryFlow(category)
            .map { entities -> entities.map { it.toDomainModel() } }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSyncByTimestamp()) {
                    Timber.d("getProgramsByCategory: Triggering background sync for category=$category")
                    syncProgramsByCategoryFromFirestore(category)
                }
            }
    }

    /**
     * Get single program by ID with offline-first approach
     */
    override fun getProgram(programId: String): Flow<Program?> {
        require(programId.isNotBlank()) { "Program ID cannot be blank" }
        Timber.d("getProgram: Returning offline-first Flow for id=$programId")

        return programDao.getProgramByIdFlow(programId)
            .map { entity -> entity?.toDomainModel() }
            .onStart {
                // Trigger sync in background if needed
                Timber.d("getProgram: Triggering background sync for id=$programId")
                syncProgramFromFirestore(programId)
            }
    }

    /**
     * Get popular programs with offline-first approach
     */
    override fun getPopularPrograms(limit: Int): Flow<List<Program>> {
        require(limit > 0) { "Limit must be > 0" }
        Timber.d("getPopularPrograms: Returning offline-first Flow (limit=$limit)")

        return programDao.getPopularProgramsFlow()
            .map { entities -> entities.take(limit).map { it.toDomainModel() } }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSyncByTimestamp()) {
                    Timber.d("getPopularPrograms: Triggering background sync")
                    syncPopularProgramsFromFirestore(limit)
                }
            }
    }

    /**
     * Get programs by level with offline-first approach
     */
    override fun getProgramsByLevel(level: String): Flow<List<Program>> {
        require(level.isNotBlank()) { "Level cannot be blank" }
        Timber.d("getProgramsByLevel: Returning offline-first Flow for level=$level")

        return programDao.getProgramsByLevelFlow(level)
            .map { entities -> entities.map { it.toDomainModel() } }
            .onStart {
                // Trigger sync in background if needed
                if (shouldSyncByTimestamp()) {
                    Timber.d("getProgramsByLevel: Triggering background sync for level=$level")
                    syncProgramsByLevelFromFirestore(level)
                }
            }
    }

    /**
     * Get total program count (cached value)
     */
    override suspend fun getTotalProgramCount(): Int = try {
        val count = programDao.getProgramCount()
        Timber.d("getTotalProgramCount: Count = $count")
        count
    } catch (e: Exception) {
        Timber.e(e, "getTotalProgramCount: Error getting count")
        0
    }

    // ============================================================================
    // Private Helper Methods - Firestore Sync
    // ============================================================================

    /**
     * Sync all published programs from Firestore
     */
    private suspend fun syncAllProgramsFromFirestore() {
        try {
            Timber.d("syncAllPrograms: Fetching from Firestore")

            val snapshot = firestore
                .collection(COLLECTION_PROGRAMS)
                .whereEqualTo("status", "published")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()

            val programs = snapshot.documents.mapNotNull { doc ->
                try {
                    val programDoc = doc.toObject(ProgramDocument::class.java)
                    if (programDoc != null) {
                        ProgramMapper.fromFirestore(doc.id, programDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "syncAllPrograms: Error mapping document ${doc.id}")
                    null
                }
            }

            // Convert to entities and cache
            val entities = programs.map { it.toEntity() }
            programDao.insertAllPrograms(entities)

            Timber.d("syncAllPrograms: Synced ${entities.size} programs to Room")
        } catch (e: Exception) {
            Timber.e(e, "syncAllPrograms: Error syncing from Firestore")
        }
    }

    /**
     * Sync programs by category from Firestore
     */
    private suspend fun syncProgramsByCategoryFromFirestore(category: String) {
        try {
            // Map French category back to backend category
            val backendCategory = mapFrenchToBackendCategory(category)

            Timber.d("syncProgramsByCategory: Fetching category=$backendCategory from Firestore")

            val snapshot = firestore
                .collection(COLLECTION_PROGRAMS)
                .whereEqualTo("status", "published")
                .whereEqualTo("category", backendCategory)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()

            val programs = snapshot.documents.mapNotNull { doc ->
                try {
                    val programDoc = doc.toObject(ProgramDocument::class.java)
                    if (programDoc != null) {
                        ProgramMapper.fromFirestore(doc.id, programDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "syncProgramsByCategory: Error mapping document ${doc.id}")
                    null
                }
            }

            val entities = programs.map { it.toEntity() }
            programDao.insertAllPrograms(entities)

            Timber.d("syncProgramsByCategory: Synced ${entities.size} programs for category=$category")
        } catch (e: Exception) {
            Timber.e(e, "syncProgramsByCategory: Error syncing category=$category")
        }
    }

    /**
     * Sync single program from Firestore
     */
    private suspend fun syncProgramFromFirestore(programId: String) {
        try {
            Timber.d("syncProgram: Fetching program id=$programId from Firestore")

            val doc = firestore
                .collection(COLLECTION_PROGRAMS)
                .document(programId)
                .get()
                .await()

            if (doc.exists()) {
                val programDoc = doc.toObject(ProgramDocument::class.java)
                if (programDoc != null && programDoc.status == "published") {
                    val program = ProgramMapper.fromFirestore(doc.id, programDoc)
                    val entity = program.toEntity()
                    programDao.insertProgram(entity)

                    Timber.d("syncProgram: Synced program id=$programId")
                } else {
                    Timber.w("syncProgram: Program $programId not published or invalid")
                }
            } else {
                Timber.w("syncProgram: Program $programId not found in Firestore")
            }
        } catch (e: Exception) {
            Timber.e(e, "syncProgram: Error syncing program id=$programId")
        }
    }

    /**
     * Sync popular programs from Firestore
     */
    private suspend fun syncPopularProgramsFromFirestore(limit: Int) {
        try {
            Timber.d("syncPopularPrograms: Fetching top $limit programs from Firestore")

            val snapshot = firestore
                .collection(COLLECTION_PROGRAMS)
                .whereEqualTo("status", "published")
                .orderBy("participant_count", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val programs = snapshot.documents.mapNotNull { doc ->
                try {
                    val programDoc = doc.toObject(ProgramDocument::class.java)
                    if (programDoc != null) {
                        ProgramMapper.fromFirestore(doc.id, programDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "syncPopularPrograms: Error mapping document ${doc.id}")
                    null
                }
            }

            val entities = programs.map { it.toEntity() }
            programDao.insertAllPrograms(entities)

            Timber.d("syncPopularPrograms: Synced ${entities.size} popular programs")
        } catch (e: Exception) {
            Timber.e(e, "syncPopularPrograms: Error syncing popular programs")
        }
    }

    /**
     * Sync programs by level from Firestore
     */
    private suspend fun syncProgramsByLevelFromFirestore(level: String) {
        try {
            // Map French level back to backend difficulty
            val backendLevel = mapFrenchToBackendLevel(level)

            Timber.d("syncProgramsByLevel: Fetching difficulty=$backendLevel from Firestore")

            val snapshot = firestore
                .collection(COLLECTION_PROGRAMS)
                .whereEqualTo("status", "published")
                .whereEqualTo("difficulty", backendLevel)
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()

            val programs = snapshot.documents.mapNotNull { doc ->
                try {
                    val programDoc = doc.toObject(ProgramDocument::class.java)
                    if (programDoc != null) {
                        ProgramMapper.fromFirestore(doc.id, programDoc)
                    } else null
                } catch (e: Exception) {
                    Timber.e(e, "syncProgramsByLevel: Error mapping document ${doc.id}")
                    null
                }
            }

            val entities = programs.map { it.toEntity() }
            programDao.insertAllPrograms(entities)

            Timber.d("syncProgramsByLevel: Synced ${entities.size} programs for level=$level")
        } catch (e: Exception) {
            Timber.e(e, "syncProgramsByLevel: Error syncing level=$level")
        }
    }

    // ============================================================================
    // Private Helper Methods - Cache Management
    // ============================================================================

    /**
     * Check if sync is needed based on timestamp
     * This is a simple time-based check without querying specific entities
     */
    private suspend fun shouldSyncByTimestamp(): Boolean {
        return try {
            val entities = programDao.getProgramsNeedingSync(
                System.currentTimeMillis() - SYNC_INTERVAL_MS
            )
            val needsSync = entities.isNotEmpty()
            Timber.d("shouldSyncByTimestamp: needsSync=$needsSync (${entities.size} stale programs)")
            needsSync
        } catch (e: Exception) {
            Timber.e(e, "shouldSyncByTimestamp: Error checking sync status")
            true // Default to syncing on error
        }
    }

    // ============================================================================
    // Private Helper Methods - Mapping
    // ============================================================================

    /**
     * Map French category to backend category
     */
    private fun mapFrenchToBackendCategory(category: String): String {
        return when (category) {
            "Méditation" -> "meditation"
            "Yoga" -> "yoga"
            "Pleine Conscience" -> "mindfulness"
            "Bien-être" -> "wellness"
            else -> {
                Timber.w("mapFrenchToBackendCategory: Unknown category $category, defaulting to wellness")
                "wellness"
            }
        }
    }

    /**
     * Map French level to backend difficulty
     */
    private fun mapFrenchToBackendLevel(level: String): String {
        return when (level) {
            "Débutant" -> "beginner"
            "Intermédiaire" -> "intermediate"
            "Avancé" -> "advanced"
            "Tous niveaux" -> "beginner" // Default to beginner for "all levels"
            else -> {
                Timber.w("mapFrenchToBackendLevel: Unknown level $level, defaulting to beginner")
                "beginner"
            }
        }
    }

    // ============================================================================
    // Extension Functions - Domain ↔ Entity Conversion
    // ============================================================================

    /**
     * Convert Program (domain model) to ProgramEntity (Room entity)
     */
    private fun Program.toEntity(): ProgramEntity {
        val now = System.currentTimeMillis()
        return ProgramEntity(
            id = id,
            title = title,
            description = description,
            category = category,
            level = level,
            durationDays = duration,
            thumbnailUrl = thumbnailUrl,
            instructor = instructor,
            isActive = isActive,
            isPremiumOnly = isPremiumOnly,
            participantCount = participantCount,
            rating = rating,
            lessonIds = sessions.mapNotNull { it["id"] as? String },
            tags = emptyList(), // Tags not currently in Program model
            createdAt = createdAt?.toDate()?.time ?: now,
            updatedAt = updatedAt?.toDate()?.time ?: now,
            lastSyncedAt = now
        )
    }

    /**
     * Convert ProgramEntity (Room entity) to Program (domain model)
     */
    private fun ProgramEntity.toDomainModel(): Program {
        return Program().apply {
            id = this@toDomainModel.id
            title = this@toDomainModel.title
            description = this@toDomainModel.description
            category = this@toDomainModel.category
            duration = this@toDomainModel.durationDays
            level = this@toDomainModel.level
            participantCount = this@toDomainModel.participantCount
            rating = this@toDomainModel.rating
            thumbnailUrl = this@toDomainModel.thumbnailUrl
            instructor = this@toDomainModel.instructor
            isPremiumOnly = this@toDomainModel.isPremiumOnly
            isActive = this@toDomainModel.isActive

            // Convert lessonIds to sessions format
            sessions = this@toDomainModel.lessonIds.mapIndexed { index, lessonId ->
                mapOf(
                    "id" to lessonId,
                    "day" to (index + 1),
                    "title" to "", // Will be populated when lessons are fetched
                    "duration" to "",
                    "isCompleted" to false
                )
            }

            // Convert timestamps
            createdAt = com.google.firebase.Timestamp(
                this@toDomainModel.createdAt / 1000,
                ((this@toDomainModel.createdAt % 1000) * 1_000_000).toInt()
            )
            updatedAt = com.google.firebase.Timestamp(
                this@toDomainModel.updatedAt / 1000,
                ((this@toDomainModel.updatedAt % 1000) * 1_000_000).toInt()
            )
        }
    }
}
