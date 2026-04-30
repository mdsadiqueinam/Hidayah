package io.github.mdsadiqueinam.hidayah

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.mdsadiqueinam.hidayah.ui.screens.shield.ShieldScreen
import io.github.mdsadiqueinam.hidayah.ui.theme.HidayahTheme

@AndroidEntryPoint
class ShieldActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // checking packageName exists
        intent.getStringExtra("packageName") ?: run {
            finish()
            return
        }

        showShield()
    }

    private fun showShield() {
        setContent {
            HidayahTheme {
                ShieldScreen(
                    onClose = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onOpen = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showShield()
    }
}
