package io.github.mdsadiqueinam.hidayah.data

import android.content.Context
import android.content.Intent

data class AppItem(
    val packageName: String,
    val appName: String
)

class AppRepository(private val context: Context) {
    fun getInstalledApps(): List<AppItem> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return packageManager.queryIntentActivities(intent, 0)
            .filter { resolveInfo ->
                // Filter out system apps if they don't have a launcher icon 
                // and are marked as system apps. 
                // We keep them if they are in the launcher but we can be more strict:
                val isSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystemApp = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                // If it's a system app, we check if it's one we typically want to see in a "drawer"
                // Actually, queryIntentActivities with CATEGORY_LAUNCHER already filters for drawer apps.
                // The issue was visibility. Now we just filter out fundamental system apps if desired.
                // For "Drawer only", CATEGORY_LAUNCHER is already correct.
                true 
            }
            .map { resolveInfo ->
                AppItem(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }
}
