package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.usage.UsageStats
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import java.util.concurrent.TimeUnit

object HomeStatsHelper {
    private const val MILLIS_PER_HOUR = 1000 * 60 * 60
    private const val EXCELLENT_THRESHOLD_HOURS = 2
    private const val GOOD_THRESHOLD_HOURS = 3
    private const val MODERATE_THRESHOLD_HOURS = 4
    private const val HIGH_THRESHOLD_HOURS = 5
    private const val TOP_APPS_LIMIT = 3

    fun calculateTotalScreenTime(stats: Map<String, UsageStats>): Long {
        return stats.values.sumOf { it.totalTimeInForeground }
    }

    fun calculateControlledScreenTime(
        stats: Map<String, UsageStats>,
        controlledPackageNames: Set<String>
    ): Long {
        return stats.filter { controlledPackageNames.contains(it.key) }
            .values.sumOf { it.totalTimeInForeground }
    }

    fun calculateScreenTimeStatus(totalTimeHours: Double): String {
        return when {
            totalTimeHours < EXCELLENT_THRESHOLD_HOURS -> "Excellent"
            totalTimeHours < GOOD_THRESHOLD_HOURS -> "Good"
            totalTimeHours < MODERATE_THRESHOLD_HOURS -> "Moderate"
            totalTimeHours < HIGH_THRESHOLD_HOURS -> "High"
            else -> "Very High"
        }
    }

    fun buildTopAppsList(
        context: Context,
        stats: Map<String, UsageStats>
    ): List<TopApp> {
        return stats.values
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(TOP_APPS_LIMIT)
            .map { usageStats ->
                val appName = getApplicationName(context, usageStats.packageName)
                val appCategory = getApplicationCategory(context, usageStats.packageName)
                TopApp(
                    name = appName,
                    usage = formatDuration(usageStats.totalTimeInForeground),
                    category = appCategory,
                    packageName = usageStats.packageName
                )
            }
    }

    fun buildControlledAppsList(
        rawControlledApps: List<ControlledApp>,
        stats: Map<String, UsageStats>
    ): List<ControlledAppWithUsage> {
        return rawControlledApps.map { app ->
            ControlledAppWithUsage(
                app = app,
                usage = formatDuration(stats[app.packageName]?.totalTimeInForeground ?: 0L),
                limit = if (app.dailyLimit > 0) {
                    formatDuration(TimeUnit.MINUTES.toMillis(app.dailyLimit.toLong()))
                } else {
                    "No limit"
                }
            )
        }
    }

    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    private fun getApplicationName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("HomeStatsHelper", "Package not found: $packageName")
            packageName
        } catch (e: SecurityException) {
            Log.w("HomeStatsHelper", "Security exception getting app info for $packageName")
            packageName
        }
    }

    private fun getApplicationCategory(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                ApplicationInfo.CATEGORY_GAME -> "Games"
                ApplicationInfo.CATEGORY_IMAGE -> "Image"
                ApplicationInfo.CATEGORY_MAPS -> "Maps"
                ApplicationInfo.CATEGORY_NEWS -> "News"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                ApplicationInfo.CATEGORY_VIDEO -> "Video"
                else -> "App"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("HomeStatsHelper", "Package not found: $packageName")
            "App"
        } catch (e: SecurityException) {
            Log.w("HomeStatsHelper", "Security exception getting category for $packageName")
            "App"
        }
    }
}
