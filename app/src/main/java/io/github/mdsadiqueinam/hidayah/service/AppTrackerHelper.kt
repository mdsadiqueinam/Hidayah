package io.github.mdsadiqueinam.hidayah.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import io.github.mdsadiqueinam.hidayah.ShieldActivity
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig

object AppTrackerHelper {
    private const val CHANNEL_ID = "app_tracker_channel"
    private const val POLL_LOOKBACK_MS = 1000L

    fun createNotificationChannel(context: Context) {
        val name = "App Tracking Service"
        val descriptionText = "Monitoring app usage for Hidayah"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun createNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Hidayah Protection Active")
            .setContentText("Monitoring app usage to keep you focused")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    fun isProtectionActive(config: ShieldConfig?, now: Long): Boolean =
        config != null && config.isProtectionActive && config.pausedUntil <= now

    fun pollTopPackage(usageStatsManager: UsageStatsManager, now: Long): String? {
        val startTime = now - POLL_LOOKBACK_MS
        val events = usageStatsManager.queryEvents(startTime, now)
        val event = UsageEvents.Event()

        var topPackage: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                topPackage = event.packageName
            }
        }

        return topPackage
    }

    fun getShieldIntent(context: Context, packageName: String): Intent {
        return Intent(context, ShieldActivity::class.java).apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }
}
