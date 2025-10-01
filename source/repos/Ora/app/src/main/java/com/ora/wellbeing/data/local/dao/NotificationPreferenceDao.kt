package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.NotificationPreference
import com.ora.wellbeing.data.local.entities.NotificationType
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferenceDao {

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun getUserNotificationPreferences(userId: String): Flow<List<NotificationPreference>>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId AND type = :type LIMIT 1")
    suspend fun getNotificationPreference(userId: String, type: NotificationType): NotificationPreference?

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId AND isEnabled = 1")
    suspend fun getEnabledNotifications(userId: String): List<NotificationPreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPreference(preference: NotificationPreference)

    @Update
    suspend fun updateNotificationPreference(preference: NotificationPreference)

    @Query("UPDATE notification_preferences SET isEnabled = :isEnabled WHERE userId = :userId AND type = :type")
    suspend fun updateNotificationEnabled(userId: String, type: NotificationType, isEnabled: Boolean)

    @Delete
    suspend fun deleteNotificationPreference(preference: NotificationPreference)

    @Query("DELETE FROM notification_preferences WHERE userId = :userId")
    suspend fun deleteUserNotificationPreferences(userId: String)
}