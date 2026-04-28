package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "controlled_apps")
data class ControlledApp(
    @PrimaryKey val packageName: String,
    val appName: String
)
