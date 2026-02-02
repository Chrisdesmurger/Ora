package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdFlow(userId: String): Flow<User?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET lastActiveAt = :lastActiveAt WHERE id = :userId")
    suspend fun updateLastActiveTime(userId: String, lastActiveAt: java.time.LocalDateTime)

    @Query("UPDATE users SET isOnboardingCompleted = :completed WHERE id = :userId")
    suspend fun updateOnboardingStatus(userId: String, completed: Boolean)

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE id = :userId")
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean)

    @Query("UPDATE users SET darkModeEnabled = :enabled WHERE id = :userId")
    suspend fun updateDarkModeSettings(userId: String, enabled: Boolean)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}