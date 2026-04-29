package io.github.mdsadiqueinam.hidayah.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val context: Context,
    private val controlledAppDao: ControlledAppDao,
    private val shieldConfigDao: ShieldConfigDao
) {
    fun getInstalledApps(): List<AppItem> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val usageStats = getDailyUsageStats()

        return (packageManager.queryIntentActivities(intent, 0) ?: emptyList())
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val usageTime = usageStats[packageName]?.totalTimeInForeground ?: 0L
                AppItem(
                    packageName = packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString(),
                    usageTime = usageTime,
                    formattedUsage = formatDuration(usageTime)
                )
            }
            .distinctBy { it.packageName }
            .sortedByDescending { it.usageTime }
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    fun getDailyUsageStats(): Map<String, UsageStats> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return usageStatsManager.queryAndAggregateUsageStats(startTime, endTime) ?: emptyMap()
    }

    fun getControlledApps(): Flow<List<ControlledApp>> {
        return controlledAppDao.getAllControlledApps()
    }

    suspend fun getControlledApp(packageName: String): ControlledApp? {
        return controlledAppDao.getControlledApp(packageName)
    }

    suspend fun saveControlledApps(apps: List<ControlledApp>) {
        controlledAppDao.deleteAll()
        controlledAppDao.insertAll(apps)
    }

    suspend fun updateControlledApp(app: ControlledApp) {
        controlledAppDao.update(app)
    }

    suspend fun removeControlledApp(packageName: String) {
        controlledAppDao.delete(packageName)
    }

    suspend fun getControlledPackageNames(): List<String> {
        return controlledAppDao.getAllPackageNames()
    }

    fun getShieldConfig(): Flow<ShieldConfig?> {
        return shieldConfigDao.getShieldConfig()
    }

    suspend fun updateShieldConfig(config: ShieldConfig) {
        shieldConfigDao.insertOrUpdate(config)
    }
}
