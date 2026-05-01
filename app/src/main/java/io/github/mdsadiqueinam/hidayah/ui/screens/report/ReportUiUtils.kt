package io.github.mdsadiqueinam.hidayah.ui.screens.report

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ReportUiUtils {
    /**
     * Returns the color associated with a category index for charts and legends.
     */
    @Composable
    fun getCategoryColor(index: Int): Color {
        return when (index) {
            0 -> MaterialTheme.colorScheme.primary
            1 -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.tertiaryFixedDim
        }
    }

    /**
     * Returns a list of colors for the category donut chart.
     */
    @Composable
    fun getCategoryColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiaryFixedDim
        )
    }
}
