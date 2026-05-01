package io.github.mdsadiqueinam.hidayah.ui.screens.shield

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppDatabaseRepository
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import io.github.mdsadiqueinam.hidayah.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ShieldUiState(
    val controlledApp: ControlledApp? = null,
    val shieldConfig: ShieldConfig = ShieldConfig(),
    val usageTimeMs: Long = 0L,
    val attempts: Int = 0,
    val countdownSeconds: Int = 60,
    val isCountdownActive: Boolean = false,
    val isCountdownFinished: Boolean = false
) {
    val usageTime: String get() = DateTimeUtils.formatDuration(usageTimeMs)
}

@HiltViewModel
class ShieldViewModel @Inject constructor(
    private val repository: AppRepository,
    private val dbRepository: AppDatabaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(ShieldUiState())
    val uiState: StateFlow<ShieldUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        // Observe Shield Config
        dbRepository.getShieldConfig()
            .onEach { config ->
                if (config != null) {
                    _uiState.update { it.copy(shieldConfig = config) }
                }
            }
            .launchIn(viewModelScope)

        // Observe Controlled App
        dbRepository.getControlledAppFlow(packageName)
            .onEach { app ->
                _uiState.update { it.copy(controlledApp = app, attempts = app?.attempts ?: 0) }
            }
            .launchIn(viewModelScope)

        // Fetch Usage
        viewModelScope.launch {
            val stats = repository.getDailyUsageStats()
            val usageMs = stats[packageName]?.totalTimeInForeground ?: 0L
            _uiState.update {
                it.copy(usageTimeMs = usageMs)
            }
        }
    }

    fun startCountdown() {
        _uiState.update { it.copy(isCountdownActive = true) }
    }

    fun tickCountdown() {
        _uiState.update {
            if (it.countdownSeconds > 1) {
                it.copy(countdownSeconds = it.countdownSeconds - 1)
            } else {
                it.copy(countdownSeconds = 0, isCountdownActive = false, isCountdownFinished = true)
            }
        }
    }

    fun incrementAttempts() {
        viewModelScope.launch {
            uiState.value.controlledApp?.let { app ->
                dbRepository.updateControlledApp(app.copy(attempts = app.attempts + 1))
            }
        }
    }
}
