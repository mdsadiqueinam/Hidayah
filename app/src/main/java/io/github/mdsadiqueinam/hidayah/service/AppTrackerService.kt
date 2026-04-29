package io.github.mdsadiqueinam.hidayah.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.mdsadiqueinam.hidayah.ShieldActivity
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppTrackerService : Service() {

    @Inject
    lateinit var repository: AppRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var trackingJob: Job? = null

    private var currentPackageName: String? = null
    private var sessionStartTime: Long = 0L
    private var lastShieldTriggeredPackage: String? = null

    private var configCache: ShieldConfig? = null
    private var controlledAppsCache: Map<String, ControlledApp> = emptyMap()

    companion object {
        private const val CHANNEL_ID = "app_tracker_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("AppTrackerService", "Service onCreate called")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("AppTrackerService", "Service onStartCommand called")
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (trackingJob == null) {
            startTracking()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val name = "App Tracking Service"
        val descriptionText = "Monitoring app usage for Hidayah"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hidayah Protection Active")
            .setContentText("Monitoring app usage to keep you focused")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun startTracking() {
        Log.i("AppTrackerService", "Tracking is started")

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        
        // Check if permission is granted
        val nowForCheck = System.currentTimeMillis()
        val testEvents = usageStatsManager.queryEvents(nowForCheck - 1000, nowForCheck)
        if (!testEvents.hasNextEvent()) {
            Log.w("AppTrackerService", "No usage events found. Check if 'Usage Access' permission is granted.")
        }

        trackingJob = serviceScope.launch {

            val myPackageName = packageName

            // 🔹 Cache config updates
            launch {
                repository.getShieldConfig().collect {
                    Log.i("AppTrackerService", "Config updated: $it")
                    configCache = it ?: ShieldConfig() // Use default if null
                }
            }

            // 🔹 Cache controlled apps (IMPORTANT)
            launch {
                repository.getControlledApps().collect {
                    Log.i("AppTrackerService", "Controlled apps updated: ${it.size} apps")
                    controlledAppsCache = it.associateBy { app -> app.packageName }
                }
            }

            while (isActive) {

                val config = configCache
                val now = System.currentTimeMillis()

                if (config == null) {
                    delay(500)
                    continue
                }

                if (!config.isProtectionActive || config.pausedUntil > now) {
                    resetSession()
                    delay(1000)
                    continue
                }

                val startTime = now - 2000

                val events = usageStatsManager.queryEvents(startTime, now)
                val event = UsageEvents.Event()

                var latestResumedPackage: String? = null

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        latestResumedPackage = event.packageName
                    }
                }

                if (latestResumedPackage != null &&
                    latestResumedPackage != myPackageName &&
                    latestResumedPackage != "io.github.mdsadiqueinam.hidayah"
                ) {

                    // 🔹 NEW APP OPENED
                    if (latestResumedPackage != currentPackageName) {
                        currentPackageName = latestResumedPackage
                        sessionStartTime = now
                        lastShieldTriggeredPackage = null

                        val app = controlledAppsCache[currentPackageName]

                        if (app != null) {
                            triggerShield(currentPackageName!!)
                            delay(300) // prevent rapid re-trigger
                            continue
                        }
                    }

                    // 🔹 SAME APP → SESSION LIMIT CHECK
                    else {
                        val app = controlledAppsCache[currentPackageName]

                        if (app != null && app.sessionLimit > 0) {

                            val sessionDuration = now - sessionStartTime

                            if (sessionDuration >= app.sessionLimit * 60_000) {
                                triggerShield(currentPackageName!!)
                            }
                        }
                    }
                }

                delay(250)
            }
        }
    }

    private fun resetSession() {
        currentPackageName = null
        sessionStartTime = 0L
        lastShieldTriggeredPackage = null
    }

    private fun triggerShield(packageName: String) {

        if (lastShieldTriggeredPackage == packageName) return
        lastShieldTriggeredPackage = packageName

        Log.i("AppTrackerService", "Triggering shield for $packageName")

        val intent = Intent(this, ShieldActivity::class.java).apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(intent)
    }

    override fun onDestroy() {
        Log.i("AppTrackerService", "Tracking is stopped")

        super.onDestroy()
        serviceScope.cancel()
    }
}