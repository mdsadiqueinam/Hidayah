package io.github.mdsadiqueinam.hidayah.data

import io.github.mdsadiqueinam.hidayah.util.DateTimeUtils

data class AppItem(
    val packageName: String,
    val appName: String,
    val usageTime: Long = 0
) {
    val formattedUsage: String get() = DateTimeUtils.formatDuration(usageTime)
}
