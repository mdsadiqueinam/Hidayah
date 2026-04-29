package io.github.mdsadiqueinam.hidayah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.mdsadiqueinam.hidayah.ui.screens.shield.ShieldScreen
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShieldActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val packageName = intent.getStringExtra("packageName") ?: finish().run { return }

        setContent {
            HidayahTheme {
                ShieldScreen(
                    onClose = {
                        // Return to home/launcher
                        finish()
                    },
                    onOpen = {
                        // Close shield and let user use the app
                        finish()
                    }
                )
            }
        }
    }
}
