package io.github.mdsadiqueinam.hidayah.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.github.mdsadiqueinam.hidayah.MainActivity
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (trackingJob == null) {
            startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        trackingJob = serviceScope.launch {
            val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val myPackageName = packageName

            repository.getShieldConfig().collectLatest { config ->
                if (config == null) return@collectLatest

                if (!config.isProtectionActive) {
                    Log.d("AppTrackerService", "Protection is disabled. Skipping tracking.")
                    resetSession()
                    return@collectLatest
                }

                while (isActive) {
                    val now = System.currentTimeMillis()
                    
                    // Check if paused
                    if (config.pausedUntil > now) {
                        delay(1000)
                        continue
                    }

                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000 // Last 1000ms

                    val events = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()

                    var latestResumedPackage: String? = null
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event)
                        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            latestResumedPackage = event.packageName
                        }
                    }

                    if (latestResumedPackage != null && latestResumedPackage != myPackageName) {
                        if (latestResumedPackage != currentPackageName) {
                            // New app opened (Switch detected)
                            currentPackageName = latestResumedPackage
                            sessionStartTime = now
                            lastShieldTriggeredPackage = null // Reset for new session
                            
                            Log.d("AppTrackerService", "New session detected: $currentPackageName")
                            
                            // 1. INSTANT SHIELD: Trigger immediately if app is controlled
                            val controlledApp = repository.getControlledApp(currentPackageName!!)
                            if (controlledApp != null) {
                                triggerShield(currentPackageName!!)
                            }
                        } else {
                            // Still in the same app, check for SESSION LIMIT
                            val controlledApp = repository.getControlledApp(currentPackageName!!)
                            if (controlledApp != null && controlledApp.sessionLimit > 0) {
                                val sessionDurationMinutes = TimeUnit.MILLISECONDS.toMinutes(now - sessionStartTime)
                                if (sessionDurationMinutes >= controlledApp.sessionLimit) {
                                    // 2. SESSION LIMIT REACHED: Trigger shield if not already triggered for this limit
                                    if (lastShieldTriggeredPackage != currentPackageName) {
                                        triggerShield(currentPackageName!!)
                                        lastShieldTriggeredPackage = currentPackageName
                                    }
                                }
                            }
                        }
                    }

                    delay(500) // Poll every 500ms
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
        Log.d("AppTrackerService", "Session limit reached for $packageName. Triggering shield.")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("show_shield", packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
