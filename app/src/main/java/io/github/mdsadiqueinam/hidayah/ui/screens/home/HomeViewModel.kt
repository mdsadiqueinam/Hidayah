package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TopApp(
    val name: String,
    val usage: String,
    val category: String,
    val packageName: String
)

data class ControlledAppWithUsage(
    val app: ControlledApp,
    val usage: String,
    val limit: String = "No limit"
)

data class HomeUiState(
    val title: String = "Controlled Apps",
    val controlledApps: List<ControlledAppWithUsage> = emptyList(),
    val isProtectionActive: Boolean = true,
    val isFocusModeActive: Boolean = false,
    val selectedPauseDuration: String? = null,
    val totalScreenTime: String = "0h 0m",
    val controlledScreenTime: String = "0h 0m",
    val screenTimeLimit: String = "5h 0m",
    val screenTimeLimitMs: Long = TimeUnit.HOURS.toMillis(DEFAULT_SCREEN_TIME_LIMIT_HOURS),
    val totalScreenTimeMs: Long = 0,
    val controlledScreenTimeMs: Long = 0,
    val screenTimePercentage: String = "0% of the day",
    val screenTimeStatus: String = "Excellent",
    val topApps: List<TopApp> = emptyList(),
    val isUsageStatsPermissionGranted: Boolean = true,
    val onAddClick: () -> Unit = {},
    val onProtectionToggle: (Boolean) -> Unit = {},
    val onFocusModeToggle: (Boolean) -> Unit = {},
    val onPauseDurationChange: (String?) -> Unit = {}
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val MILLIS_PER_HOUR = 1000 * 60 * 60
        private const val REFRESH_INTERVAL_MS = 60000L
        private const val EXCELLENT_THRESHOLD_HOURS = 2
        private const val GOOD_THRESHOLD_HOURS = 3
        private const val MODERATE_THRESHOLD_HOURS = 4
        private const val HIGH_THRESHOLD_HOURS = 5
        private const val DEFAULT_SCREEN_TIME_LIMIT_HOURS = 5L
        private const val TOP_APPS_LIMIT = 3
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var rawControlledApps: List<ControlledApp> = emptyList()
    private var shieldConfig: ShieldConfig = ShieldConfig()

    init {
        _uiState.update { state ->
            state.copy(
                onProtectionToggle = { active ->
                    updateProtectionStatus(active)
                },
                onFocusModeToggle = { active ->
                    _uiState.update { it.copy(isFocusModeActive = active) }
                },
                onPauseDurationChange = { duration ->
                    updatePauseDuration(duration)
                }
            )
        }
        observeShieldConfig()
        observeControlledApps()
        checkPermission()
        startStatsRefresh()
    }

    private fun observeShieldConfig() {
        repository.getShieldConfig()
            .onEach { config ->
                if (config != null) {
                    shieldConfig = config
                    _uiState.update {
                        it.copy(
                            isProtectionActive = config.isProtectionActive,
                            selectedPauseDuration = config.selectedPauseDuration
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateProtectionStatus(active: Boolean) {
        val newConfig = shieldConfig.copy(isProtectionActive = active)
        saveShieldConfig(newConfig)
    }

    private fun updatePauseDuration(duration: String?) {
        val pausedUntil = if (duration != null) {
            System.currentTimeMillis() + parsePauseDuration(duration)
        } else {
            0L
        }
        val newConfig = shieldConfig.copy(
            selectedPauseDuration = duration,
            pausedUntil = pausedUntil
        )
        saveShieldConfig(newConfig)
    }

    private fun parsePauseDuration(duration: String): Long {
        return try {
            val value = duration.dropLast(1).toLong()
            val unit = duration.last()
            when (unit) {
                'm' -> TimeUnit.MINUTES.toMillis(value)
                'h' -> TimeUnit.HOURS.toMillis(value)
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun saveShieldConfig(config: ShieldConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShieldConfig(config)
        }
    }

    private fun checkPermission() {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val granted = mode == AppOpsManager.MODE_ALLOWED
        _uiState.update { it.copy(isUsageStatsPermissionGranted = granted) }
        if (granted) {
            refreshUsageStats()
        }
    }

    fun onResume() {
        checkPermission()
    }

    private fun startStatsRefresh() {
        viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isUsageStatsPermissionGranted) {
                    refreshUsageStats()
                }
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    private fun observeControlledApps() {
        viewModelScope.launch {
            repository.getControlledApps().collect { apps ->
                rawControlledApps = apps
                refreshUsageStats()
            }
        }
    }

    private fun refreshUsageStats() {
        if (!_uiState.value.isUsageStatsPermissionGranted) return

        val stats = repository.getDailyUsageStats()
        val controlledPackageNames = rawControlledApps.map { it.packageName }.toSet()

        // Calculate screen time totals
        val totalTimeMs = calculateTotalScreenTime(stats)
        val controlledTimeMs = calculateControlledScreenTime(stats, controlledPackageNames)

        // Determine status
        val totalTimeHours = totalTimeMs.toDouble() / MILLIS_PER_HOUR
        val status = calculateScreenTimeStatus(totalTimeHours)

        // Build lists
        val topApps = buildTopAppsList(stats)
        val controlledAppsWithUsage = buildControlledAppsList(stats)

        // Update UI state
        _uiState.update { state ->
            state.copy(
                totalScreenTime = formatDuration(totalTimeMs),
                controlledScreenTime = formatDuration(controlledTimeMs),
                totalScreenTimeMs = totalTimeMs,
                controlledScreenTimeMs = controlledTimeMs,
                screenTimeStatus = status,
                topApps = topApps,
                controlledApps = controlledAppsWithUsage
            )
        }
    }

    private fun calculateTotalScreenTime(
        stats: Map<String, android.app.usage.UsageStats>
    ): Long {
        return stats.values.sumOf { it.totalTimeInForeground }
    }

    private fun calculateControlledScreenTime(
        stats: Map<String, android.app.usage.UsageStats>,
        controlledPackageNames: Set<String>
    ): Long {
        return stats.filter { controlledPackageNames.contains(it.key) }
            .values.sumOf { it.totalTimeInForeground }
    }

    private fun calculateScreenTimeStatus(totalTimeHours: Double): String {
        return when {
            totalTimeHours < EXCELLENT_THRESHOLD_HOURS -> "Excellent"
            totalTimeHours < GOOD_THRESHOLD_HOURS -> "Good"
            totalTimeHours < MODERATE_THRESHOLD_HOURS -> "Moderate"
            totalTimeHours < HIGH_THRESHOLD_HOURS -> "High"
            else -> "Very High"
        }
    }

    private fun buildTopAppsList(
        stats: Map<String, android.app.usage.UsageStats>
    ): List<TopApp> {
        return stats.values
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(TOP_APPS_LIMIT)
            .map { usageStats ->
                val appName = getApplicationName(usageStats.packageName)
                val appCategory = getApplicationCategory(usageStats.packageName)
                TopApp(
                    name = appName,
                    usage = formatDuration(usageStats.totalTimeInForeground),
                    category = appCategory,
                    packageName = usageStats.packageName
                )
            }
    }

    private fun getApplicationName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            Log.w("HomeViewModel", "Package not found: $packageName")
            packageName
        } catch (e: SecurityException) {
            Log.w("HomeViewModel", "Security exception getting app info for $packageName")
            packageName
        }
    }

    private fun getApplicationCategory(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            when (appInfo.category) {
                android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> "Audio"
                android.content.pm.ApplicationInfo.CATEGORY_GAME -> "Games"
                android.content.pm.ApplicationInfo.CATEGORY_IMAGE -> "Image"
                android.content.pm.ApplicationInfo.CATEGORY_MAPS -> "Maps"
                android.content.pm.ApplicationInfo.CATEGORY_NEWS -> "News"
                android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> "Video"
                else -> "App"
            }
        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
            Log.w("HomeViewModel", "Package not found: $packageName")
            "App"
        } catch (e: SecurityException) {
            Log.w("HomeViewModel", "Security exception getting category for $packageName")
            "App"
        }
    }

    private fun buildControlledAppsList(
        stats: Map<String, android.app.usage.UsageStats>
    ): List<ControlledAppWithUsage> {
        return rawControlledApps.map { app ->
            ControlledAppWithUsage(
                app = app,
                usage = formatDuration(stats[app.packageName]?.totalTimeInForeground ?: 0L),
                limit = if (app.dailyLimit > 0) {
                    formatDuration(TimeUnit.MINUTES.toMillis(app.dailyLimit.toLong()))
                } else {
                    "No limit"
                }
            )
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
