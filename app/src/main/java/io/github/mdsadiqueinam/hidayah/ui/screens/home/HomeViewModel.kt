package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.mdsadiqueinam.hidayah.data.AppDatabaseRepository
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import io.github.mdsadiqueinam.hidayah.util.PermissionUtils
import io.github.mdsadiqueinam.hidayah.util.TimeUtils
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DEFAULT_SCREEN_TIME_LIMIT_HOURS = 5L

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
    private val dbRepository: AppDatabaseRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val MILLIS_PER_HOUR = 1000 * 60 * 60
        private const val REFRESH_INTERVAL_MS = 60000L
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
        dbRepository.getShieldConfig()
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
            dbRepository.updateShieldConfig(config)
        }
    }

    private fun checkPermission() {
        val granted = PermissionUtils.hasUsageStatsPermission(context)
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
            dbRepository.getControlledApps().collect { apps ->
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
        val totalTimeMs = HomeStatsHelper.calculateTotalScreenTime(stats)
        val controlledTimeMs =
            HomeStatsHelper.calculateControlledScreenTime(stats, controlledPackageNames)

        // Determine status
        val totalTimeHours = totalTimeMs.toDouble() / MILLIS_PER_HOUR
        val status = HomeStatsHelper.calculateScreenTimeStatus(totalTimeHours)

        // Build lists
        val topApps = HomeStatsHelper.buildTopAppsList(context, stats)
        val controlledAppsWithUsage =
            HomeStatsHelper.buildControlledAppsList(rawControlledApps, stats)

        // Update UI state
        _uiState.update { state ->
            state.copy(
                totalScreenTime = TimeUtils.formatDuration(totalTimeMs),
                controlledScreenTime = TimeUtils.formatDuration(controlledTimeMs),
                totalScreenTimeMs = totalTimeMs,
                controlledScreenTimeMs = controlledTimeMs,
                screenTimeStatus = status,
                topApps = topApps,
                controlledApps = controlledAppsWithUsage
            )
        }
    }
}
