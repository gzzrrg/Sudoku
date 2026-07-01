/**
 * ## SudokuBoard — 9×9 数独棋盘组件
 *
 * **职责：**
 * - 渲染标准数独 9×9 宫格（81 个单元格）
 * - 绘制网格线：3×3 宫之间粗线（2.5px），宫内细线（1px）
 * - 根据 [BoardState] 渲染各格状态：选中、冲突、同数高亮、给定数字、用户数字、笔记
 * - 支持浅色/深色双模式，通过 `isDarkTheme` 参数切换配色
 *
 * **视觉层次（逐级叠加）：**
 * 1. 空格背景（浅色透明 / 深色 #262626）
 * 2. 给定格背景（浅色 #DCE8DA / 深色 #333333）
 * 3. 选中格覆盖（浅色 #D0E8D0 / 深色 #9ACD32 25%）
 * 4. 冲突格覆盖（浅色 #E74C3C 20% / 深色 #FF5252 25%）
 * 5. 同数格覆盖（浅色 #D8ECD8 / 深色 #9ACD32 15%）
 *
 * **数字渲染：**
 * - 给定数字：Bold + 深色
 * - 用户数字：Normal + 蓝色调（浅色）/ 浅灰（深色）
 * - 冲突数字：红色
 * - 同数关联数字：淡紫色（仅深色模式）
 *
 * **笔记渲染：**
 * - NotesGrid：3×3 子网格，每个位置显示对应的候选数字（8sp 小号灰色文字）
 *
 * @see BoardState
 * @see Cell
 * @see SudokuColors
 */
package com.zir.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zir.sudoku.domain.model.BoardState
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@Composable
fun SudokuBoard(
    boardState: BoardState,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSudokuPalette.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, palette.boxBorder)
            .drawWithContent {
                drawContent()
                val cellW = size.width / 9f
                val cellH = size.height / 9f
                val thinPx = 1.dp.toPx()
                val thickPx = 2.dp.toPx()
                // Pass 1: draw thin inner lines first
                for (i in 1..8) {
                    if (i % 3 == 0) continue
                    val x = i * cellW
                    drawLine(palette.gridLine, Offset(x, 0f), Offset(x, size.height), thinPx)
                    val y = i * cellH
                    drawLine(palette.gridLine, Offset(0f, y), Offset(size.width, y), thinPx)
                }
                // Pass 2: draw thick 3x3 box lines on top
                for (i in 3..6 step 3) {
                    val x = i * cellW
                    drawLine(palette.boxBorder, Offset(x, 0f), Offset(x, size.height), thickPx)
                    val y = i * cellH
                    drawLine(palette.boxBorder, Offset(0f, y), Offset(size.width, y), thickPx)
                }
            }
    ) {
        for (row in 0..8) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0..8) {
                    SudokuCell(
                        boardState = boardState,
                        row = row,
                        col = col,
                        onClick = { onCellClick(row, col) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SudokuCell(
    boardState: BoardState,
    row: Int,
    col: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalSudokuPalette.current
    val cell = boardState.cells[row][col]
    val isSelected = row == boardState.selectedRow && col == boardState.selectedCol
    val isConflict = boardState.conflicts.contains(Pair(row, col))
    val isSameValue = boardState.sameValueHighlights.contains(Pair(row, col))
    val isRelated = boardState.selectedRow in 0..8 && boardState.selectedCol in 0..8 &&
            (row == boardState.selectedRow || col == boardState.selectedCol ||
                    (row / 3 == boardState.selectedRow / 3 && col / 3 == boardState.selectedCol / 3))

    val isWrongValue = boardState.wrongCells.contains(Pair(row, col))

    // Is the currently selected cell a wrong cell?
    val selectedIsWrong = boardState.selectedRow in 0..8 && boardState.selectedCol in 0..8 &&
        boardState.wrongCells.contains(Pair(boardState.selectedRow, boardState.selectedCol))

    // When the wrong cell is selected, sameValue cells get red focus instead of green
    val isWrongGroup = selectedIsWrong && isSameValue

    // Only show conflict highlighting while editing a wrong cell
    val showConflict = isConflict && selectedIsWrong

    // Background color
    val bgColor = when {
        isSelected && isWrongValue -> palette.conflictBackground
        isSelected -> palette.selectedCell
        showConflict -> palette.conflictBackground
        isWrongGroup -> palette.conflictBackground
        isWrongValue -> palette.cellEmpty
        isSameValue -> palette.sameValueHighlight
        isRelated -> palette.relatedCellHighlight
        cell.isGiven -> palette.cellGivenBg
        else -> palette.cellEmpty
    }

    Box(
        modifier = modifier
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            val textColor = when {
                isSelected && isWrongValue -> palette.conflictText
                isSelected -> palette.selectedOnHighlight
                showConflict -> palette.conflictText
                isWrongGroup -> palette.conflictText
                isWrongValue -> palette.textError
                isSameValue -> palette.selectedOnHighlight
                cell.isGiven -> palette.givenNumber
                else -> palette.userNumber
            }
            Text(
                text = cell.value.toString(),
                fontSize = 20.sp,
                fontWeight = if (cell.isGiven || cell.value != 0) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center
            )
        } else if (cell.notes.isNotEmpty()) {
            NotesGrid(cell.notes)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>) {
    val palette = LocalSudokuPalette.current
    // Use dedicated noteText color from palette for consistent visibility across themes
    val noteColor = palette.noteText
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        verticalArrangement = Arrangement.Center
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0..2) {
                    val num = row * 3 + col + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (num in notes) {
                            Text(
                                text = num.toString(),
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                color = noteColor,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}
