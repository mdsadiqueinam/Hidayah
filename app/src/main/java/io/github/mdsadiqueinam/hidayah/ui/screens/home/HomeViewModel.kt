package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val title: String = "Controlled Apps",
    val controlledApps: List<ControlledApp> = emptyList(),
    val onAddClick: () -> Unit = {}
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
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
