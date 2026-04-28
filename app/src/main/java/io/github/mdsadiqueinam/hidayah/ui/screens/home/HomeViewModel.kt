package io.github.mdsadiqueinam.hidayah.ui.screens.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val title: String = "Controlled Apps",
    val onAddClick: () -> Unit = {}
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            onAddClick = {
                // Handle add click
            }
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
