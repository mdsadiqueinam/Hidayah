package io.github.mdsadiqueinam.hidayah

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.mdsadiqueinam.hidayah.service.AppTrackerService
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the background tracking service
        android.util.Log.i("MainActivity", "Starting AppTrackerService as foreground")
        ContextCompat.startForegroundService(this, Intent(this, AppTrackerService::class.java))

        setContent {
            HidayahTheme {
                MainScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
