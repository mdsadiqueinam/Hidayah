package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "controlled_apps")
data class ControlledApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimit: Int = 0, // 0 for "No Limit"
    val openDelay: Int = 0, // In seconds
    val sessionLimit: Int = 0, // In minutes
    val isHardLocked: Boolean = false
)
