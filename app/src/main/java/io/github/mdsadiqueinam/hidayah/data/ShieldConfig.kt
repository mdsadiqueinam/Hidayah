package io.github.mdsadiqueinam.hidayah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shield_config")
data class ShieldConfig(
    @PrimaryKey val id: Int = 1, // Singleton entry
    val headline: String = "",
    val subHeadline: String = "",
    val imagePath: String? = null
)
