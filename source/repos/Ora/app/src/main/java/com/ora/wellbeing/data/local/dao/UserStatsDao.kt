package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    suspend fun getUserStats(userId: String): UserStats?

    @Query("SELECT * FROM user_stats WHERE userId = :userId LIMIT 1")
    fun getUserStatsFlow(userId: String): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Update
    suspend fun updateUserStats(stats: UserStats)

    @Query("UPDATE user_stats SET totalSessions = totalSessions + 1, totalMinutes = totalMinutes + :minutes WHERE userId = :userId")
    suspend fun incrementSessionStats(userId: String, minutes: Int)

    @Query("UPDATE user_stats SET streakDays = :streakDays WHERE userId = :userId")
    suspend fun updateStreak(userId: String, streakDays: Int)

    @Delete
    suspend fun deleteUserStats(stats: UserStats)

    @Query("DELETE FROM user_stats WHERE userId = :userId")
    suspend fun deleteUserStatsById(userId: String)
}