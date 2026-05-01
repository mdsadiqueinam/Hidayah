package io.github.mdsadiqueinam.hidayah.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.provider.Settings

object PermissionUtils {
    /**
     * Checks if the app has the Usage Stats permission.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if the app has the Overlay (Draw over other apps) permission.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
}
