package io.github.mdsadiqueinam.hidayah.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToAppSettings: (String) -> Unit = {}
) {
    ControlledAppSection(uiState, onNavigateToAppSettings)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HidayahTheme {
        HomeScreen(
            uiState = HomeUiState(
                title = "Controlled Apps",
                onAddClick = {}
            )
        )
    }
}
