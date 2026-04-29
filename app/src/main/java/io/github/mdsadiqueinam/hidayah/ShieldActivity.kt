package io.github.mdsadiqueinam.hidayah

import android.content.Intent
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
        
        val packageName = intent.getStringExtra("packageName") ?: run {
            finish()
            return
        }

        setContent {
            HidayahTheme {
                ShieldScreen(
                    onClose = {
                        // Effectively "closes" the target app by taking user to Home screen
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onOpen = {
                        // Dismiss the shield and allow the user to see the app behind it
                        finish()
                    }
                )
            }
        }
    }
}
