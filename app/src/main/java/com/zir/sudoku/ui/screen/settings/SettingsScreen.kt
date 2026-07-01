/**
 * ## SettingsScreen — 设置页面
 *
 * **职责：**
 * - 展示和管理用户偏好设置，按 Material 3 列表规范排列
 * - 所有设置变更即时写入 DataStore 并响应式更新 UI
 *
 * **设置项列表：**
 * 1. **主题**：右侧显示当前值（浅色/深色/跟随系统），点击弹出 ModalBottomSheet 三选一
 *    - 选中项显示 ✓ 图标 + 主题色加粗
 * 2. **音效**：Switch 开关，右侧直接切换
 * 3. **冲突提示**：Switch 开关 + 副文本"错误输入时标红提醒"
 * 4. **清除数据**：红色警告文字，点击弹出 AlertDialog 确认
 *    - 确认后调用 `viewModel.clearAllData()` → 清除 Room + DataStore
 * 5. **版本**：只读显示 "v1.0.0"
 *
 * **组件复用：**
 * - `SettingsItem`：通用设置行组件（标题 + 副文本 + 尾部控件 + 可点击）
 * - `ThemeBottomSheet`：主题选择底部弹出面板
 *
 * @see SettingsViewModel
 */
package com.zir.sudoku.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zir.sudoku.ui.theme.LocalSudokuPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDebugColors: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    val palette = LocalSudokuPalette.current

    Scaffold(
        containerColor = palette.appBackground,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme setting
            SettingsItem(
                title = "主题",
                subtitle = when (uiState.themeMode) {
                    "light" -> "浅色"
                    "dark" -> "深色"
                    "white" -> "白色"
                    else -> "跟随系统"
                },
                onClick = { showThemeSheet = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sound setting
            SettingsItem(
                title = "音效",
                subtitle = null,
                trailing = {
                    Switch(
                        checked = uiState.soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Conflict highlight setting
            SettingsItem(
                title = "冲突提示",
                subtitle = "错误输入时标红提醒",
                trailing = {
                    Switch(
                        checked = uiState.highlightConflicts,
                        onCheckedChange = { viewModel.setHighlightConflicts(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Vibration setting
            SettingsItem(
                title = "震动",
                subtitle = "操作时震动反馈",
                trailing = {
                    Switch(
                        checked = uiState.vibrationEnabled,
                        onCheckedChange = { viewModel.setVibrationEnabled(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Score animation setting
            SettingsItem(
                title = "得分动画",
                subtitle = "完成时播放庆祝动画",
                trailing = {
                    Switch(
                        checked = uiState.scoreAnimationEnabled,
                        onCheckedChange = { viewModel.setScoreAnimationEnabled(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Error limit setting
            SettingsItem(
                title = "错误上限",
                subtitle = "开启后最多错误3次",
                trailing = {
                    Switch(
                        checked = uiState.errorLimitEnabled,
                        onCheckedChange = { viewModel.setErrorLimitEnabled(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Debug: color preview
            SettingsItem(
                title = "颜色调试",
                subtitle = "查看所有颜色变量与 Hex 值",
                onClick = onNavigateToDebugColors
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Clear data
            SettingsItem(
                title = "清除数据",
                subtitle = null,
                titleColor = Color(0xFFD32F2F),
                onClick = { showClearDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Version
            SettingsItem(
                title = "版本",
                subtitle = "v6.06.29",
                enabled = false
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Author
            SettingsItem(
                title = "作者",
                subtitle = "@gzzrrg",
                enabled = false
            )
        }
    }

    // Theme bottom sheet
    if (showThemeSheet) {
        ThemeBottomSheet(
            currentMode = uiState.themeMode,
            onSelect = { mode ->
                viewModel.setThemeMode(mode)
                showThemeSheet = false
            },
            onDismiss = { showThemeSheet = false }
        )
    }

    // Clear data confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("确认清除") },
            text = { Text("此操作将清除所有游戏记录和偏好设置，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDialog = false
                    }
                ) {
                    Text("确认", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeBottomSheet(
    currentMode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "选择主题",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            val options = listOf(
                "system" to "跟随系统",
                "light" to "浅色",
                "dark" to "深色",
                "white" to "白色"
            )
            options.forEach { (value, label) ->
                val isSelected = value == currentMode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(value) }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        label,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
