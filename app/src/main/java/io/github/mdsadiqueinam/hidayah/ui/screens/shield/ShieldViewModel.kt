package io.github.mdsadiqueinam.hidayah.ui.screens.shield

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppDatabaseRepository
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
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
    val usageTime: String = "0m",
    val attempts: Int = 0
)

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

        // Fetch Controlled App and Usage
        viewModelScope.launch {
            val app = dbRepository.getControlledApp(packageName)
            val stats = repository.getDailyUsageStats()
            val usageMs = stats[packageName]?.totalTimeInForeground ?: 0L
            _uiState.update {
                it.copy(
                    controlledApp = app,
                    usageTime = formatDuration(usageMs)
                )
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
