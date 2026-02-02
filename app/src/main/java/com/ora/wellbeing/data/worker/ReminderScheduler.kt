package com.ora.wellbeing.data.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleEveningReminder(hourOfDay: Int = 20, minute: Int = 0) {
        // Calcul du délai jusqu'à la prochaine notification
        val currentTime = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)

            // Si l'heure est déjà passée aujourd'hui, programmer pour demain
            if (timeInMillis <= currentTime) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - currentTime

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<EveningReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag("evening_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            EveningReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    fun cancelEveningReminder() {
        workManager.cancelUniqueWork(EveningReminderWorker.WORK_NAME)
    }

    fun isReminderScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(EveningReminderWorker.WORK_NAME)
        return try {
            workInfos.get().any { workInfo ->
                workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            false
        }
    }
}