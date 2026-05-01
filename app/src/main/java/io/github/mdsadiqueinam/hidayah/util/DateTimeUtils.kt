package io.github.mdsadiqueinam.hidayah.util

import java.util.Calendar

object DateTimeUtils {
    private const val END_HOUR = 23
    private const val END_MINUTE = 59
    private const val END_SECOND = 59
    private const val END_MILLIS = 999

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
}
