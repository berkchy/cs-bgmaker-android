package dev.berkchy.bgmaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.berkchy.bgmaker.ui.AppNav
import dev.berkchy.bgmaker.ui.MainViewModel
import dev.berkchy.bgmaker.theme.BgmakerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            BgmakerTheme(
                themeMode = viewModel.settings.themeMode,
                dynamicColor = viewModel.settings.dynamicColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNav(viewModel)
                }
            }
        }
    }
}
