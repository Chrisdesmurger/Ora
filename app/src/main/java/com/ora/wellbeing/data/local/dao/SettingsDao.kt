package com.ora.wellbeing.data.local.dao

import androidx.room.*
import com.ora.wellbeing.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

@Dao
interface SettingsDao {

    @Query("SELECT * FROM app_settings WHERE userId = :userId")
    suspend fun getSettings(userId: String): Settings?

    @Query("SELECT * FROM app_settings WHERE userId = :userId")
    fun getSettingsFlow(userId: String): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: Settings)

    @Update
    suspend fun updateSettings(settings: Settings)

    @Query("UPDATE app_settings SET notificationsEnabled = :enabled WHERE userId = :userId")
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean)

    @Query("UPDATE app_settings SET darkModeEnabled = :enabled WHERE userId = :userId")
    suspend fun updateDarkModeEnabled(userId: String, enabled: Boolean)

    @Query("UPDATE app_settings SET autoPlayEnabled = :enabled WHERE userId = :userId")
    suspend fun updateAutoPlayEnabled(userId: String, enabled: Boolean)

    @Query("UPDATE app_settings SET biometricAuthEnabled = :enabled WHERE userId = :userId")
    suspend fun updateBiometricAuthEnabled(userId: String, enabled: Boolean)

    @Query("UPDATE app_settings SET morningReminderTime = :time WHERE userId = :userId")
    suspend fun updateMorningReminderTime(userId: String, time: LocalTime?)

    @Query("UPDATE app_settings SET dayReminderTime = :time WHERE userId = :userId")
    suspend fun updateDayReminderTime(userId: String, time: LocalTime?)

    @Query("UPDATE app_settings SET eveningReminderTime = :time WHERE userId = :userId")
    suspend fun updateEveningReminderTime(userId: String, time: LocalTime?)

    @Query("UPDATE app_settings SET defaultSessionDuration = :duration WHERE userId = :userId")
    suspend fun updateDefaultSessionDuration(userId: String, duration: Int)

    @Delete
    suspend fun deleteSettings(settings: Settings)

    // Notification Preferences
    @Query("SELECT * FROM notification_preferences WHERE userId = :userId")
    fun getNotificationPreferencesFlow(userId: String): Flow<List<NotificationPreference>>

    @Query("SELECT * FROM notification_preferences WHERE userId = :userId AND type = :type")
    suspend fun getNotificationPreference(userId: String, type: NotificationType): NotificationPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPreference(preference: NotificationPreference)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPreferences(preferences: List<NotificationPreference>)

    @Update
    suspend fun updateNotificationPreference(preference: NotificationPreference)

    @Query("UPDATE notification_preferences SET isEnabled = :enabled WHERE userId = :userId AND type = :type")
    suspend fun updateNotificationPreferenceEnabled(userId: String, type: NotificationType, enabled: Boolean)

    @Delete
    suspend fun deleteNotificationPreference(preference: NotificationPreference)

    @Query("DELETE FROM notification_preferences WHERE userId = :userId")
    suspend fun deleteAllNotificationPreferences(userId: String)
}