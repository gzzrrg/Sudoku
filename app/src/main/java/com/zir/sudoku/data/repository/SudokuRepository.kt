/**
 * ## SudokuRepository — 统一数据访问层
 *
 * **职责（核心中间层）：**
 * - 封装所有数据源（Room DAO + DataStore + Retrofit API），为 ViewModel 提供统一接口
 * - 所有数据访问方法均为挂起函数或返回 Flow，ViewModel 不直接接触 DAO 或 API
 * - `fetchPuzzle()` 包含 API → 本地生成器的自动降级逻辑
 * - 提供棋盘数据 ↔ JSON 双向序列化工具方法
 *
 * **降级流程：**
 * ```
 * fetchPuzzle(difficulty)
 *   → try API (dosuku)
 *   → 成功 → 返回谜题+解答
 *   → 失败 → SudokuGenerator.generatePuzzle(difficulty) → 返回本地谜题+解答
 * ```
 *
 * **序列化：**
 * - 使用 Gson 进行 Array<IntArray> ↔ JSON 转换
 * - notes 三维数组（9×9×Set）的序列化/反序列化处理
 *
 * **线程安全：** 所有挂起函数在协程上下文中执行，Room 和 DataStore 自带线程安全
 */
package com.zir.sudoku.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zir.sudoku.data.local.dao.GameRecordDao
import com.zir.sudoku.data.local.dao.GameSessionDao
import com.zir.sudoku.data.local.datastore.SettingsDataStore
import com.zir.sudoku.data.local.entity.GameRecord
import com.zir.sudoku.data.local.entity.GameSession
import com.zir.sudoku.data.remote.SudokuApiService
import com.zir.sudoku.domain.engine.SudokuGenerator
import kotlinx.coroutines.flow.Flow

class SudokuRepository(
    private val gameSessionDao: GameSessionDao,
    private val gameRecordDao: GameRecordDao,
    private val settingsDataStore: SettingsDataStore,
    private val apiService: SudokuApiService?
) {
    private val gson = Gson()

    // ---- Game Session ----

    fun getActiveSession(): Flow<GameSession?> = gameSessionDao.getActiveSession()

    suspend fun getActiveSessionOnce(): GameSession? = gameSessionDao.getActiveSessionOnce()

    suspend fun saveSession(session: GameSession) {
        gameSessionDao.upsertSession(session)
    }

    suspend fun deleteActiveSession() {
        gameSessionDao.deleteActiveSession()
    }

    // ---- Game Records ----

    fun getAllRecords(): Flow<List<GameRecord>> = gameRecordDao.getAllRecords()

    fun getRecordsByDifficulty(difficulty: String): Flow<List<GameRecord>> =
        gameRecordDao.getRecordsByDifficulty(difficulty)

    fun getBestTime(difficulty: String): Flow<Int?> = gameRecordDao.getBestTime(difficulty)

    fun getAverageTime(difficulty: String): Flow<Double?> = gameRecordDao.getAverageTime(difficulty)

    fun getCountByDifficulty(difficulty: String): Flow<Int> =
        gameRecordDao.getCountByDifficulty(difficulty)

    fun getTotalCount(): Flow<Int> = gameRecordDao.getTotalCount()

    fun getErrorFreeWinCount(difficulty: String): Flow<Int> =
        gameRecordDao.getErrorFreeWinCount(difficulty)

    fun getOverallBestTime(): Flow<Int?> = gameRecordDao.getOverallBestTime()

    suspend fun saveRecord(record: GameRecord) {
        gameRecordDao.insertRecord(record)
    }

    suspend fun deleteAllRecords() {
        gameRecordDao.deleteAllRecords()
    }

    // ---- Settings ----

    fun getSettings(): Flow<SettingsDataStore.UserSettings> = settingsDataStore.settings

    suspend fun setThemeMode(mode: String) = settingsDataStore.setThemeMode(mode)

    suspend fun setHighlightConflicts(enabled: Boolean) =
        settingsDataStore.setHighlightConflicts(enabled)

    suspend fun setSoundEnabled(enabled: Boolean) = settingsDataStore.setSoundEnabled(enabled)

    suspend fun setVibrationEnabled(enabled: Boolean) =
        settingsDataStore.setVibrationEnabled(enabled)

    suspend fun setScoreAnimationEnabled(enabled: Boolean) =
        settingsDataStore.setScoreAnimationEnabled(enabled)

    suspend fun setErrorLimitEnabled(enabled: Boolean) =
        settingsDataStore.setErrorLimitEnabled(enabled)

    // ---- Statistics ----

    fun getStats(difficulty: String): Flow<SettingsDataStore.DifficultyStats> =
        settingsDataStore.getStats(difficulty)

    suspend fun incrementGamesStarted(difficulty: String) =
        settingsDataStore.incrementGamesStarted(difficulty)

    suspend fun recordGameWin(difficulty: String) =
        settingsDataStore.recordGameWin(difficulty)

    suspend fun recordGameLoss(difficulty: String) =
        settingsDataStore.recordGameLoss(difficulty)

    suspend fun resetStats(difficulty: String) =
        settingsDataStore.resetStats(difficulty)

    suspend fun resetAllStats() =
        settingsDataStore.resetAllStats()

    suspend fun clearAllData() {
        gameRecordDao.deleteAllRecords()
        gameSessionDao.deleteActiveSession()
        settingsDataStore.clearAll()
    }

    // ---- Puzzle Fetching ----

    suspend fun fetchPuzzle(difficulty: String): Result<Pair<Array<IntArray>, Array<IntArray>>> {
        return try {
            // Try API first
            val response = apiService?.getPuzzle()
            if (response != null && response.newboard.grids.isNotEmpty()) {
                val grid = response.newboard.grids.first()
                val puzzle = grid.value.map { it.toIntArray() }.toTypedArray()
                val solution = grid.solution.map { it.toIntArray() }.toTypedArray()
                Result.success(Pair(puzzle, solution))
            } else {
                // API returned no data, use local generator
                generateLocally(difficulty)
            }
        } catch (e: Exception) {
            // API failed, fallback to local generator
            generateLocally(difficulty)
        }
    }

    private fun generateLocally(difficulty: String): Result<Pair<Array<IntArray>, Array<IntArray>>> {
        return try {
            val generatorDifficulty = when (difficulty) {
                "简单" -> com.zir.sudoku.domain.model.Difficulty.EASY
                "困难" -> com.zir.sudoku.domain.model.Difficulty.HARD
                else -> com.zir.sudoku.domain.model.Difficulty.MEDIUM
            }
            val (puzzle, solution) = SudokuGenerator.generatePuzzle(generatorDifficulty)
            Result.success(Pair(puzzle, solution))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ---- Serialization helpers ----

    fun boardToJson(board: Array<IntArray>): String = gson.toJson(board)

    fun jsonToBoard(json: String): Array<IntArray> {
        val type = object : TypeToken<Array<IntArray>>() {}.type
        return gson.fromJson(json, type)
    }

    fun notesToJson(notes: Array<Array<Set<Int>>>): String {
        val listNotes = notes.map { row -> row.map { it.toList() } }
        return gson.toJson(listNotes)
    }

    fun jsonToNotes(json: String): Array<Array<Set<Int>>> {
        val type = object : TypeToken<List<List<List<Int>>>>() {}.type
        val listNotes: List<List<List<Int>>> = gson.fromJson(json, type)
        return listNotes.map { row -> row.map { it.toSet() }.toTypedArray() }.toTypedArray()
    }
}
