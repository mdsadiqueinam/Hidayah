package io.github.mdsadiqueinam.hidayah.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val context: Context,
    private val controlledAppDao: ControlledAppDao
) {
    fun getInstalledApps(): List<AppItem> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        return packageManager.queryIntentActivities(intent, 0)
            .map { resolveInfo ->
                AppItem(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(packageManager).toString()
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }

    fun getControlledApps(): Flow<List<ControlledApp>> {
        return controlledAppDao.getAllControlledApps()
    }

    suspend fun getControlledApp(packageName: String): ControlledApp? {
        return controlledAppDao.getControlledApp(packageName)
    }

    suspend fun saveControlledApps(apps: List<ControlledApp>) {
        controlledAppDao.deleteAll()
        controlledAppDao.insertAll(apps)
    }

    suspend fun updateControlledApp(app: ControlledApp) {
        controlledAppDao.update(app)
    }

    suspend fun getControlledPackageNames(): List<String> {
        return controlledAppDao.getAllPackageNames()
    }
}
