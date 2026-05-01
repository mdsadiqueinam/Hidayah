package io.github.mdsadiqueinam.hidayah.util

import java.util.concurrent.TimeUnit

object TimeUtils {
    private const val MINUTES_PER_HOUR = 60

    /**
     * Formats a duration in milliseconds to a human-readable string like "2h 30m" or "45m".
     */
    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % MINUTES_PER_HOUR
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
