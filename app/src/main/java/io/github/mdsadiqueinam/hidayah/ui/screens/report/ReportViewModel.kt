package io.github.mdsadiqueinam.hidayah.ui.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.mdsadiqueinam.hidayah.data.AppRepository
import io.github.mdsadiqueinam.hidayah.util.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val interventionsCount: Int = 0,
    val reclaimedTime: String = "0 hours",
    val efficiencyChange: String = "0%",
    val dailyAverage: String = "0h 0m",
    val dailyAverageProgress: Float = 0f,
    val weeklyTrend: List<Long> = emptyList(),
    val categoryBreakdown: Map<String, Float> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch(Dispatchers.IO) {
            val weeklyStats = repository.getWeeklyUsageStats()
            val categoryStats = repository.getCategoryUsageStats()
            val interventions = repository.getInterventionsCount()

            weeklyStats.sum()
            val reclaimedMinutes = interventions * 5L // Assuming 5 mins saved per intervention
            val reclaimedHours = reclaimedMinutes.toFloat() / 60f

            val dailyAvg = if (weeklyStats.isNotEmpty()) weeklyStats.average().toLong() else 0L
            val lastWeekAvg = 2.5 * 3600000 // Mock last week average
            val efficiency = ((lastWeekAvg - dailyAvg) / lastWeekAvg * 100).toInt()

            _uiState.update {
                it.copy(
                    interventionsCount = interventions,
                    reclaimedTime = String.format("%.1f hours", reclaimedHours),
                    efficiencyChange = if (efficiency >= 0) "$efficiency% less" else "${-efficiency}% more",
                    dailyAverage = TimeUtils.formatDuration(dailyAvg),
                    dailyAverageProgress = (dailyAvg.toFloat() / (4 * 3600000f)).coerceIn(
                        0f,
                        1f
                    ), // Max 4h
                    weeklyTrend = weeklyStats,
                    categoryBreakdown = categoryStats,
                    isLoading = false
                )
            }
        }
    }
}
