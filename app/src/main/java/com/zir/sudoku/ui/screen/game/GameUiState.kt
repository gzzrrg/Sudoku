/**
 * ## GameUiState — 游戏页 UI 状态定义
 *
 * **职责：**
 * - 使用 Kotlin `sealed interface` 精确定义游戏全生命周期的 6 种状态
 * - 每种状态携带对应阶段所需的全部数据，UI 根据状态类型进行分支渲染
 *
 * **状态流转：**
 * ```
 * Loading → Active → Completed
 *                  → Failed（错误达 3 次）
 *                  → Paused（暂停）
 * Active ↔ Paused（暂停/恢复）
 * any → Error（异常）
 * ```
 *
 * **状态说明：**
 * - `Loading`：谜题生成中（API 请求或本地生成）
 * - `Active`：游戏进行中，包含棋盘状态、难度、计时、提示次数、错误计数
 * - `Paused`：游戏暂停，棋盘数据保留但计时停止
 * - `Completed`：通关成功，包含用时和最佳纪录标识
 * - `Failed`：游戏失败（错误达到 maxErrors 上限），包含用时
 * - `Error`：异常状态，携带错误消息
 */
package com.zir.sudoku.ui.screen.game
import com.zir.sudoku.domain.model.BoardState
sealed interface GameUiState {
    data object Loading : GameUiState
    data class Active(
        val boardState: BoardState,
        val difficulty: String,
        val elapsedSeconds: Int,
        val hintCount: Int,
        val errorCount: Int,
        val maxErrors: Int = 3,
        val canUndo: Boolean = true,
        val canRedo: Boolean = false
    ) : GameUiState
    data class Paused(
        val boardState: BoardState,
        val elapsedSeconds: Int
    ) : GameUiState
    data class Completed(
        val timeSeconds: Int,
        val isNewBest: Boolean,
        val difficulty: String
    ) : GameUiState
    data class Failed(
        val timeSeconds: Int,
        val difficulty: String,
        val errorCount: Int = 3,
        val maxErrors: Int = 3
    ) : GameUiState
    data class Error(val message: String) : GameUiState
}