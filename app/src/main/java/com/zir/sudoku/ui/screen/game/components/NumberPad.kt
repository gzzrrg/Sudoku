/**
 * ## NumberPad — 1-9 数字输入键盘
 *
 * **职责：**
 * - 渲染 1-9 数字按钮（横向等宽排列）
 * - 数字始终可点击，无灰化禁用态
 * - 当前选中格的数字以绿色背景 + 白色文字 + 边框高亮标识
 * - 支持宽屏模式（手机横屏/平板）：分为两行（1-5 / 6-9）
 *
 * **交互行为：**
 * - 正常模式：点击数字 → `onNumberClick(num)` → ViewModel 填入/替换
 * - 笔记模式：点击数字 → ViewModel 切换该格的候选数
 *
 * @see BoardState
 */
package com.zir.sudoku.ui.screen.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zir.sudoku.domain.model.BoardState
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@Composable
fun NumberPad(
    boardState: BoardState,
    onNumberClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isWide: Boolean = false
) {
    val palette = LocalSudokuPalette.current

    val hasSelection = boardState.selectedRow in 0..8 && boardState.selectedCol in 0..8
    val selectedValue = if (hasSelection) {
        boardState.cells[boardState.selectedRow][boardState.selectedCol].value
    } else 0
    // Notes of the selected cell (only relevant when note mode is active)
    val selectedNotes: Set<Int> = if (hasSelection && boardState.isNoteMode) {
        boardState.cells[boardState.selectedRow][boardState.selectedCol].notes
    } else emptySet()

    if (isWide) {
        // Wide layout: two rows — 1-5, then 6-9
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: 1-5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (number in 1..5) {
                    NumberButton(
                        number = number,
                        isSelected = number == selectedValue && number != 0,
                        isNoted = number in selectedNotes,
                        palette = palette,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // Row 2: 6-9 — centered as a group, same button width as Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                for (number in 6..9) {
                    NumberButton(
                        number = number,
                        isSelected = number == selectedValue && number != 0,
                        isNoted = number in selectedNotes,
                        palette = palette,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.weight(0.5f))
            }
        }
    } else {
        // Phone portrait: single row of 1-9
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (number in 1..9) {
                NumberButton(
                    number = number,
                    isSelected = number == selectedValue && number != 0,
                    isNoted = number in selectedNotes,
                    palette = palette,
                    onClick = { onNumberClick(number) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isSelected: Boolean,
    isNoted: Boolean = false,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highlighted = isSelected || isNoted
    val borderColor = if (highlighted) palette.boxBorder else Color.Transparent

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) palette.selectedCell else Color.Transparent)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 24.sp,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            color = if (highlighted) Color.White else palette.numPadColor,
            textAlign = TextAlign.Center
        )
    }
}
