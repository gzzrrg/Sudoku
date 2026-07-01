/**
 * ## HomeViewModel — 首页业务逻辑
 *
 * **职责：**
 * - 管理首页 UI 状态（[HomeUiState]）
 * - `checkActiveSession()`：查询 Room 中是否存在活跃 [GameSession]（id=1）
 * - `deleteActiveSession()`：删除当前活跃存档
 *
 * **初始化流程：**
 * ```
 * init → checkActiveSession()
 *      → repository.getActiveSessionOnce()
 *      → session != null → Ready(hasActiveSession = true)
 *      → session == null → Ready(hasActiveSession = false)
 * ```
 *
 * **Factory：**
 * 通过 `HomeViewModel.Factory(repository)` 创建，用于 `viewModel()` 函数注入
 *
 * @see SudokuRepository
 * @see GameSessionDao
 */
package com.zir.sudoku.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zir.sudoku.data.repository.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkActiveSession()
    }

    fun checkActiveSession() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading
                val session = repository.getActiveSessionOnce()
                val bestTime = repository.getOverallBestTime().first()
                _uiState.value = HomeUiState.Ready(
                    hasActiveSession = session != null,
                    bestTimeSeconds = bestTime
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun deleteActiveSession() {
        viewModelScope.launch {
            repository.deleteActiveSession()
            checkActiveSession()
        }
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
