package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class TopApp(
    val name: String,
    val usage: String
)

data class ControlledAppWithUsage(
    val app: ControlledApp,
    val usage: String
)

enum class ScreenTimeCategory(val label: String) {
    TOTAL("Total"),
    CONTROLLED_APP("Controlled App")
}

data class HomeUiState(
    val title: String = "Controlled Apps",
    val controlledApps: List<ControlledAppWithUsage> = emptyList(),
    val isProtectionActive: Boolean = true,
    val isFocusModeActive: Boolean = false,
    val selectedPauseDuration: String? = null,
    val selectedScreenTimeCategory: ScreenTimeCategory = ScreenTimeCategory.TOTAL,
    val totalScreenTime: String = "0h 0m",
    val screenTimePercentage: String = "0% of the day",
    val screenTimeStatus: String = "Excellent",
    val topApps: List<TopApp> = emptyList(),
    val isUsageStatsPermissionGranted: Boolean = true,
    val onAddClick: () -> Unit = {},
    val onProtectionToggle: (Boolean) -> Unit = {},
    val onFocusModeToggle: (Boolean) -> Unit = {},
    val onPauseDurationChange: (String?) -> Unit = {},
    val onScreenTimeCategoryChange: (ScreenTimeCategory) -> Unit = {}
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var rawControlledApps: List<ControlledApp> = emptyList()

    init {
        _uiState.update { state ->
            state.copy(
                onProtectionToggle = { active -> 
                    _uiState.update { it.copy(isProtectionActive = active) }
                },
                onFocusModeToggle = { active ->
                    _uiState.update { it.copy(isFocusModeActive = active) }
                },
                onPauseDurationChange = { duration ->
                    _uiState.update { it.copy(selectedPauseDuration = duration) }
                },
                onScreenTimeCategoryChange = { category ->
                    _uiState.update { it.copy(selectedScreenTimeCategory = category) }
                    refreshUsageStats()
                }
            )
        }
        observeControlledApps()
        checkPermission()
        startStatsRefresh()
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
                delay(60000) // Refresh every minute
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
        val category = _uiState.value.selectedScreenTimeCategory

        val filteredStats = if (category == ScreenTimeCategory.TOTAL) {
            stats
        } else {
            stats.filter { controlledPackageNames.contains(it.key) }
        }

        val totalTimeMs = filteredStats.values.sumOf { it.totalTimeInForeground }
        val totalTimeHours = totalTimeMs.toDouble() / (1000 * 60 * 60)
        
        val status = if (category == ScreenTimeCategory.TOTAL) {
            when {
                totalTimeHours < 2 -> "Excellence"
                totalTimeHours < 3 -> "Good"
                totalTimeHours < 4 -> "Moderate"
                totalTimeHours < 5 -> "High"
                else -> "Very High"
            }
        } else {
            when {
                totalTimeHours < 1 -> "Excellence"
                totalTimeHours < 2 -> "Good"
                totalTimeHours < 3 -> "Moderate"
                totalTimeHours < 4 -> "High"
                else -> "Very High"
            }
        }

        // Calculate percentage of day passed for display info (optional, keeping it for now if UI needs it)
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayStart = calendar.timeInMillis
        val dayPassedMs = now - dayStart
        
        val percentage = if (dayPassedMs > 0) (totalTimeMs * 100 / dayPassedMs).toInt() else 0

        val topApps = filteredStats.values
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(3)
            .map {
                val appName = try {
                    val packageManager = context.packageManager
                    val appInfo = packageManager.getApplicationInfo(it.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    it.packageName
                }
                TopApp(
                    name = appName,
                    usage = formatDuration(it.totalTimeInForeground)
                )
            }

        val controlledAppsWithUsage = rawControlledApps.map { app ->
            ControlledAppWithUsage(
                app = app,
                usage = formatDuration(stats[app.packageName]?.totalTimeInForeground ?: 0L)
            )
        }

        _uiState.update { state ->
            state.copy(
                totalScreenTime = formatDuration(totalTimeMs),
                screenTimePercentage = "$percentage% of the day",
                screenTimeStatus = status,
                topApps = topApps,
                controlledApps = controlledAppsWithUsage
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
