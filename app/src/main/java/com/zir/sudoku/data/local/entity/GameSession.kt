/**
 * ## GameSession — 当前游戏存档实体
 *
 * **职责：**
 * - Room @Entity，存储唯一活跃游戏的完整快照
 * - 主键固定为 1（单存档模型，同一时间只有一个进行中的游戏）
 * - 棋盘数据（谜题/解答/当前状态/笔记）以 JSON 字符串存储
 * - 操作历史（撤销/重做栈）同样以 JSON 序列化存储
 *
 * **JSON 字段说明：**
 * - `puzzleJson`：初始谜题 9×9 数组（0=空格）
 * - `solutionJson`：完整解答 9×9 数组
 * - `currentBoardJson`：用户当前填入状态 9×9 数组
 * - `notesJson`：所有格子的笔记（候选数集合）三维数组
 * - `operationHistoryJson`：撤销操作栈
 * - `redoStackJson`：重做操作栈
 *
 * @see GameSessionDao
 */
package com.zir.sudoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_session")
data class GameSession(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "puzzle_json")
    val puzzleJson: String,

    @ColumnInfo(name = "solution_json")
    val solutionJson: String,

    @ColumnInfo(name = "current_board_json")
    val currentBoardJson: String,

    @ColumnInfo(name = "notes_json")
    val notesJson: String,

    val difficulty: String,

    @ColumnInfo(name = "elapsed_seconds")
    val elapsedSeconds: Int = 0,

    @ColumnInfo(name = "hint_count")
    val hintCount: Int = 3,

    @ColumnInfo(name = "operation_history_json")
    val operationHistoryJson: String = "[]",

    @ColumnInfo(name = "redo_stack_json")
    val redoStackJson: String = "[]",

    @ColumnInfo(name = "is_paused")
    val isPaused: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
