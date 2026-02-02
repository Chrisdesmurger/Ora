package com.ora.wellbeing.data.repository

import com.ora.wellbeing.data.local.dao.SettingsDao
import com.ora.wellbeing.data.local.dao.NotificationPreferenceDao
import com.ora.wellbeing.data.local.entities.Settings
import com.ora.wellbeing.data.local.entities.NotificationPreference
import com.ora.wellbeing.data.local.entities.NotificationType
import com.ora.wellbeing.data.local.entities.NotificationFrequency
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val notificationPreferenceDao: NotificationPreferenceDao
) {

    fun getUserSettings(userId: String): Flow<Settings?> {
        return settingsDao.getSettingsFlow(userId)
    }

    suspend fun updateUserSettings(settings: Settings) {
        settingsDao.insertSettings(settings)
    }

    fun getUserNotificationPreferences(userId: String): Flow<List<NotificationPreference>> {
        return notificationPreferenceDao.getUserNotificationPreferences(userId)
    }

    suspend fun updateNotificationPreference(preference: NotificationPreference) {
        notificationPreferenceDao.updateNotificationPreference(preference)
    }

    suspend fun initializeDefaultSettings(userId: String) {
        val existingSettings = settingsDao.getSettings(userId)
        if (existingSettings == null) {
            val defaultSettings = Settings(
                userId = userId,
                notificationsEnabled = true,
                darkModeEnabled = false,
                autoPlayEnabled = true,
                downloadOnWifiOnly = true,
                defaultSessionDuration = 15,
                language = "fr"
            )
            settingsDao.insertSettings(defaultSettings)
            initializeDefaultNotificationPreferences(userId)
        }
    }

    private suspend fun initializeDefaultNotificationPreferences(userId: String) {
        val defaultPreferences = listOf(
            NotificationPreference(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = NotificationType.DAILY_REMINDER,
                isEnabled = true,
                scheduledTime = "07:00",
                frequency = NotificationFrequency.DAILY
            ),
            NotificationPreference(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = NotificationType.SESSION_REMINDER,
                isEnabled = true,
                scheduledTime = "12:00",
                frequency = NotificationFrequency.DAILY
            )
        )

        defaultPreferences.forEach { preference ->
            notificationPreferenceDao.insertNotificationPreference(preference)
        }
    }
}