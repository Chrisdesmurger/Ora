package com.ora.wellbeing.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ora.wellbeing.MainActivity
import com.ora.wellbeing.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EveningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "evening_reminder_channel"
        const val CHANNEL_NAME = "Rappels du soir"
        const val WORK_NAME = "evening_reminder_work"
    }

    override suspend fun doWork(): Result {
        return try {
            showEveningReminderNotification()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showEveningReminderNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Créer le canal de notification pour Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les rappels du soir"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent pour ouvrir l'app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Messages motivants aléatoires
        val messages = listOf(
            "Prenez quelques minutes pour vos gratitudes du jour 🙏",
            "Comment s'est passée votre journée ? Partagez vos pensées 💭",
            "Moment de réflexion : notez vos accomplissements d'aujourd'hui ✨",
            "Votre journal vous attend pour clôturer cette belle journée 📖",
            "Quelques minutes de méditation pour terminer en douceur ? 🧘‍♀️"
        )

        val randomMessage = messages.random()

        // Créer la notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Vous devrez ajouter cette icône
            .setContentTitle("Ora - Moment de réflexion")
            .setContentText(randomMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(randomMessage))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}