package com.zir.sudoku.ui.screen.settings

data class SettingsUiState(
    val themeMode: String = "system",
    val highlightConflicts: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val scoreAnimationEnabled: Boolean = true,
    val errorLimitEnabled: Boolean = true
)
