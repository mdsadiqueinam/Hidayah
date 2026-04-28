package io.github.mdsadiqueinam.hidayah.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppItem
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.data.ControlledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppSelectionUiState(
    val searchQuery: String = "",
    val filteredApps: List<AppItem> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val onSearchQueryChange: (String) -> Unit = {},
    val onAppToggle: (String) -> Unit = {},
    val onSave: (Set<String>) -> Unit = {}
)

@HiltViewModel
class AppSelectionViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    private var allApps: List<AppItem> = emptyList()

    private val _uiState = MutableStateFlow(
        AppSelectionUiState(
            onSearchQueryChange = ::onSearchQueryChange,
            onAppToggle = ::onAppToggle,
            onSave = ::onSave
        )
    )
    val uiState: StateFlow<AppSelectionUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            allApps = repository.getInstalledApps()
            val savedPackages = repository.getControlledPackageNames().toSet()
            _uiState.update { it.copy(selectedPackages = savedPackages) }
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

    private fun onSave(selectedPackages: Set<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val appsToSave = allApps.filter { selectedPackages.contains(it.packageName) }
                .map { ControlledApp(it.packageName, it.appName) }
            repository.saveControlledApps(appsToSave)
        }
    }
}
