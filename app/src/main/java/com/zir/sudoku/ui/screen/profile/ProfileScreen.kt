package com.zir.sudoku.ui.screen.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zir.sudoku.data.repository.SudokuRepository
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: SudokuRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(repository)
    )
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val palette = LocalSudokuPalette.current

    Scaffold(
        containerColor = palette.appBackground,
        topBar = {
            TopAppBar(
                title = { Text("我", color = palette.givenNumber) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.appBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Settings entry card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSettings() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = palette.iconColor,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        "设置",
                        fontSize = 16.sp,
                        color = palette.givenNumber,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = palette.statusGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Statistics section
            Text(
                "统计数据",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = palette.givenNumber
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (diff in listOf("简单", "中等", "困难")) {
                    FilterChip(
                        selected = uiState.selectedDifficulty == diff,
                        onClick = { vm.selectDifficulty(diff) },
                        label = { Text(diff) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.selectedCell,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated stats content
            AnimatedContent(
                targetState = uiState.selectedDifficulty,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                },
                label = "stats_animation"
            ) { currentDifficulty ->
                val stats = uiState.stats[currentDifficulty]
                if (stats != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Total games across all difficulties
                        StatCard(
                            label = "总游戏数",
                            value = "${uiState.totalGamesAllDifficulties}",
                            modifier = Modifier.fillMaxWidth(),
                            palette = palette
                        )

                        // Wins bar chart
                        WinsChart(
                            gamesStarted = stats.gamesStarted,
                            gamesWon = stats.gamesWon,
                            palette = palette
                        )

                        // Stats grid
                        StatsGrid(stats, palette)
                    }
                } else {
                    Text(
                        "加载中...",
                        color = palette.statusGray,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset stats button
            var showResetDialog by remember { mutableStateOf(false) }

            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("重置统计数据", color = Color(0xFFD32F2F), fontSize = 14.sp)
            }

            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("确认重置") },
                    text = { Text("将清除 ${uiState.selectedDifficulty} 难度的所有统计数据，是否继续？") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.resetStats(uiState.selectedDifficulty)
                                showResetDialog = false
                            }
                        ) {
                            Text("确认", color = Color(0xFFD32F2F))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(
    stats: DifficultyStatsUi,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Row 1: games started + games won
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "游戏开始数",
                value = "${stats.gamesStarted}",
                modifier = Modifier.weight(1f),
                palette = palette
            )
            StatCard(
                label = "游戏获胜数",
                value = "${stats.gamesWon}",
                modifier = Modifier.weight(1f),
                palette = palette
            )
        }

        // Row 2: win rate + error-free wins
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "胜率",
                value = "${(stats.winRate * 100).toInt()}%",
                modifier = Modifier.weight(1f),
                palette = palette
            )
            StatCard(
                label = "零错误获胜",
                value = "${stats.errorFreeWins}",
                modifier = Modifier.weight(1f),
                palette = palette
            )
        }

        // Row 3: current streak + max streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "当前连胜",
                value = "${stats.currentWinStreak}",
                modifier = Modifier.weight(1f),
                palette = palette
            )
            StatCard(
                label = "最高连胜",
                value = "${stats.maxWinStreak}",
                modifier = Modifier.weight(1f),
                palette = palette
            )
        }

        // Row 4: best time + average time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "最佳用时",
                value = if (stats.bestTime != null) formatTime(stats.bestTime) else "暂无记录",
                modifier = Modifier.weight(1f),
                palette = palette
            )
            StatCard(
                label = "平均用时",
                value = if (stats.averageTime != null) formatTime(stats.averageTime.toInt()) else "暂无记录",
                modifier = Modifier.weight(1f),
                palette = palette
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isPlaceholder = value.startsWith("暂无")
            Text(
                text = value,
                fontSize = if (isPlaceholder) 16.sp else 28.sp,
                fontWeight = if (isPlaceholder) FontWeight.Normal else FontWeight.Bold,
                color = if (isPlaceholder) palette.statusGray else palette.givenNumber,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = palette.statusGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WinsChart(
    gamesStarted: Int,
    gamesWon: Int,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surfaceBackground),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "游戏胜负统计",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = palette.givenNumber
            )
            Spacer(modifier = Modifier.height(12.dp))
            val maxVal = maxOf(gamesStarted, 1).toFloat()
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val barWidth = size.width * 0.25f
                val gap = barWidth * 0.3f
                val startX = (size.width - barWidth * 2 - gap) / 2
                val startedHeight = (gamesStarted / maxVal) * size.height
                val wonHeight = (gamesWon / maxVal) * size.height

                // Games started bar
                drawRoundRect(
                    color = palette.statusGray,
                    topLeft = Offset(startX, size.height - startedHeight),
                    size = Size(barWidth, startedHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Games won bar
                drawRoundRect(
                    color = palette.selectedCell,
                    topLeft = Offset(startX + barWidth + gap, size.height - wonHeight),
                    size = Size(barWidth, wonHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(palette.statusGray, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("开始: $gamesStarted", fontSize = 12.sp, color = palette.statusGray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(palette.selectedCell, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("获胜: $gamesWon", fontSize = 12.sp, color = palette.statusGray)
                }
            }
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
