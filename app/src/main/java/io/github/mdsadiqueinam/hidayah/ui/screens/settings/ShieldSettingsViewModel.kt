package io.github.mdsadiqueinam.hidayah.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ShieldConfig
import io.github.mdsadiqueinam.hidayah.data.ShieldImage
import io.github.mdsadiqueinam.hidayah.data.defaultShieldImageResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShieldSettingsUiState(
    val config: ShieldConfig = ShieldConfig(),
    val selectedImage: ShieldImage = ShieldImage.Resource(defaultShieldImageResources.first()),
    val onHeadlineChange: (String) -> Unit = {},
    val onSubHeadlineChange: (String) -> Unit = {},
    val onImageSelected: (ShieldImage) -> Unit = {},
    val onResetToDefaults: () -> Unit = {},
    val onSave: () -> Unit = {}
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
                    _uiState.update { state ->
                        state.copy(
                            config = config,
                            selectedImage = parseImagePath(config.imagePath)
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

        _uiState.update { state ->
            state.copy(
                onHeadlineChange = { updateHeadline(it) },
                onSubHeadlineChange = { updateSubHeadline(it) },
                onImageSelected = { updateSelectedImage(it) },
                onResetToDefaults = { resetToDefaults() },
                onSave = { saveConfig() }
            )
        }
    }

    private fun parseImagePath(path: String?): ShieldImage {
        if (path.isNullOrBlank()) return ShieldImage.Resource(defaultShieldImageResources.first())

        return if (path.startsWith("res:")) {
            val resId = path.substringAfter("res:").toIntOrNull()
            if (resId != null) ShieldImage.Resource(resId)
            else ShieldImage.Resource(defaultShieldImageResources.first())
        } else {
            ShieldImage.UriImage(path)
        }
    }

    private fun updateHeadline(headline: String) {
        _uiState.update { it.copy(config = it.config.copy(headline = headline)) }
    }

    private fun updateSubHeadline(subHeadline: String) {
        _uiState.update { it.copy(config = it.config.copy(subHeadline = subHeadline)) }
    }

    private fun updateSelectedImage(image: ShieldImage) {
        val path = when (image) {
            is ShieldImage.Resource -> "res:${image.resId}"
            is ShieldImage.UriImage -> image.uri
        }
        _uiState.update {
            it.copy(
                selectedImage = image,
                config = it.config.copy(imagePath = path)
            )
        }
    }

    private fun resetToDefaults() {
        val defaultConfig = ShieldConfig()
        _uiState.update {
            it.copy(
                config = defaultConfig,
                selectedImage = parseImagePath(defaultConfig.imagePath)
            )
        }
    }

    private fun saveConfig() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShieldConfig(_uiState.value.config)
        }
    }
}
