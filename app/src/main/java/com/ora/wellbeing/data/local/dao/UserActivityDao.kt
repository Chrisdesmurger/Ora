package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserActivityDao {

    @Query("SELECT * FROM user_activities WHERE userId = :userId ORDER BY startedAt DESC")
    fun getUserActivitiesFlow(userId: String): Flow<List<UserActivity>>

    @Query("SELECT * FROM user_activities WHERE userId = :userId ORDER BY startedAt DESC LIMIT :limit")
    suspend fun getRecentUserActivities(userId: String, limit: Int = 20): List<UserActivity>

    @Query("SELECT * FROM user_activities WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedActivitiesFlow(userId: String): Flow<List<UserActivity>>

    @Query("SELECT * FROM user_activities WHERE userId = :userId AND contentId = :contentId ORDER BY startedAt DESC")
    fun getActivitiesForContentFlow(userId: String, contentId: String): Flow<List<UserActivity>>

    @Query("SELECT * FROM user_activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): UserActivity?

    @Query("""
        SELECT COUNT(*) FROM user_activities
        WHERE userId = :userId AND isCompleted = 1
        AND substr(completedAt, 1, 10) = substr(:date, 1, 10)
    """)
    suspend fun getCompletedSessionsForDate(userId: String, date: String): Int

    @Query("""
        SELECT COALESCE(SUM(durationMinutes), 0) FROM user_activities
        WHERE userId = :userId AND isCompleted = 1
    """)
    suspend fun getTotalMinutesSpent(userId: String): Int

    @Query("""
        SELECT COUNT(*) FROM user_activities
        WHERE userId = :userId AND isCompleted = 1
    """)
    suspend fun getTotalCompletedSessions(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivity)

    @Update
    suspend fun updateActivity(activity: UserActivity)

    @Query("UPDATE user_activities SET isCompleted = 1, completedAt = :completedAt, durationMinutes = :duration WHERE id = :activityId")
    suspend fun completeActivity(activityId: String, completedAt: LocalDateTime, duration: Int)

    @Query("UPDATE user_activities SET rating = :rating, notes = :notes WHERE id = :activityId")
    suspend fun rateActivity(activityId: String, rating: Int, notes: String?)

    @Delete
    suspend fun deleteActivity(activity: UserActivity)

    // User Favorites
    @Query("SELECT * FROM user_favorites WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserFavoritesFlow(userId: String): Flow<List<UserFavorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND contentId = :contentId)")
    suspend fun isContentFavorited(userId: String, contentId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: UserFavorite)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND contentId = :contentId")
    suspend fun removeFromFavorites(userId: String, contentId: String)

    // User Stats
    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: String): UserStats?

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun getUserStatsFlow(userId: String): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(stats: UserStats)

    @Query("UPDATE user_stats SET streakDays = :streak WHERE userId = :userId")
    suspend fun updateCurrentStreak(userId: String, streak: Int)
}