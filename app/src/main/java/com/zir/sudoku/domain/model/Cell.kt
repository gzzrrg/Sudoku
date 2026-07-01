/**
 * ## Cell — 数独单元格数据模型
 *
 * **职责：**
 * - 表示棋盘上单个格子的完整状态
 * - 不包含 UI 渲染属性（颜色/高亮等），仅为纯数据模型
 * - 多个 Cell 组成 9×9 的 [BoardState] 传递给 UI 层
 *
 * **字段说明：**
 * - `row` / `col`：格子在棋盘上的坐标（0-8）
 * - `value`：填入的数字（0=空格，1-9=有效数字）
 * - `isGiven`：是否为题目预填（true 的格子不可被用户修改）
 * - `notes`：笔记模式下的候选数集合（如 setOf(3,5,7) 表示该格可能是 3/5/7）
 */
package com.zir.sudoku.domain.model

data class Cell(
    val row: Int,
    val col: Int,
    val value: Int = 0,         // 0 = empty, 1-9 = filled
    val isGiven: Boolean = false, // from initial puzzle, cannot be modified
    val notes: Set<Int> = emptySet() // candidate numbers in note mode
)
