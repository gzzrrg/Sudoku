/**
 * ## Difficulty — 游戏难度枚举
 *
 * **职责：**
 * - 定义三个难度等级及其中文显示名称
 * - 包含各难度的给定数范围（givensRange），控制谜题生成时保留的初始数字数量
 * - 提供 `fromDisplayName()` 工厂方法用于字符串→枚举转换
 *
 * **给定数范围（总 81 格）：**
 * - EASY（简单）：38–42 个给定数，约 39–43 个空格
 * - MEDIUM（中等）：28–32 个给定数，约 49–53 个空格
 * - HARD（困难）：22–26 个给定数，约 55–59 个空格
 *
 * 给定数越少，谜题通常越难（但不绝对，还依赖逻辑推理链条的复杂度）。
 */
package com.zir.sudoku.domain.model

enum class Difficulty(val displayName: String, val givensRange: IntRange) {
    EASY("简单", 38..42),
    MEDIUM("中等", 28..32),
    HARD("困难", 22..26);

    companion object {
        fun fromDisplayName(name: String): Difficulty = when (name) {
            "简单" -> EASY
            "困难" -> HARD
            else -> MEDIUM
        }
    }
}
