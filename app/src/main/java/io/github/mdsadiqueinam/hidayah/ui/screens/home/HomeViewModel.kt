package io.github.mdsadiqueinam.hidayah.ui.screens.home

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
    val totalScreenTime: String = "2h 45m",
    val screenTimePercentage: String = "11% of the day",
    val screenTimeStatus: String = "Good",
    val topApps: List<TopApp> = listOf(
        TopApp("Brave", "1h 37m"),
        TopApp("Untap", "19m"),
        TopApp("Al Quran", "14m")
    ),
    val onAddClick: () -> Unit = {},
    val onProtectionToggle: (Boolean) -> Unit = {},
    val onFocusModeToggle: (Boolean) -> Unit = {},
    val onPauseDurationChange: (String?) -> Unit = {},
    val onScreenTimeCategoryChange: (ScreenTimeCategory) -> Unit = {}
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
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
                }
            )
        }
        observeControlledApps()
    }

    private fun observeControlledApps() {
        viewModelScope.launch {
            repository.getControlledApps().collect { apps ->
                _uiState.update { it.copy(controlledApps = apps) }
            }
        }
    }
}
