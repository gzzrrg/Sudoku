/**
 * ## SettingsViewModel — 设置页业务逻辑
 *
 * **职责：**
 * - 管理 [SettingsUiState]，响应式监听 DataStore 变更
 * - 提供设置项更新方法：主题模式、冲突提示、音效
 * - `clearAllData()`：清除 Room 数据库全部记录 + DataStore 全部偏好
 *
 * **数据流：**
 * ```
 * DataStore → repository.getSettings() → Flow<UserSettings>
 *   → collect → SettingsUiState → StateFlow → UI
 * ```
 *
 * **写入流程：**
 * ```
 * UI 操作 → setThemeMode(mode) → viewModelScope.launch → repository.setThemeMode(mode)
 *   → DataStore.edit → Flow<UserSettings> 自动发射新值 → UI 重组
 * ```
 *
 * @see SettingsDataStore
 * @see SudokuRepository
 */
package com.zir.sudoku.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zir.sudoku.data.repository.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSettings().collect { settings ->
                _uiState.value = SettingsUiState(
                    themeMode = settings.themeMode,
                    highlightConflicts = settings.highlightConflicts,
                    soundEnabled = settings.soundEnabled,
                    vibrationEnabled = settings.vibrationEnabled,
                    scoreAnimationEnabled = settings.scoreAnimationEnabled,
                    errorLimitEnabled = settings.errorLimitEnabled
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setHighlightConflicts(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHighlightConflicts(enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
        }
    }

    fun setScoreAnimationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setScoreAnimationEnabled(enabled)
        }
    }

    fun setErrorLimitEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setErrorLimitEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
