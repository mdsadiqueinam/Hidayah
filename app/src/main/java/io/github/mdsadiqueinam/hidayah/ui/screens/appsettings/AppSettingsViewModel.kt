package io.github.mdsadiqueinam.hidayah.ui.screens.appsettings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSettingsUiState(
    val app: ControlledApp? = null,
    val onDailyLimitChange: (Int?) -> Unit = {},
    val onOpenDelayChange: (Int?) -> Unit = {},
    val onSessionLimitChange: (Int?) -> Unit = {},
    val onHardLockToggle: (Boolean) -> Unit = {},
    val onRemoveApp: () -> Unit = {}
)

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AppRepository
) : ViewModel() {
    val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    init {
        loadAppData()
    }

    private fun loadAppData() {
        viewModelScope.launch {
            val app = repository.getControlledApp(packageName)
            _uiState.update { state ->
                state.copy(
                    app = app,
                    onDailyLimitChange = { limit -> updateApp { it.copy(dailyLimit = limit ?: 0) } },
                    onOpenDelayChange = { delay -> updateApp { it.copy(openDelay = delay ?: 0) } },
                    onSessionLimitChange = { limit -> updateApp { it.copy(sessionLimit = limit ?: 0) } },
                    onHardLockToggle = { locked -> updateApp { it.copy(isHardLocked = locked) } },
                    onRemoveApp = { removeApp() }
                )
            }
        }
    }

    private fun removeApp() {
        viewModelScope.launch {
            repository.removeControlledApp(packageName)
        }
    }

    private fun updateApp(update: (ControlledApp) -> ControlledApp) {
        val currentApp = _uiState.value.app ?: return
        val updatedApp = update(currentApp)
        _uiState.update { it.copy(app = updatedApp) }
        viewModelScope.launch {
            repository.updateControlledApp(updatedApp)
        }
    }
}
