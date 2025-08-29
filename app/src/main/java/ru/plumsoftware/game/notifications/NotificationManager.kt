package ru.plumsoftware.game.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import ru.plumsoftware.game.MainActivity
import ru.plumsoftware.game.R

class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "quiz_reminders"
        const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        channel.enableLights(true)
        channel.lightColor = Color.GREEN
        channel.enableVibration(true)

        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleDailyNotification() {
        // This would typically use WorkManager or AlarmManager
        // For now, we'll just show how to create the notification
        showDailyNotification()
    }

    fun showDailyNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val remoteViews = RemoteViews(context.packageName, R.layout.notification_layout)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.profile)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotifications() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
} 