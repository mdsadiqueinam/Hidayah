package io.github.mdsadiqueinam.hidayah.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun <T> FlowButton(
    options: List<T>,
    labels: List<String>,
    selectedOption: T?,
    onOptionSelected: (T?) -> Unit,
    maxItemsInEachRow: Int,
    modifier: Modifier = Modifier,
    isToggleable: Boolean = false
) {
    val chunks = options.zip(labels).chunked(maxItemsInEachRow)
    
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        chunks.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { (option, label) ->
                    val isSelected = option == selectedOption
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = isSelected,
                        onClick = { 
                            if (isToggleable && isSelected) {
                                onOptionSelected(null)
                            } else {
                                onOptionSelected(option)
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            selectedBorderWidth = 1.dp,
                            borderColor = MaterialTheme.colorScheme.outline
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Add empty spacers to maintain the grid structure if the row isn't full
                if (rowItems.size < maxItemsInEachRow) {
                    repeat(maxItemsInEachRow - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun FlowButtonPreview() {
    val options = listOf(1, 2, 3, 4, 5)
    val labels = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")

    Surface {
        Box(modifier = Modifier.padding(16.dp)) {
            FlowButton(
                options = options,
                labels = labels,
                selectedOption = 1,
                onOptionSelected = {},
                maxItemsInEachRow = 3,
                isToggleable = true
            )
        }
    }
}
