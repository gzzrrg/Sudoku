/**
 * ## GameRecord — 历史完成记录实体
 *
 * **职责：**
 * - Room @Entity，存储每次成功完成的游戏数据
 * - 自增主键，支持多条历史记录
 * - 记录难度、用时（秒）和完成时间戳
 *
 * **用途：**
 * - 历史记录列表展示（按完成时间倒序）
 * - 按难度筛选统计
 * - 最佳用时 / 平均用时等聚合查询
 * - 统计页面数据来源
 *
 * @see GameRecordDao
 */
package com.zir.sudoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_record")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val difficulty: String,

    @ColumnInfo(name = "time_seconds")
    val timeSeconds: Int,

    @ColumnInfo(name = "error_count")
    val errorCount: Int = 0,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis()
)
