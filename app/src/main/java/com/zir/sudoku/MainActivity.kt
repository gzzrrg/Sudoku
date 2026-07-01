package com.zir.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.zir.sudoku.ui.navigation.SudokuNavHost
import com.zir.sudoku.ui.screen.splash.SplashScreen
import com.zir.sudoku.ui.theme.SudokuColorPalette
import com.zir.sudoku.ui.theme.SudokuTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SudokuApplication).appContainer

        setContent {
            val settings by appContainer.repository.getSettings()
                .collectAsState(initial = null)

            // Cache theme mode across rotations to prevent flash of wrong theme
            var cachedThemeMode by rememberSaveable { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                if (cachedThemeMode == null) {
                    cachedThemeMode = appContainer.repository.getSettings().first().themeMode
                }
            }

            val themeMode = settings?.themeMode ?: cachedThemeMode ?: "system"
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            val isWhiteTheme = themeMode == "white"

            // Determine splash palette from theme mode
            val splashPalette = when (themeMode) {
                "dark" -> SudokuColorPalette.Dark
                "white" -> SudokuColorPalette.White
                "light" -> SudokuColorPalette.Light
                else -> if (isSystemInDarkTheme()) SudokuColorPalette.Dark else SudokuColorPalette.Light
            }

            var showSplash by rememberSaveable { mutableStateOf(true) }
            val navController = rememberNavController()

            Box(modifier = Modifier.fillMaxSize()) {
                // NavHost always in composition — preserves back stack across rotations
                SudokuTheme(darkTheme = isDarkTheme, whiteTheme = isWhiteTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SudokuNavHost(
                            navController = navController,
                            repository = appContainer.repository
                        )
                    }
                }

                // Splash overlays on top, removed after first launch
                if (showSplash) {
                    SplashScreen(palette = splashPalette) {
                        showSplash = false
                    }
                }
            }
        }
    }
}
