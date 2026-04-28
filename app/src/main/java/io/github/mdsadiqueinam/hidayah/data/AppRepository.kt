package io.github.mdsadiqueinam.hidayah.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val context: Context,
    private val controlledAppDao: ControlledAppDao
) {
    fun getInstalledApps(): List<AppItem> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return packageManager.queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                AppItem(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun getDailyUsageStats(): Map<String, UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
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
}
