package dev.berkchy.bgmaker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.berkchy.bgmaker.ui.configure.ConfigureScreen
import dev.berkchy.bgmaker.ui.picker.PickerScreen
import dev.berkchy.bgmaker.ui.result.ProgressScreen
import dev.berkchy.bgmaker.ui.result.ResultScreen
import dev.berkchy.bgmaker.ui.settings.SettingsScreen

@Composable
fun AppNav(viewModel: MainViewModel) {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "picker") {
        composable("picker") {
            PickerScreen(
                viewModel = viewModel,
                onConfigure = { navController.navigate("configure") },
                onSettings = { navController.navigate("settings") },
            )
        }
        composable("configure") {
            ConfigureScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onConvert = { navController.navigate("progress") },
            )
        }
        composable("progress") {
            ProgressScreen(
                viewModel = viewModel,
                onDone = {
                    navController.navigate("result") {
                        popUpTo("configure") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable("result") {
            ResultScreen(
                viewModel = viewModel,
                onAgain = {
                    viewModel.startOver()
                    navController.navigate("picker") {
                        popUpTo("picker") { inclusive = true }
                    }
                },
                onSettings = { navController.navigate("settings") },
            )
        }
        composable("settings") {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
