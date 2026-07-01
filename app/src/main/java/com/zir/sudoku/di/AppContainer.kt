/**
 * ## AppContainer — 手动依赖注入容器
 *
 * **职责：**
 * - 创建并持有应用中所有核心依赖的单例实例
 * - 组装 Room 数据库 → DAO、DataStore、Retrofit API → [SudokuRepository]
 * - 不依赖 Hilt/Koin 等第三方 DI 框架，保持构建简单
 *
 * **依赖关系：**
 * ```
 * Context → SudokuDatabase → GameSessionDao + GameRecordDao
 * Context → SettingsDataStore
 * Retrofit → SudokuApiService
 * DAOs + DataStore + ApiService → SudokuRepository
 * ```
 *
 * **注意：** Retrofit 初始化包裹在 try-catch 中，构建失败时 apiService 为 null，
 * Repository 会自动降级到本地生成器。
 */
package com.zir.sudoku.di

import android.content.Context
import com.zir.sudoku.data.local.SudokuDatabase
import com.zir.sudoku.data.local.datastore.SettingsDataStore
import com.zir.sudoku.data.remote.SudokuApiService
import com.zir.sudoku.data.repository.SudokuRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val database = SudokuDatabase.getInstance(context)
    private val settingsDataStore = SettingsDataStore(context)

    private val apiService: SudokuApiService? = try {
        Retrofit.Builder()
            .baseUrl(SudokuApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SudokuApiService::class.java)
    } catch (e: Exception) {
        null
    }

    val repository = SudokuRepository(
        gameSessionDao = database.gameSessionDao(),
        gameRecordDao = database.gameRecordDao(),
        settingsDataStore = settingsDataStore,
        apiService = apiService
    )
}
