package io.github.mdsadiqueinam.hidayah.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log

object PackageUtils {
    private const val TAG = "PackageUtils"

    /**
     * Retrieves the application label for a given package name.
     */
    fun getApplicationName(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package not found: $packageName")
            packageName
        } catch (e: SecurityException) {
            Log.w(TAG, "Security exception getting app info for $packageName")
            packageName
        }
    }

    /**
     * Retrieves the application category for a given package name.
     */
    fun getApplicationCategory(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                ApplicationInfo.CATEGORY_GAME -> "Games"
                ApplicationInfo.CATEGORY_IMAGE -> "Image"
                ApplicationInfo.CATEGORY_MAPS -> "Maps"
                ApplicationInfo.CATEGORY_NEWS -> "News"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                ApplicationInfo.CATEGORY_VIDEO -> "Video"
                else -> "App"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Package not found: $packageName")
            "App"
        } catch (e: SecurityException) {
            Log.w(TAG, "Security exception getting category for $packageName")
            "App"
        }
    }
}
