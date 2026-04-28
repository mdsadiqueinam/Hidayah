package io.github.mdsadiqueinam.hidayah.data

data class AppItem(
    val packageName: String,
    val appName: String,
    val usageTime: Long = 0,
    val formattedUsage: String = "0m"
)
