/**
 * ## HomeUiState — 首页 UI 状态定义
 *
 * **职责：**
 * - 使用 Kotlin `sealed interface` 精确定义首页的三种可能状态
 * - 配合 StateFlow 驱动 Compose UI 重组
 *
 * **状态说明：**
 * - `Loading`：初始加载中（查询是否存在活跃存档）
 * - `Ready(hasActiveSession)`：就绪态，根据 hasActiveSession 决定"继续游戏"按钮行为
 * - `Error(message)`：加载失败（极少发生，数据库错误等异常情况）
 */
package com.zir.sudoku.ui.screen.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(
        val hasActiveSession: Boolean,
        val bestTimeSeconds: Int? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
