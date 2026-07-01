/**
 * ## Color — 应用全局配色定义
 *
 * **职责：**
 * - 定义 Material 3 主题所需的全套色值（浅色 / 深色双模式）
 * - 提供 `SudokuColors` 对象，包含棋盘渲染所需的全部专用颜色
 *
 * **配色体系（v2.0 — HTML 设计稿对齐）：**
 *
 * **浅色模式（护眼绿调）：**
 * - 应用背景 `#DFECD1`，棋盘背景 `#F7FAF2`
 * - 宫格细线 `#CAD5BF` / 粗线 `#5D6A52`
 * - 选中 `#8CB85C`（绿底白字）/ 关联 `#C7E8B2`（浅绿）/ 同数聚焦 `#519E43`（深绿白字）
 * - 冲突 `#FF6A6E`（红底白字）/ 填错文字 `#E36749`（橙红）
 *
 * **暗黑模式（深灰 + 绿强调）：**
 * - 应用背景 `#1C1C1E`，棋盘背景 `#2C2C2E`
 * - 宫格细线 `#3A3A3C` / 粗线 `#636366`
 * - 选中 `#8DC563`（绿底白字）/ 关联 `#38383A`（深灰）/ 同数聚焦 `#8DC563`（绿底白字）
 * - 冲突 `#E65A53`（红底白字）/ 填错文字 `#E36749`（橙红）
 *
 * @see SudokuTheme
 */
package com.zir.sudoku.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Light Mode — HTML 设计稿浅色主题
// ============================================================

val LightPrimary = Color(0xFF8CB85C)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFC7E8B2)
val LightOnPrimaryContainer = Color(0xFF1A2920)
val LightSecondary = Color(0xFF6D8970)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDFECD1)
val LightOnSecondaryContainer = Color(0xFF2F3D2C)
val LightTertiary = Color(0xFF22663D)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFDFECD1)
val LightOnBackground = Color(0xFF2C4B2A)
val LightSurface = Color(0xFFF7FAF2)
val LightOnSurface = Color(0xFF1A2920)
val LightSurfaceVariant = Color(0xFFCAD5BF)
val LightOnSurfaceVariant = Color(0xFF4D5D4B)
val LightError = Color(0xFFE54246)
val LightOnError = Color(0xFFFFFFFF)
val LightOutline = Color(0xFFCAD5BF)

// ============================================================
// Dark Mode — HTML 设计稿深色主题
// ============================================================

val DarkPrimary = Color(0xFF8DC563)
val DarkOnPrimary = Color(0xFF1A2920)
val DarkPrimaryContainer = Color(0xFF38383A)
val DarkOnPrimaryContainer = Color(0xFFFFFFFF)
val DarkSecondary = Color(0xFF8E8E93)
val DarkOnSecondary = Color(0xFF1C1C1E)
val DarkSecondaryContainer = Color(0xFF48484A)
val DarkOnSecondaryContainer = Color(0xFFFFFFFF)
val DarkTertiary = Color(0xFF5B4091)
val DarkOnTertiary = Color(0xFFFFFFFF)
val DarkBackground = Color(0xFF1C1C1E)
val DarkOnBackground = Color(0xFFD1D1D6)
val DarkSurface = Color(0xFF2C2C2E)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkSurfaceVariant = Color(0xFF3A3A3C)
val DarkOnSurfaceVariant = Color(0xFF8E8E93)
val DarkError = Color(0xFFFF4B4B)
val DarkOnError = Color(0xFFFFFFFF)
val DarkOutline = Color(0xFF636366)

// ============================================================
// White Mode — 纯白主题
// ============================================================

val WhitePrimary = Color(0xFF8B7D6B)
val WhiteOnPrimary = Color(0xFFFFFFFF)
val WhitePrimaryContainer = Color(0xFFE8E0D8)
val WhiteOnPrimaryContainer = Color(0xFF333333)
val WhiteSecondary = Color(0xFF8E8E8E)
val WhiteOnSecondary = Color(0xFFFFFFFF)
val WhiteSecondaryContainer = Color(0xFFF0F0F0)
val WhiteOnSecondaryContainer = Color(0xFF444444)
val WhiteTertiary = Color(0xFF6750A4)
val WhiteOnTertiary = Color(0xFFFFFFFF)
val WhiteBackground = Color(0xFFFFFFFF)
val WhiteOnBackground = Color(0xFF1A1A1A)
val WhiteSurface = Color(0xFFF8F8F8)
val WhiteOnSurface = Color(0xFF1A1A1A)
val WhiteSurfaceVariant = Color(0xFFE8E0D8)
val WhiteOnSurfaceVariant = Color(0xFF555555)
val WhiteError = Color(0xFFE54246)
val WhiteOnError = Color(0xFFFFFFFF)
val WhiteOutline = Color(0xFFCCCCCC)

// ============================================================
// Sudoku Board Colors Palette — 数独棋盘专用配色
// 通过 CompositionLocal 自动根据浅色/深色/白色模式切换
// ============================================================
data class SudokuColorPalette(
    // Grid 网格线
    val gridLine: Color,                     // 宫格细线（分隔每个格子）
    val boxBorder: Color,                    // 宫格粗线（分隔3×3宫 + 外边框）

    // Cell backgrounds 格子背景
    val cellEmpty: Color,                    // 空格背景
    val cellGivenBg: Color,                  // 预填数字格背景
    val selectedCell: Color,                 // 当前选中格高亮（highlight-selected）
    val sameValueHighlight: Color,           // 同数聚焦高亮（highlight-focus）
    val relatedCellHighlight: Color,         // 同行/同列/同宫关联高亮（highlight-bg）

    // Number colors 数字颜色
    val givenNumber: Color,                  // 预填数字颜色（粗体）
    val userNumber: Color,                   // 用户填入的数字颜色
    val noteText: Color,                     // 笔记/候选数字颜色

    // Conflicts 冲突色
    val conflictBackground: Color,           // 冲突格背景色（highlight-error 红底）
    val conflictText: Color,                 // 冲突数字文字颜色（白色）

    // Toolbar buttons 底部控制栏按钮
    val eraseBtn: Color,                     // 擦除按钮图标背景色
    val undoBtn: Color,                      // 撤回按钮图标背景色
    val noteBtn: Color,                      // 笔记按钮图标背景色
    val hintBtn: Color,                      // 提示按钮图标背景色

    // Labels & text 标签与文字
    val labelColor: Color,                   // 底部按钮标签文字颜色
    val numPadColor: Color,                  // 数字键盘文字颜色
    val iconColor: Color,                    // 顶部栏图标填充色
    val selectedOnHighlight: Color,          // 选中/聚焦格上的文字颜色（白色）
    val textError: Color,                    // 填错但无冲突的文字颜色（橙红 text-error）

    // Status & panels 状态栏与面板
    val boardBackground: Color,              // 棋盘填充背景色
    val errorCountText: Color,               // 错误计数文字颜色
    val statusGray: Color,                   // 状态栏灰色文字
    val appBackground: Color,                // 全局应用背景色
    val surfaceBackground: Color,            // 表面/卡片背景色
    val rightPanelBg: Color,                 // 右侧面板背景（平板双栏）

    // Overlay 遮罩
    val pauseOverlay: Color = Color(0xBB000000),

    // ======== 向后兼容字段（不再使用，保留默认值） ========
    @Suppress("unused")
    val boxBorderTablet: Color = boxBorder,
    @Suppress("unused")
    val relatedText: Color = givenNumber,
    @Suppress("unused")
    val funcButtonDefault: Color = Color.Transparent,
    @Suppress("unused")
    val funcButtonActive: Color = Color.Transparent,
    @Suppress("unused")
    val notesToggleGreen: Color = Color(0xFF4CAF50),
    @Suppress("unused")
    val buttonClickFeedback: Color = Color(0xFFCCE6CC),
) {
    companion object {
        val Light = SudokuColorPalette(
            // Grid
            gridLine = Color(0xFFCAD5BF),
            boxBorder = Color(0xFF5D6A52),
            // Cell backgrounds
            cellEmpty = Color.Transparent,
            cellGivenBg = Color.Transparent,
            selectedCell = Color(0xFF8CB85C),
            sameValueHighlight = Color(0xFF519E43),
            relatedCellHighlight = Color(0xFFC7E8B2),
            // Number colors
            givenNumber = Color(0xFF1A2920),
            userNumber = Color(0xFF1A2920),
            noteText = Color(0xFF6D8970),
            // Conflicts
            conflictBackground = Color(0xFFFF6A6E),
            conflictText = Color(0xFFFFFFFF),
            // Toolbar buttons
            eraseBtn = Color(0xFF5D6159),
            undoBtn = Color(0xFF959A90),
            noteBtn = Color(0xFF22663D),
            hintBtn = Color(0xFF22663D),
            // Labels & text
            labelColor = Color(0xFF4D5D4B),
            numPadColor = Color(0xFF2F3D2C),
            iconColor = Color(0xFF2C4B2A),
            selectedOnHighlight = Color(0xFFFFFFFF),
            textError = Color(0xFFE36749),
            // Status & panels
            boardBackground = Color(0xFFF7FAF2),
            errorCountText = Color(0xFFE54246),
            statusGray = Color(0xFF6D8970),
            appBackground = Color(0xFFDFECD1),
            surfaceBackground = Color(0xFFF7FAF2),
            rightPanelBg = Color(0xFFF7FAF2),
        )

        val Dark = SudokuColorPalette(
            // Grid
            gridLine = Color(0xFF3A3A3C),
            boxBorder = Color(0xFF636366),
            // Cell backgrounds
            cellEmpty = Color(0xFF2C2C2E),
            cellGivenBg = Color(0xFF2C2C2E),
            selectedCell = Color(0xFF8DC563),
            sameValueHighlight = Color(0xFF8DC563),
            relatedCellHighlight = Color(0xFF38383A),
            // Number colors
            givenNumber = Color(0xFFFFFFFF),
            userNumber = Color(0xFFFFFFFF),
            noteText = Color(0xFF8E8E93),
            // Conflicts
            conflictBackground = Color(0xFFE65A53),
            conflictText = Color(0xFFFFFFFF),
            // Toolbar buttons
            eraseBtn = Color(0xFF1C1C1E),
            undoBtn = Color(0xFF48484A),
            noteBtn = Color(0xFF4A8C4A),
            hintBtn = Color(0xFF4A8C4A),
            // Labels & text
            labelColor = Color(0xFF8E8E93),
            numPadColor = Color(0xFFD1D1D6),
            iconColor = Color(0xFFFFFFFF),
            selectedOnHighlight = Color(0xFFFFFFFF),
            textError = Color(0xFFE36749),
            // Status & panels
            boardBackground = Color(0xFF2C2C2E),
            errorCountText = Color(0xFFFF4B4B),
            statusGray = Color(0xFF8E8E93),
            appBackground = Color(0xFF1C1C1E),
            surfaceBackground = Color(0xFF2C2C2E),
            rightPanelBg = Color(0xFF2C2C2E),
        )

        val White = SudokuColorPalette(
            // Grid
            gridLine = Color(0xFFD8D8D8),
            boxBorder = Color(0xFF999999),
            // Cell backgrounds
            cellEmpty = Color.Transparent,
            cellGivenBg = Color.Transparent,
            selectedCell = Color(0xFFE8E0D8),
            sameValueHighlight = Color(0xFFD5CCC0),
            relatedCellHighlight = Color(0xFFF2EFEC),
            // Number colors
            givenNumber = Color(0xFF1A1A1A),
            userNumber = Color(0xFF1A1A1A),
            noteText = Color(0xFFAAAAAA),
            // Conflicts
            conflictBackground = Color(0xFFFF6A6E),
            conflictText = Color(0xFFFFFFFF),
            // Toolbar buttons
            eraseBtn = Color(0xFF888888),
            undoBtn = Color(0xFFAAAAAA),
            noteBtn = Color(0xFF8B7D6B),
            hintBtn = Color(0xFF8B7D6B),
            // Labels & text
            labelColor = Color(0xFF777777),
            numPadColor = Color(0xFF1A1A1A),
            iconColor = Color(0xFF444444),
            selectedOnHighlight = Color(0xFF333333),
            textError = Color(0xFFE36749),
            // Status & panels
            boardBackground = Color(0xFFFFFFFF),
            errorCountText = Color(0xFFE54246),
            statusGray = Color(0xFF999999),
            appBackground = Color(0xFFFFFFFF),
            surfaceBackground = Color(0xFFF5F5F5),
            rightPanelBg = Color(0xFFF5F5F5),
        )
    }
}
