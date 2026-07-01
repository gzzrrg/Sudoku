/**
 * ## Operation — 撤销/重做操作记录
 *
 * **职责：**
 * - 记录单次棋盘操作的完整快照变更
 * - 被序列化为 JSON 存储在 [GameSession.operationHistoryJson] 和 [redoStackJson] 中
 * - 撤销时恢复 `previous*` 值，重做时重新应用 `new*` 值
 *
 * **字段说明：**
 * - `row` / `col`：操作发生的位置
 * - `previousValue` / `newValue`：操作前后格子的数字值
 * - `previousNotes` / `newNotes`：操作前后格子的笔记集合
 *
 * **使用示例：**
 * 用户在 (2,3) 填入数字 5，之前为空格无笔记：
 * ```kotlin
 * Operation(row=2, col=3, previousValue=0, newValue=5, previousNotes=setOf(), newNotes=setOf())
 * ```
 */
package com.zir.sudoku.domain.model

data class Operation(
    val row: Int,
    val col: Int,
    val previousValue: Int = 0,
    val newValue: Int = 0,
    val previousNotes: Set<Int> = emptySet(),
    val newNotes: Set<Int> = emptySet()
)
