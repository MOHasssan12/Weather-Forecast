package com.example.weatherforecast.Alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weatherforecast.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show notification when the alarm is triggered
        showNotification(context, intent)
    }

    private fun showNotification(context: Context, intent: Intent) {
        val notificationId = 1
        val channelId = "alarm_channel_id"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_alert)  // Use a suitable icon
            .setContentTitle("Alarm Alert")
            .setContentText(intent.getStringExtra("message") ?: "The alarm you set has gone off!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        // Check for permission before showing the notification
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, do not proceed with the notification
            return
        }

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
