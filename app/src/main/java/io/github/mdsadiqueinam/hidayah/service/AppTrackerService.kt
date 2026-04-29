package io.github.mdsadiqueinam.hidayah.service

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (trackingJob == null) {
            startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        trackingJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            
            repository.getShieldConfig().collectLatest { config ->
                if (config == null) return@collectLatest
                
                if (!config.isProtectionActive) {
                    Log.d("AppTrackerService", "Protection is disabled. Skipping tracking.")
                    return@collectLatest
                }

                val now = System.currentTimeMillis()
                if (config.pausedUntil > now) {
                    val remainingMs = config.pausedUntil - now
                    Log.d("AppTrackerService", "Protection is paused. Remaining: ${TimeUnit.MILLISECONDS.toMinutes(remainingMs)}m. Skipping tracking.")
                    return@collectLatest
                }

                while (isActive) {
                    val endTime = System.currentTimeMillis()
                    val startTime = endTime - 1000 // Last 1000ms

                    val events = usageStatsManager.queryEvents(startTime, endTime)
                    val event = UsageEvents.Event()
                    
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event)
                        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            Log.d("AppTrackerService", "Foreground App Detected: ${event.packageName}")
                        }
                    }
                    
                    delay(500) // Poll every 500ms
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
