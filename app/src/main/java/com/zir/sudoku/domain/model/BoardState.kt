/**
 * ## BoardState — 棋盘 UI 渲染快照
 *
 * **职责：**
 * - 包含 UI 渲染棋盘所需的所有状态信息的不可变数据类
 * - 由 [GameViewModel.buildBoardState()] 在每次状态变更时重建
 * - 通过 StateFlow 推送到 UI 层，Compose 根据此快照重组棋盘
 *
 * **字段说明：**
 * - `cells`：9×9 的 [Cell] 列表，每个格子的值/预填/笔记状态
 * - `selectedRow` / `selectedCol`：当前用户选中的格子坐标（-1 表示未选中）
 * - `conflicts`：所有存在行列宫冲突的格子坐标集合（用于红色高亮）
 * - `sameValueHighlights`：与选中格数字相同的所有格子坐标集合（辅助推理）
 * - `wrongCells`：填错但未产生行列宫冲突的格子坐标集合（用于 text-error 橙色文字）
 * - `isNoteMode`：当前是否为笔记模式（影响数字键盘输入行为）
 */
package com.zir.sudoku.domain.model

data class BoardState(
    val cells: List<List<Cell>>,
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val conflicts: Set<Pair<Int, Int>> = emptySet(),
    val sameValueHighlights: Set<Pair<Int, Int>> = emptySet(),
    val wrongCells: Set<Pair<Int, Int>> = emptySet(),
    val isNoteMode: Boolean = false
) {
    val selectedCell: Cell?
        get() = if (selectedRow in 0..8 && selectedCol in 0..8) {
            cells[selectedRow][selectedCol]
        } else null
}
