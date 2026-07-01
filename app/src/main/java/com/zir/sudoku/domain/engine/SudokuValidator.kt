/**
 * ## SudokuValidator — 数独校验器
 *
 * **职责：**
 * - 提供数独规则相关的全部校验逻辑，被 Generator 和 ViewModel 共同使用
 * - 所有方法为纯函数（无副作用），输入 9×9 IntArray 棋盘，输出判定结果
 *
 * **方法说明：**
 * - `isValidPlacement(board, row, col, value)`：检查在指定位置放置 value 是否合法（检查行列宫无重复）
 * - `findConflicts(board)`：全盘扫描，返回所有有冲突的格子坐标集合（用于红色高亮）
 * - `isComplete(board)`：判定是否通关（全盘无空格且无冲突）
 * - `findSameValuePositions(board, row, col)`：查找与指定格数字相同的所有其他格子（用于辅助高亮）
 * - `getRelatedCells(row, col)`：获取指定格所在行/列/宫的所有格子坐标（用于相关格背景高亮）
 *
 * **数独规则：**
 * 每行、每列、每个 3×3 宫内，数字 1-9 必须恰好出现一次（不重复）
 */
package com.zir.sudoku.domain.engine

object SudokuValidator {

    fun isValidPlacement(board: Array<IntArray>, row: Int, col: Int, value: Int): Boolean {
        if (value == 0) return true
        // Check row
        for (c in 0..8) {
            if (c != col && board[row][c] == value) return false
        }
        // Check column
        for (r in 0..8) {
            if (r != row && board[r][col] == value) return false
        }
        // Check 3x3 box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) return false
            }
        }
        return true
    }

    fun findConflicts(board: Array<IntArray>): Set<Pair<Int, Int>> {
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        for (row in 0..8) {
            for (col in 0..8) {
                val value = board[row][col]
                if (value != 0 && !isValidPlacement(board, row, col, value)) {
                    conflicts.add(Pair(row, col))
                }
            }
        }
        return conflicts
    }

    fun isComplete(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == 0) return false
                if (!isValidPlacement(board, row, col, board[row][col])) return false
            }
        }
        return true
    }

    fun findSameValuePositions(board: Array<IntArray>, row: Int, col: Int): Set<Pair<Int, Int>> {
        val value = board[row][col]
        if (value == 0) return emptySet()

        val positions = mutableSetOf<Pair<Int, Int>>()

        // Scan entire board for cells with the same value
        for (r in 0..8) {
            for (c in 0..8) {
                if ((r != row || c != col) && board[r][c] == value) {
                    positions.add(Pair(r, c))
                }
            }
        }

        return positions
    }

    fun getRelatedCells(row: Int, col: Int): Set<Pair<Int, Int>> {
        val cells = mutableSetOf<Pair<Int, Int>>()

        // Same row
        for (c in 0..8) {
            if (c != col) cells.add(Pair(row, c))
        }
        // Same column
        for (r in 0..8) {
            if (r != row) cells.add(Pair(r, col))
        }
        // Same 3x3 box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (r != row || c != col) cells.add(Pair(r, c))
            }
        }

        return cells
    }
}
