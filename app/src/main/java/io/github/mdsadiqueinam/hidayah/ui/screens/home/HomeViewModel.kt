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

enum class ScreenTimeCategory(val label: String) {
    TOTAL("Total"),
    CONTROLLED_APP("Controlled App")
}

data class HomeUiState(
    val title: String = "Controlled Apps",
    val controlledApps: List<ControlledApp> = emptyList(),
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
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
                _uiState.update { it.copy(controlledApps = apps) }
                refreshUsageStats()
            }
        }
    }

    private fun refreshUsageStats() {
        if (!_uiState.value.isUsageStatsPermissionGranted) return

        val stats = repository.getDailyUsageStats()
        val controlledPackageNames = _uiState.value.controlledApps.map { it.packageName }.toSet()
        val category = _uiState.value.selectedScreenTimeCategory

        val filteredStats = if (category == ScreenTimeCategory.TOTAL) {
            stats
        } else {
            stats.filter { controlledPackageNames.contains(it.key) }
        }

        val totalTimeMs = filteredStats.values.sumOf { it.totalTimeInForeground }
        
        // Calculate percentage of day passed
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayStart = calendar.timeInMillis
        val dayPassedMs = now - dayStart
        
        val percentage = if (dayPassedMs > 0) (totalTimeMs * 100 / dayPassedMs).toInt() else 0
        
        val status = when {
            percentage < 5 -> "Excellent"
            percentage < 10 -> "Good"
            percentage < 20 -> "Moderate"
            else -> "Bad"
        }

        // Get app names for top apps
        val appNameMap = repository.getInstalledApps().associate { it.packageName to it.appName }
        
        val topApps = filteredStats.values
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(3)
            .map {
                TopApp(
                    name = appNameMap[it.packageName] ?: it.packageName,
                    usage = formatDuration(it.totalTimeInForeground)
                )
            }

        _uiState.update { state ->
            state.copy(
                totalScreenTime = formatDuration(totalTimeMs),
                screenTimePercentage = "$percentage% of the day",
                screenTimeStatus = status,
                topApps = topApps
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
