/**
 * ## SudokuGenerator — 回溯法数独生成器
 *
 * **职责：**
 * - 使用经典回溯（Backtracking）算法生成完整合法数独盘面
 * - 按难度等级挖空生成谜题，并通过唯一解验证确保谜题质量
 *
 * **核心算法流程：**
 * 1. `generateCompleteBoard()`：从空盘开始，回溯填充 1-9（每步随机打乱顺序），生成完整盘面
 * 2. `generatePuzzle(difficulty)`：复制完整盘面 → 随机打乱所有格子位置 → 逐个挖空
 *    → 每挖一格调用 `hasUniqueSolution()` → 保留唯一解的挖空 → 直到达到目标给定数
 * 3. `hasUniqueSolution(board)`：复制当前盘面 → 回溯计数解答数量 → 达到 2 时提前终止
 *
 * **性能考虑：**
 * - 回溯算法最坏 O(9^(n))，但对 9×9 标准数独，通常毫秒级完成
 * - `hasUniqueSolution` 预提取所有空格坐标，避免每次迭代扫描全盘
 * - 唯一解验证是性能瓶颈（需回溯多次），通过 early return（计数到 2 即停止）优化
 *
 * @see SudokuValidator
 */
package com.zir.sudoku.domain.engine

import com.zir.sudoku.domain.model.Difficulty
import kotlin.random.Random

object SudokuGenerator {

    private const val EMPTY = 0

    /**
     * Generate a complete, valid Sudoku board using backtracking.
     */
    fun generateCompleteBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) { EMPTY } }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == EMPTY) {
                    val numbers = (1..9).shuffled(Random)
                    for (num in numbers) {
                        if (SudokuValidator.isValidPlacement(board, row, col, num)) {
                            board[row][col] = num
                            if (fillBoard(board)) return true
                            board[row][col] = EMPTY
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * Generate a puzzle with the given difficulty.
     * Returns Pair(puzzle, solution).
     */
    fun generatePuzzle(difficulty: Difficulty): Pair<Array<IntArray>, Array<IntArray>> {
        val solution = generateCompleteBoard()
        val puzzle = solution.map { it.copyOf() }.toTypedArray()

        val targetGivens = difficulty.givensRange.random()
        val cellsToRemove = 81 - targetGivens

        // Create a list of all positions and shuffle them
        val positions = (0..8).flatMap { row -> (0..8).map { col -> Pair(row, col) } }
            .shuffled(Random)

        var removed = 0
        for ((row, col) in positions) {
            if (removed >= cellsToRemove) break

            val backup = puzzle[row][col]
            puzzle[row][col] = EMPTY

            // Check if the puzzle still has a unique solution
            if (hasUniqueSolution(puzzle)) {
                removed++
            } else {
                puzzle[row][col] = backup // Restore
            }
        }

        return Pair(puzzle, solution)
    }

    /**
     * Check if the given board has exactly one solution.
     * Uses backtracking and stops counting at 2 solutions.
     */
    fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        val copy = board.map { it.copyOf() }.toTypedArray()
        val solutions = countSolutions(copy, 2)
        return solutions == 1
    }

    private fun countSolutions(board: Array<IntArray>, limit: Int): Int {
        var count = 0
        val rows = IntArray(81)
        val cols = IntArray(81)
        var index = 0

        // Find all empty cells
        for (row in 0..8) {
            for (col in 0..8) {
                if (board[row][col] == EMPTY) {
                    rows[index] = row
                    cols[index] = col
                    index++
                }
            }
        }

        fun solve(pos: Int): Boolean {
            if (pos == index) {
                count++
                return count >= limit
            }

            val row = rows[pos]
            val col = cols[pos]

            for (num in 1..9) {
                if (SudokuValidator.isValidPlacement(board, row, col, num)) {
                    board[row][col] = num
                    if (solve(pos + 1)) return true
                    board[row][col] = EMPTY
                }
            }
            return false
        }

        if (index == 0) return 1 // Already complete
        solve(0)
        return count
    }
}
