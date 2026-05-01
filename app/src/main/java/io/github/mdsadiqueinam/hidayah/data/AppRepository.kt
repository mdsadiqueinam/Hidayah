package io.github.mdsadiqueinam.hidayah.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val MINUTES_PER_HOUR = 60
        private const val LAST_DAY_INDEX = 6

        private const val DEFAULT_SOCIAL_PROPORTION = 0.4f
        private const val DEFAULT_PRODUCTIVITY_PROPORTION = 0.3f
        private const val DEFAULT_OTHER_PROPORTION = 0.3f

        private const val MOCK_INTERVENTION_MIN = 10
        private const val MOCK_INTERVENTION_MAX = 50

        private const val END_HOUR = 23
        private const val END_MINUTE = 59
        private const val END_SECOND = 59
        private const val END_MILLIS = 999
    }

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
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % MINUTES_PER_HOUR
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    fun getWeeklyUsageStats(): List<Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val stats = mutableListOf<Long>()

        for (i in LAST_DAY_INDEX downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            setCalendarToStartOfDay(calendar)
            val startTime = calendar.timeInMillis

            setCalendarToEndOfDay(calendar)
            val endTime = calendar.timeInMillis

            val dailyStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
            val totalTime = dailyStats?.sumOf { it.totalTimeInForeground } ?: 0L
            stats.add(totalTime)
        }
        return stats
    }

    private fun setCalendarToStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun setCalendarToEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, END_HOUR)
        calendar.set(Calendar.MINUTE, END_MINUTE)
        calendar.set(Calendar.SECOND, END_SECOND)
        calendar.set(Calendar.MILLISECOND, END_MILLIS)
    }

    fun getCategoryUsageStats(): Map<String, Float> {
        val dailyStats = getDailyUsageStats().values
        var socialTime = 0L
        var productivityTime = 0L
        var otherTime = 0L

        val socialKeywords = listOf(
            "social", "message", "chat", "instagram", "facebook", "twitter", "tiktok", "whatsapp"
        )
        val prodKeywords = listOf(
            "work", "tool", "study", "calendar", "mail", "notes", "drive", "zoom"
        )

        dailyStats.forEach { stats ->
            val pkg = stats.packageName.lowercase()
            when {
                socialKeywords.any { pkg.contains(it) } -> socialTime += stats.totalTimeInForeground
                prodKeywords.any { pkg.contains(it) } -> productivityTime += stats.totalTimeInForeground
                else -> otherTime += stats.totalTimeInForeground
            }
        }

        val total = socialTime + productivityTime + otherTime
        if (total == 0L) return mapOf(
            "Social Media" to DEFAULT_SOCIAL_PROPORTION,
            "Productivity" to DEFAULT_PRODUCTIVITY_PROPORTION,
            "Other" to DEFAULT_OTHER_PROPORTION
        )

        return mapOf(
            "Social Media" to socialTime.toFloat() / total,
            "Productivity" to productivityTime.toFloat() / total,
            "Other" to otherTime.toFloat() / total
        )
    }

    fun getInterventionsCount(): Int = (MOCK_INTERVENTION_MIN..MOCK_INTERVENTION_MAX).random()

    fun getDailyUsageStats(): Map<String, UsageStats> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        setCalendarToStartOfDay(calendar)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        return usageStatsManager.queryAndAggregateUsageStats(startTime, endTime) ?: emptyMap()
    }
}
