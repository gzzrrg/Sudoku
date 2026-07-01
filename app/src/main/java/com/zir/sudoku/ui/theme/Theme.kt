/**
 * ## SudokuTheme — Material 3 主题包装器
 *
 * **职责：**
 * - 封装 Material 3 主题配置，是整个应用 UI 树的顶层 Composable
 * - 根据 `darkTheme` 参数和系统设置切换浅色/深色配色方案
 * - 通过 `SideEffect` 同步状态栏外观（浅色状态栏图标 vs 深色状态栏图标）
 *
 * **使用方式：**
 * ```kotlin
 * SudokuTheme(darkTheme = isDarkTheme) {
 *     // 应用内容
 * }
 * ```
 *
 * **动态颜色：**
 * - `dynamicColor` 默认 false，使用自定义配色而非系统动态颜色
 * - 可开启以使用 Android 12+ Material You 动态取色
 *
 * @see Color
 */
package com.zir.sudoku.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    outline = LightOutline
)

private val WhiteColorScheme = lightColorScheme(
    primary = WhitePrimary,
    onPrimary = WhiteOnPrimary,
    primaryContainer = WhitePrimaryContainer,
    onPrimaryContainer = WhiteOnPrimaryContainer,
    secondary = WhiteSecondary,
    onSecondary = WhiteOnSecondary,
    secondaryContainer = WhiteSecondaryContainer,
    onSecondaryContainer = WhiteOnSecondaryContainer,
    tertiary = WhiteTertiary,
    onTertiary = WhiteOnTertiary,
    background = WhiteBackground,
    onBackground = WhiteOnBackground,
    surface = WhiteSurface,
    onSurface = WhiteOnSurface,
    surfaceVariant = WhiteSurfaceVariant,
    onSurfaceVariant = WhiteOnSurfaceVariant,
    error = WhiteError,
    onError = WhiteOnError,
    outline = WhiteOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    outline = DarkOutline
)

val LocalSudokuPalette = compositionLocalOf { SudokuColorPalette.Light }

@Composable
fun SudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    whiteTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        whiteTheme -> WhiteColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val sudokuPalette = when {
        whiteTheme -> SudokuColorPalette.White
        darkTheme -> SudokuColorPalette.Dark
        else -> SudokuColorPalette.Light
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(
                LocalSudokuPalette provides sudokuPalette
            ) {
                content()
            }
        }
    )
}
