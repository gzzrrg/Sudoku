/**
 * ## GameRecordDao — 历史记录 DAO
 *
 * **职责：**
 * - 对 [GameRecord] 表进行增删查操作
 * - 支持按难度筛选、按时间排序
 * - 提供聚合统计查询：最佳用时（MIN）、平均用时（AVG）、计数（COUNT）
 * - 所有读取返回 Flow，支持 Compose 响应式收集
 *
 * **典型使用场景：**
 * - 首页展示最近完成记录
 * - 通关后判断是否为新纪录（对比 `getBestTime()`）
 * - 设置页面"清除数据"时调用 `deleteAllRecords()`
 */
package com.zir.sudoku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.zir.sudoku.data.local.entity.GameRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert
    suspend fun insertRecord(record: GameRecord)

    @Query("SELECT * FROM game_record ORDER BY completed_at DESC")
    fun getAllRecords(): Flow<List<GameRecord>>

    @Query("SELECT * FROM game_record WHERE difficulty = :difficulty ORDER BY completed_at DESC")
    fun getRecordsByDifficulty(difficulty: String): Flow<List<GameRecord>>

    @Query("SELECT MIN(time_seconds) FROM game_record WHERE difficulty = :difficulty")
    fun getBestTime(difficulty: String): Flow<Int?>

    @Query("SELECT AVG(time_seconds) FROM game_record WHERE difficulty = :difficulty")
    fun getAverageTime(difficulty: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM game_record WHERE difficulty = :difficulty")
    fun getCountByDifficulty(difficulty: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_record")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_record WHERE difficulty = :difficulty AND error_count = 0")
    fun getErrorFreeWinCount(difficulty: String): Flow<Int>

    @Query("SELECT MIN(time_seconds) FROM game_record")
    fun getOverallBestTime(): Flow<Int?>

    @Query("DELETE FROM game_record")
    suspend fun deleteAllRecords()
}
