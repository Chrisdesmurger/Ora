package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {

    @Query("SELECT * FROM content ORDER BY title ASC")
    fun getAllContentFlow(): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE type = :type ORDER BY title ASC")
    fun getContentByTypeFlow(type: ContentType): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE category = :category ORDER BY title ASC")
    fun getContentByCategoryFlow(category: Category): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE level = :level ORDER BY title ASC")
    fun getContentByLevelFlow(level: ExperienceLevel): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE isFlashSession = 1 ORDER BY title ASC")
    fun getFlashSessionsFlow(): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE durationMinutes <= :maxDuration ORDER BY durationMinutes ASC")
    fun getContentByMaxDurationFlow(maxDuration: Int): Flow<List<Content>>

    @Query("""
        SELECT * FROM content
        WHERE (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:level IS NULL OR level = :level)
        AND (:maxDuration IS NULL OR durationMinutes <= :maxDuration)
        AND (:isFlashSession IS NULL OR isFlashSession = :isFlashSession)
        ORDER BY title ASC
    """)
    fun getFilteredContentFlow(
        type: ContentType? = null,
        category: Category? = null,
        level: ExperienceLevel? = null,
        maxDuration: Int? = null,
        isFlashSession: Boolean? = null
    ): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE id = :contentId")
    suspend fun getContentById(contentId: String): Content?

    @Query("SELECT * FROM content WHERE id = :contentId")
    fun getContentByIdFlow(contentId: String): Flow<Content?>

    @Query("""
        SELECT * FROM content
        WHERE title LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR instructorName LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchContentFlow(query: String): Flow<List<Content>>

    @Query("""
        SELECT c.* FROM content c
        INNER JOIN user_favorites uf ON c.id = uf.contentId
        WHERE uf.userId = :userId
        ORDER BY uf.createdAt DESC
    """)
    fun getUserFavoritesFlow(userId: String): Flow<List<Content>>

    @Query("SELECT * FROM content WHERE isOfflineAvailable = 1")
    fun getOfflineContentFlow(): Flow<List<Content>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: Content)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContent(content: List<Content>)

    @Update
    suspend fun updateContent(content: Content)

    @Delete
    suspend fun deleteContent(content: Content)

    @Query("DELETE FROM content WHERE id = :contentId")
    suspend fun deleteContentById(contentId: String)
}