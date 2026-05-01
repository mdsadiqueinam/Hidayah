package io.github.mdsadiqueinam.hidayah.util

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DateTimeUtils {
    private const val END_HOUR = 23
    private const val END_MINUTE = 59
    private const val END_SECOND = 59
    private const val END_MILLIS = 999
    private const val MINUTES_PER_HOUR = 60

    /**
     * Sets the given calendar to the start of the day (00:00:00.000).
     */
    fun setStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    /**
     * Sets the given calendar to the end of the day (23:59:59.999).
     */
    fun setEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, END_HOUR)
        calendar.set(Calendar.MINUTE, END_MINUTE)
        calendar.set(Calendar.SECOND, END_SECOND)
        calendar.set(Calendar.MILLISECOND, END_MILLIS)
    }

    /**
     * Returns the short weekday name (e.g., "MON", "TUE") for the given calendar.
     */
    fun getShortWeekdayName(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }
    }

    /**
     * Formats a duration in milliseconds to a human-readable string like "2h 30m" or "45m".
     */
    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % MINUTES_PER_HOUR
        var formatted = ""
        if (hours == 0L && minutes == 0L) return "0m"
        if (hours > 0) formatted = "${hours}h"
        if (minutes > 0) formatted = "$formatted ${minutes}m"

        return formatted.trim()
    }

    /**
     * Formats a minutes to a human-readable string like "2h 30m" or "45m".
     */
    fun formatMinutes(minutes: Int): String = formatDuration(minutes * 60000L)

    /**
     * Parses a pause duration string like "10m" or "2h" into milliseconds.
     */
    fun parsePauseDuration(duration: String): Long {
        return try {
            val value = duration.dropLast(1).toLong()
            val unit = duration.last()
            when (unit) {
                'm' -> TimeUnit.MINUTES.toMillis(value)
                'h' -> TimeUnit.HOURS.toMillis(value)
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}
