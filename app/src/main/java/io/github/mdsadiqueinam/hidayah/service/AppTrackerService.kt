package io.github.mdsadiqueinam.hidayah.service

import android.app.KeyguardManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
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
        private const val NOTIFICATION_ID = 1
        private const val POLL_INTERVAL_MS = 300L
        private const val CHECK_INTERVAL_MS = 1000L
        private const val SHIELD_TRIGGER_DELAY_MS = 500L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("AppTrackerService", "Service onCreate called")
        AppTrackerHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("AppTrackerService", "Service onStartCommand called")

        val notification = AppTrackerHelper.createNotification(this)
        startForeground(NOTIFICATION_ID, notification)

        if (trackingJob == null) {
            startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        Log.i("AppTrackerService", "Tracking is started")

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        if (!Settings.canDrawOverlays(this)) {
            Log.w(
                "AppTrackerService",
                "Overlay permission NOT granted. ShieldActivity might not show."
            )
        }

        trackingJob = serviceScope.launch {
            val myPackageName = packageName

            launch {
                repository.getShieldConfig().collect {
                    Log.i("AppTrackerService", "Config updated: $it")
                    configCache = it ?: ShieldConfig()
                }
            }

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

        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
            delay(CHECK_INTERVAL_MS)
            return
        }

        if (!AppTrackerHelper.isProtectionActive(config, now)) {
            resetSession()
            delay(CHECK_INTERVAL_MS)
            return
        }

        val topPackage = AppTrackerHelper.pollTopPackage(usageStatsManager, now)

        if (topPackage != null && topPackage != myPackageName &&
            topPackage != "io.github.mdsadiqueinam.hidayah"
        ) {
            handleTopPackageDetected(topPackage, now)
        }

        delay(POLL_INTERVAL_MS)
    }

    private suspend fun handleTopPackageDetected(topPackage: String, now: Long) {
        if (topPackage != currentPackageName) {
            Log.i("AppTrackerService", "New app detected: $topPackage")
            currentPackageName = topPackage
            sessionStartTime = now
            lastShieldTriggeredPackage = null

            val app = controlledAppsCache[currentPackageName]
            if (app != null) {
                Log.i(
                    "AppTrackerService",
                    "Controlled app opened: $currentPackageName. Triggering shield."
                )
                triggerShield(currentPackageName!!)
                delay(SHIELD_TRIGGER_DELAY_MS)
            }
        } else {
            val app = controlledAppsCache[topPackage]
            if (app != null && app.sessionLimit > 0) {
                val sessionDurationMs = now - sessionStartTime
                val sessionLimitMs = app.sessionLimit * 60000L

                if (sessionDurationMs >= sessionLimitMs) {
                    Log.i("AppTrackerService", "Session limit hit for $packageName")
                    triggerShield(topPackage)
                }
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

        val intent = AppTrackerHelper.getShieldIntent(this, packageName)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppTrackerService", "Failed to start ShieldActivity: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        Log.i("AppTrackerService", "Tracking is stopped")
        super.onDestroy()
        serviceScope.cancel()
    }
}
