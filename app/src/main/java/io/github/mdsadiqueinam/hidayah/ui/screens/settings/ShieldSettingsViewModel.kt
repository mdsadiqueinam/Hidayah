package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShieldSettingsUiState(
    val config: ShieldConfig = ShieldConfig(),
    val onHeadlineChange: (String) -> Unit = {},
    val onSubHeadlineChange: (String) -> Unit = {},
    val onImageSelected: (String) -> Unit = {},
    val onUseDefault: () -> Unit = {}
)

@HiltViewModel
class ShieldSettingsViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShieldSettingsUiState())
    val uiState: StateFlow<ShieldSettingsUiState> = _uiState.asStateFlow()

    init {
        repository.getShieldConfig()
            .onEach { config ->
                if (config != null) {
                    _uiState.update { it.copy(config = config) }
                }
            }
            .launchIn(viewModelScope)

        _uiState.update { state ->
            state.copy(
                onHeadlineChange = { updateHeadline(it) },
                onSubHeadlineChange = { updateSubHeadline(it) },
                onImageSelected = { updateImagePath(it) },
                onUseDefault = { useDefault() }
            )
        }
    }

    private fun updateHeadline(headline: String) {
        val newConfig = _uiState.value.config.copy(headline = headline)
        _uiState.update { it.copy(config = newConfig) }
        saveConfig(newConfig)
    }

    private fun updateSubHeadline(subHeadline: String) {
        val newConfig = _uiState.value.config.copy(subHeadline = subHeadline)
        _uiState.update { it.copy(config = newConfig) }
        saveConfig(newConfig)
    }

    private fun updateImagePath(path: String) {
        val newConfig = _uiState.value.config.copy(imagePath = path)
        _uiState.update { it.copy(config = newConfig) }
        saveConfig(newConfig)
    }

    private fun useDefault() {
        val defaultConfig = ShieldConfig()
        _uiState.update { it.copy(config = defaultConfig) }
        saveConfig(defaultConfig)
    }

    private fun saveConfig(config: ShieldConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShieldConfig(config)
        }
    }
}
