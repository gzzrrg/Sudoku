/**
 * ## DebugColorScreen — 颜色调试预览页面
 *
 * **职责：**
 * - 以可视色块形式展示所有 Material 主题颜色和数独棋盘颜色
 * - 每项显示：40dp 色块 + 中文标签 + Hex 颜色值
 * - 便于开发者修改 Color.kt 后立即查看对应标签的颜色变化
 *
 * **使用方式：**
 * 设置 → 颜色调试 → 查看所有颜色 → 修改 Color.kt → rebuild → 回到此页验证
 */
package com.zir.sudoku.ui.screen.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugColorScreen(
    onNavigateBack: () -> Unit
) {
    val palette = LocalSudokuPalette.current
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("颜色调试预览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = palette.iconColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // ============================================
            // Section 1: Material Theme Colors
            // ============================================
            item { SectionHeader("Material 主题颜色") }

            val materialColors = listOf(
                "主色 Primary" to colorScheme.primary,
                "主色上文字 On Primary" to colorScheme.onPrimary,
                "主色容器 Primary Container" to colorScheme.primaryContainer,
                "主色容器上文字 On Primary Container" to colorScheme.onPrimaryContainer,
                "次要色 Secondary" to colorScheme.secondary,
                "次要色上文字 On Secondary" to colorScheme.onSecondary,
                "次要色容器 Secondary Container" to colorScheme.secondaryContainer,
                "次要色容器上文字 On Secondary Container" to colorScheme.onSecondaryContainer,
                "第三色 Tertiary" to colorScheme.tertiary,
                "第三色上文字 On Tertiary" to colorScheme.onTertiary,
                "背景 Background" to colorScheme.background,
                "背景上文字 On Background" to colorScheme.onBackground,
                "表面 Surface" to colorScheme.surface,
                "表面上文字 On Surface" to colorScheme.onSurface,
                "表面变体 Surface Variant" to colorScheme.surfaceVariant,
                "表面变体上文字 On Surface Variant" to colorScheme.onSurfaceVariant,
                "错误 Error" to colorScheme.error,
                "错误上文字 On Error" to colorScheme.onError,
                "边框 Outline" to colorScheme.outline,
            )
            materialColors.forEach { (label, color) ->
                item { ColorSwatch(label, color) }
            }

            // ============================================
            // Section 2: Sudoku Board Colors
            // ============================================
            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader("数独棋盘颜色") }

            val boardColors = listOf(
                "宫格细线 gridLine" to palette.gridLine,
                "宫格粗线 boxBorder" to palette.boxBorder,
                "棋盘背景 boardBackground" to palette.boardBackground,
                "空格背景 cellEmpty" to palette.cellEmpty,
                "预填格背景 cellGivenBg" to palette.cellGivenBg,
                "选中格高亮 selectedCell" to palette.selectedCell,
                "同数聚焦 sameValueHighlight" to palette.sameValueHighlight,
                "关联格高亮 relatedCellHighlight" to palette.relatedCellHighlight,
                "预填数字 givenNumber" to palette.givenNumber,
                "用户数字 userNumber" to palette.userNumber,
                "笔记文字 noteText" to palette.noteText,
                "冲突背景 conflictBackground" to palette.conflictBackground,
                "冲突文字 conflictText" to palette.conflictText,
                "高亮上文字 selectedOnHighlight" to palette.selectedOnHighlight,
                "错误文字 textError" to palette.textError,
                "按钮-擦除 eraseBtn" to palette.eraseBtn,
                "按钮-撤回 undoBtn" to palette.undoBtn,
                "按钮-笔记 noteBtn" to palette.noteBtn,
                "按钮-提示 hintBtn" to palette.hintBtn,
                "按钮标签 labelColor" to palette.labelColor,
                "数字键盘 numPadColor" to palette.numPadColor,
                "图标颜色 iconColor" to palette.iconColor,
                "错误计数 errorCountText" to palette.errorCountText,
                "状态灰字 statusGray" to palette.statusGray,
                "右侧面板 rightPanelBg" to palette.rightPanelBg,
                "全局背景 appBackground" to palette.appBackground,
                "表面背景 surfaceBackground" to palette.surfaceBackground,
                "暂停遮罩 pauseOverlay" to palette.pauseOverlay,
            )
            boardColors.forEach { (label, color) ->
                item { ColorSwatch(label, color) }
            }

            // Bottom spacer
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color square
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(6.dp)
                )
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                colorToHex(color),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun colorToHex(color: Color): String {
    val r = (color.red * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue * 255).toInt()
    val a = (color.alpha * 255).toInt()
    return if (a == 255) {
        String.format("#%02X%02X%02X", r, g, b)
    } else {
        String.format("#%02X%02X%02X%02X", r, g, b, a)
    }
}
