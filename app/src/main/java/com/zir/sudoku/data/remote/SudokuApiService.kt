/**
 * ## SudokuApiService — 远程数独谜题 API
 *
 * **职责：**
 * - 定义 Retrofit HTTP 接口，请求开源数独生成 API
 * - API 端点：`GET https://sudoku-api.vercel.app/api/dosuku`
 * - 返回 `DosukuResponse` DTO，包含谜题盘面（value）和解答（solution）
 *
 * **DTO 结构：**
 * ```
 * DosukuResponse → newboard: DosukuBoard → grids: List<DosukuGrid>
 * DosukuGrid → value: 9×9 (0=空格), solution: 9×9, difficulty: String
 * ```
 *
 * **降级策略：**
 * API 不可用时由 [SudokuRepository.fetchPuzzle()] 自动切换至本地 [SudokuGenerator]
 *
 * @see SudokuRepository
 */
package com.zir.sudoku.data.remote

import retrofit2.http.GET

data class DosukuGrid(
    val value: List<List<Int>>,
    val solution: List<List<Int>>,
    val difficulty: String
)

data class DosukuBoard(
    val grids: List<DosukuGrid>
)

data class DosukuResponse(
    val newboard: DosukuBoard
)

interface SudokuApiService {

    @GET("api/dosuku")
    suspend fun getPuzzle(): DosukuResponse

    companion object {
        const val BASE_URL = "https://sudoku-api.vercel.app/"
    }
}
