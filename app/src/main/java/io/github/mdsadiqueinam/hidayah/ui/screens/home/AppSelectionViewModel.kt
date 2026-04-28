package io.github.mdsadiqueinam.hidayah.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.mdsadiqueinam.hidayah.data.AppItem
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppSelectionUiState(
    val searchQuery: String = "",
    val filteredApps: List<AppItem> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val onSearchQueryChange: (String) -> Unit = {},
    val onAppToggle: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDismiss: () -> Unit = {}
)

class AppSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private var allApps: List<AppItem> = emptyList()

    private val _uiState = MutableStateFlow(
        AppSelectionUiState(
            onSearchQueryChange = ::onSearchQueryChange,
            onAppToggle = ::onAppToggle
        )
    )
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            allApps = repository.getInstalledApps()
            filterApps()
        }
    }

    private fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterApps()
    }

    private fun filterApps() {
        val query = _uiState.value.searchQuery.lowercase()
        val filtered = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.appName.lowercase().contains(query) }
        }
        _uiState.update { it.copy(filteredApps = filtered) }
    }

    private fun onAppToggle(packageName: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedPackages.contains(packageName)) {
                state.selectedPackages - packageName
            } else {
                state.selectedPackages + packageName
            }
            state.copy(selectedPackages = newSelected)
        }
    }
}
