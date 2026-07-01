/**
 * ## GameToolBar — 游戏工具条
 *
 * **职责：**
 * - 提供 5 个游戏功能按钮：擦除、撤回、重做、笔记、提示
 * - 每个按钮为彩色圆角方块图标 + 下方文字标签的纵向布局
 * - 笔记按钮激活时显示白色边框指示
 * - 提示按钮带红色小圆点角标（剩余次数）
 * - 不可用按钮呈现低透明度态
 * - 支持宽屏模式（手机横屏/平板）：2行布局（擦除/撤回/重做 + 笔记/提示）
 *
 * **按钮说明：**
 * | 按钮 | 图标 | 图标背景色 | 禁用条件 |
 * |------|------|-----------|----------|
 * | 擦除 | ⌫ | eraseBtn | 永远可用 |
 * | 撤回 | ↩ | undoBtn | 无操作可撤回 |
 * | 重做 | ↪ | undoBtn | 无操作可重做 |
 * | 笔记 | ✏️ | noteBtn | 永远可用 |
 * | 提示 | 💡 | hintBtn | 提示次数用尽 |
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@Composable
fun GameToolBar(
    hintCount: Int,
    isNoteMode: Boolean,
    canUndo: Boolean,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onToggleNoteMode: () -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
    canRedo: Boolean = false,
    onRedo: () -> Unit = {},
    isWide: Boolean = false
) {
    val palette = LocalSudokuPalette.current

    if (isWide) {
        // Wide layout: 2-row grid in right panel
        // Row 1: Erase / Undo / Redo
        // Row 2: Notes / Hint
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    label = "擦除",
                    onClick = onErase,
                    large = true
                )
                ToolButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "撤回",
                    onClick = onUndo,
                    enabled = canUndo,
                    large = true
                )
                ToolButton(
                    icon = Icons.AutoMirrored.Filled.Redo,
                    label = "重做",
                    onClick = onRedo,
                    enabled = canRedo,
                    large = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolButton(
                    icon = Icons.Filled.Edit,
                    label = "笔记",
                    onClick = onToggleNoteMode,
                    isActive = isNoteMode,
                    large = true
                )
                ToolButton(
                    icon = Icons.Filled.Lightbulb,
                    label = "提示",
                    onClick = onHint,
                    enabled = hintCount > 0,
                    badge = if (hintCount > 0) "$hintCount" else null,
                    large = true
                )
            }
        }
    } else {
        // Phone portrait: single row of 5 buttons
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolButton(
                icon = Icons.AutoMirrored.Filled.Backspace,
                label = "擦除",
                onClick = onErase,

            )

            ToolButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                label = "撤回",
                onClick = onUndo,
                enabled = canUndo,

            )

            ToolButton(
                icon = Icons.AutoMirrored.Filled.Redo,
                label = "重做",
                onClick = onRedo,
                enabled = canRedo,

            )

            ToolButton(
                icon = Icons.Filled.Edit,
                label = "笔记",
                onClick = onToggleNoteMode,
                isActive = isNoteMode,

            )

            ToolButton(
                icon = Icons.Filled.Lightbulb,
                label = "提示",
                onClick = onHint,
                enabled = hintCount > 0,

                badge = if (hintCount > 0) "$hintCount" else null
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isActive: Boolean = false,
    badge: String? = null,
    large: Boolean = false
) {
    val palette = LocalSudokuPalette.current

    val contentAlpha = if (enabled) 1f else 0.35f

    // Consistent button color: all buttons use the same background color per theme
    val effectiveBg = when {
        isActive && enabled -> palette.selectedCell
        else -> palette.noteBtn.copy(alpha = contentAlpha)
    }

    val iconSize = if (large) 44.dp else 40.dp
    val iconInner = if (large) 24.dp else 22.dp
    val labelSize = if (large) 13.sp else 13.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = if (large) 6.dp else 8.dp, vertical = 4.dp)
    ) {
        // Icon container
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            // Background layer (clipped to rounded square)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(effectiveBg)
                    .then(
                        if (isActive && enabled) Modifier.border(2.dp, palette.boxBorder, RoundedCornerShape(12.dp))
                        else Modifier
                    )
            )
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = contentAlpha),
                modifier = Modifier.size(iconInner)
            )
            // Hint badge (unclipped, extends beyond button bounds)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(palette.errorCountText),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = label,
            fontSize = labelSize,
            fontWeight = FontWeight.Medium,
            color = palette.labelColor.copy(alpha = contentAlpha),
            textAlign = TextAlign.Center
        )
    }
}
