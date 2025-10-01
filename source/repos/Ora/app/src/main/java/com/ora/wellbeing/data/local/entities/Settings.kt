package com.ora.wellbeing.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "app_settings")
data class Settings(
    @PrimaryKey
    val userId: String,
    val notificationsEnabled: Boolean = true,
    val morningReminderTime: LocalTime? = LocalTime.of(7, 0),
    val dayReminderTime: LocalTime? = LocalTime.of(12, 0),
    val eveningReminderTime: LocalTime? = LocalTime.of(19, 0),
    val darkModeEnabled: Boolean = false,
    val autoPlayEnabled: Boolean = true,
    val downloadOnWifiOnly: Boolean = true,
    val keepScreenOn: Boolean = false,
    val defaultSessionDuration: Int = 15, // minutes
    val language: String = "fr",
    val biometricAuthEnabled: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)