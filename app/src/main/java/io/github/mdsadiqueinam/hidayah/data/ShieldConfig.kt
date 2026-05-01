package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shield_config")
data class ShieldConfig(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val headline: String = "",
    val subHeadline: String = "",
    val imagePath: String? = null,
    val useVideo: Boolean = false,
    val videoPath: String? = null,
    val audioPath: String? = null,
    val isProtectionActive: Boolean = true,
    val selectedPauseDuration: String? = null,
    val pausedUntil: Long = 0L, // Timestamp when pause ends
    val screenTimeLimit: Long = 5 * 60 * 60 * 1000L // 5 hours default
)
