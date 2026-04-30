package io.github.mdsadiqueinam.hidayah.data

import androidx.annotation.DrawableRes
import io.github.mdsadiqueinam.hidayah.R

sealed class ShieldImage {
    data class Resource(@param:DrawableRes val resId: Int) : ShieldImage()
    data class UriImage(val uri: String) : ShieldImage()

    companion object {
        fun fromPath(path: String?): ShieldImage {
            return when {
                path == null -> Resource(R.drawable.shield_bg_1)
                path.startsWith("res:") -> {
                    path.substringAfter("res:")
                    // Note: This requires access to resources, maybe better to handle in ViewModel
                    // For now, let's assume we can map it or just use the ID if we store it as ID
                    Resource(R.drawable.shield_bg_1) // Placeholder
                }

                else -> UriImage(path)
            }
        }
    }
}

val defaultShieldImageResources = listOf(
    R.drawable.shield_bg_1,
    R.drawable.shield_bg_2,
    R.drawable.shield_bg_3,
    R.drawable.shield_bg_4
)
