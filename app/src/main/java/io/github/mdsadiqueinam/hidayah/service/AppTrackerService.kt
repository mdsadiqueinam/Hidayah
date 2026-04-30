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
        private const val POLL_INTERVAL_MS = 300L
        private const val CHECK_INTERVAL_MS = 1000L
        private const val POLL_LOOKBACK_MS = 1000L
        private const val SHIELD_TRIGGER_DELAY_MS = 500L
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
                processTrackingIteration(usageStatsManager, myPackageName)
            }
        }
    }

    private suspend fun processTrackingIteration(
        usageStatsManager: UsageStatsManager,
        myPackageName: String
    ) {
        val config = configCache
        val now = System.currentTimeMillis()

        // Check if device is locked
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
            delay(CHECK_INTERVAL_MS)
            return
        }

        // Check if protection is active
        if (!isProtectionActive(config, now)) {
            resetSession()
            delay(CHECK_INTERVAL_MS)
            return
        }

        // Poll for top package
        val topPackage = pollTopPackage(usageStatsManager, now)

        // Process the detected app
        if (topPackage != null && topPackage != myPackageName &&
            topPackage != "io.github.mdsadiqueinam.hidayah"
        ) {
            handleTopPackageDetected(topPackage, now)
        } else if (topPackage == "io.github.mdsadiqueinam.hidayah") {
            // If our own app is on top, don't reset session but don't count time either
        }

        delay(POLL_INTERVAL_MS)
    }

    private fun isProtectionActive(config: ShieldConfig?, now: Long): Boolean =
        config != null && config.isProtectionActive && config.pausedUntil <= now

    private fun pollTopPackage(usageStatsManager: UsageStatsManager, now: Long): String? {
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

    private suspend fun handleTopPackageDetected(topPackage: String, now: Long) {
        if (topPackage != currentPackageName) {
            handleNewAppDetected(topPackage, now)
        } else {
            handleContinuedAppSession(topPackage, now)
        }
    }

    private suspend fun handleNewAppDetected(topPackage: String, now: Long) {
        Log.i("AppTrackerService", "New app detected: $topPackage")
        currentPackageName = topPackage
        sessionStartTime = now
        lastShieldTriggeredPackage = null // Reset trigger for new app

        val app = controlledAppsCache[currentPackageName]
        if (app != null) {
            Log.i(
                "AppTrackerService",
                "Controlled app opened: $currentPackageName. Triggering shield."
            )
            triggerShield(currentPackageName!!)
            delay(SHIELD_TRIGGER_DELAY_MS) // Breather
        }
    }

    private fun handleContinuedAppSession(topPackage: String, now: Long) {
        val app = controlledAppsCache[topPackage]
        if (app != null && app.sessionLimit > 0) {
            enforceSessionLimit(topPackage, now, app)
        }
    }

    private fun enforceSessionLimit(
        packageName: String,
        now: Long,
        app: ControlledApp
    ) {
        val sessionDurationMs = now - sessionStartTime
        val sessionLimitMs = app.sessionLimit * 60000L

        if (sessionDurationMs >= sessionLimitMs) {
            Log.i("AppTrackerService", "Session limit hit for $packageName")
            triggerShield(packageName)
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
        } catch (e: IllegalStateException) {
            Log.e("AppTrackerService", "Failed to start ShieldActivity: ${e.message}", e)
        } catch (e: SecurityException) {
            Log.e("AppTrackerService", "Security exception when starting ShieldActivity: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.i("AppTrackerService", "Tracking is stopped")
        super.onDestroy()
        serviceScope.cancel()
    }
}
