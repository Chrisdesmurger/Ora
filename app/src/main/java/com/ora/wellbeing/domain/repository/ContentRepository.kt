package com.ora.wellbeing.domain.repository

import com.ora.wellbeing.data.model.ContentItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for content catalog (meditations, yoga videos, etc.) - Read-only
 * Collection: content/{contentId}
 *
 * Offline-first: Flow returns cache if network error
 */
interface ContentRepository {

    /**
     * Observes all active content in real-time
     * Returns content ordered by publishedAt DESC (newest first)
     *
     * @return Reactive Flow of all active content
     */
    fun getAllContent(): Flow<List<ContentItem>>

    /**
     * Observes content filtered by category in real-time
     * Returns content ordered by rating DESC
     *
     * @param category Content category (Méditation, Yoga, Respiration, Pilates, Bien-être)
     * @return Reactive Flow of content in the specified category
     */
    fun getContentByCategory(category: String): Flow<List<ContentItem>>

    /**
     * Observes a single content item in real-time
     *
     * @param contentId Content ID
     * @return Reactive Flow of the content item (null if not found)
     */
    fun getContent(contentId: String): Flow<ContentItem?>

    /**
     * Observes popular content in real-time
     * Returns content with isPopular=true, ordered by rating DESC
     *
     * @param limit Maximum number of items to return
     * @return Reactive Flow of popular content
     */
    fun getPopularContent(limit: Int = 10): Flow<List<ContentItem>>

    /**
     * Observes new content in real-time
     * Returns content with isNew=true, ordered by publishedAt DESC
     *
     * @param limit Maximum number of items to return
     * @return Reactive Flow of new content
     */
    fun getNewContent(limit: Int = 10): Flow<List<ContentItem>>

    /**
     * Observes content filtered by duration range in real-time
     *
     * @param minMinutes Minimum duration in minutes
     * @param maxMinutes Maximum duration in minutes
     * @return Reactive Flow of content within the duration range
     */
    fun getContentByDuration(minMinutes: Int, maxMinutes: Int): Flow<List<ContentItem>>

    /**
     * Searches content by query string (client-side filtering)
     * Firestore doesn't support full-text search, so this filters locally
     * Searches in: title, description, instructor, tags
     *
     * @param query Search query string
     * @return Reactive Flow of content matching the query
     */
    fun searchContent(query: String): Flow<List<ContentItem>>

    /**
     * Observes content filtered by instructor in real-time
     *
     * @param instructor Instructor name
     * @return Reactive Flow of content by the specified instructor
     */
    fun getContentByInstructor(instructor: String): Flow<List<ContentItem>>

    /**
     * Gets total count of active content
     *
     * @return Total number of active content items
     */
    suspend fun getTotalContentCount(): Int

    /**
     * Gets list of all unique instructors
     *
     * @return List of instructor names
     */
    suspend fun getAllInstructors(): List<String>
}
