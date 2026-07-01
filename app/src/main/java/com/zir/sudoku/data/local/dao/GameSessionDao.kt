/**
 * ## GameSessionDao — 游戏存档 DAO
 *
 * **职责：**
 * - 对 [GameSession] 表进行 CRUD 操作
 * - `upsertSession()` 使用 REPLACE 策略（insert or update）
 * - `getActiveSession()` 返回 Flow，支持响应式查询（自动存档时 UI 无需轮询）
 * - `getActiveSessionOnce()` 返回一次性结果（挂起函数），用于 ViewModel 初始化时快速加载
 * - `deleteActiveSession()` 清除存档（游戏完成或放弃时调用）
 */
package com.zir.sudoku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zir.sudoku.data.local.entity.GameSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: GameSession)

    @Query("SELECT * FROM game_session WHERE id = 1")
    fun getActiveSession(): Flow<GameSession?>

    @Query("SELECT * FROM game_session WHERE id = 1")
    suspend fun getActiveSessionOnce(): GameSession?

    @Query("DELETE FROM game_session WHERE id = 1")
    suspend fun deleteActiveSession()
}
