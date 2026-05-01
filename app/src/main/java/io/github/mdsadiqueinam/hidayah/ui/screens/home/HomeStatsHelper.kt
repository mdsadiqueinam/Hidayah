package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.usage.UsageStats
import android.content.Context
import android.util.Log
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.util.DateTimeUtils
import io.github.mdsadiqueinam.hidayah.util.PackageUtils
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
                val appName = PackageUtils.getApplicationName(context, usageStats.packageName)
                val appCategory = PackageUtils.getApplicationCategory(context, usageStats.packageName)
                TopApp(
                    name = appName,
                    usage = DateTimeUtils.formatDuration(usageStats.totalTimeInForeground),
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
                usage = DateTimeUtils.formatDuration(stats[app.packageName]?.totalTimeInForeground ?: 0L),
                limit = if (app.dailyLimit > 0) {
                    DateTimeUtils.formatDuration(TimeUnit.MINUTES.toMillis(app.dailyLimit.toLong()))
                } else {
                    "No limit"
                }
            )
        }
    }
}
