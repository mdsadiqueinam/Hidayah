package io.github.mdsadiqueinam.hidayah.service

import android.app.KeyguardManager
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
import android.provider.Settings
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        // Check Overlay Permission (required for Activity start from Service)
        if (!Settings.canDrawOverlays(this)) {
            Log.w("AppTrackerService", "Overlay permission NOT granted. ShieldActivity might not show.")
        }

        trackingJob = serviceScope.launch {

            val myPackageName = packageName

            // 🔹 Cache config updates
            launch {
                repository.getShieldConfig().collect {
                    Log.i("AppTrackerService", "Config updated: $it")
                    configCache = it ?: ShieldConfig()
                }
            }

            // 🔹 Cache controlled apps
            launch {
                repository.getControlledApps().collect {
                    Log.i("AppTrackerService", "Controlled apps updated: ${it.size} apps")
                    controlledAppsCache = it.associateBy { app -> app.packageName }
                }
            }

            while (isActive) {
                val config = configCache
                val now = System.currentTimeMillis()

                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isKeyguardLocked) {
                    delay(1000)
                    continue
                }

                if (config == null || !config.isProtectionActive || config.pausedUntil > now) {
                    resetSession()
                    delay(1000)
                    continue
                }

                // Poll events from the last 1 second for instant detection
                val startTime = now - 1000
                val events = usageStatsManager.queryEvents(startTime, now)
                val event = UsageEvents.Event()

                var topPackage: String? = null
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        topPackage = event.packageName
                    }
                }

                if (topPackage != null && topPackage != myPackageName && topPackage != "io.github.mdsadiqueinam.hidayah") {
                    
                    // 1. Instant Shield on App Open
                    if (topPackage != currentPackageName) {
                        Log.i("AppTrackerService", "New app detected: $topPackage")
                        currentPackageName = topPackage
                        sessionStartTime = now
                        lastShieldTriggeredPackage = null // Reset trigger for new app

                        val app = controlledAppsCache[currentPackageName]
                        if (app != null) {
                            Log.i("AppTrackerService", "Controlled app opened: $currentPackageName. Triggering shield.")
                            triggerShield(currentPackageName!!)
                            delay(500) // Breather
                            continue
                        }
                    } 
                    
                    // 2. Session Limit Enforcement
                    else {
                        val app = controlledAppsCache[currentPackageName]
                        if (app != null && app.sessionLimit > 0) {
                            val sessionDurationMs = now - sessionStartTime
                            if (sessionDurationMs >= app.sessionLimit * 60_000) {
                                Log.i("AppTrackerService", "Session limit hit for $currentPackageName")
                                triggerShield(currentPackageName!!)
                            }
                        }
                    }
                } else if (topPackage == "io.github.mdsadiqueinam.hidayah") {
                    // If our own app is on top, don't reset session if it was a controlled app
                    // but don't count session time either, or just ignore.
                }

                delay(300) 
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

        Log.i("AppTrackerService", "Actually triggering shield for $packageName")

        val intent = Intent(this, ShieldActivity::class.java).apply {
            putExtra("packageName", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppTrackerService", "Failed to start ShieldActivity", e)
        }
    }

    override fun onDestroy() {
        Log.i("AppTrackerService", "Tracking is stopped")
        super.onDestroy()
        serviceScope.cancel()
    }
}