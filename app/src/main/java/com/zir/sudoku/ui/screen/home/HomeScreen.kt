package com.zir.sudoku.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zir.sudoku.data.repository.SudokuRepository
import com.zir.sudoku.ui.screen.profile.ProfileScreen
import com.zir.sudoku.ui.theme.LocalSudokuPalette
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    repository: SudokuRepository,
    onContinueGame: () -> Unit,
    onNewGame: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val palette = LocalSudokuPalette.current

    LaunchedEffect(Unit) {
        viewModel.checkActiveSession()
    }

    Scaffold(
        containerColor = palette.appBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            HomeBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                palette = palette
            )
        }
    ) { padding ->
        when (selectedTab) {
            0 -> {
                // Home tab — adapts to portrait / landscape
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val compact = maxHeight < 500.dp
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = if (compact) 16.dp else 32.dp)
                            .then(if (!compact) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = if (compact) Arrangement.SpaceEvenly else Arrangement.Center
                    ) {
                        // Logo
                        Text(
                            text = "数独",
                            fontSize = if (compact) 36.sp else 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = if (compact) 8.sp else 12.sp
                        )

                        Spacer(modifier = Modifier.height(if (compact) 4.dp else 16.dp))

                        // Best time card
                        if (uiState is HomeUiState.Ready) {
                                val bestTime = (uiState as HomeUiState.Ready).bestTimeSeconds
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = palette.appBackground
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(if (compact) 10.dp else 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "历史最快",
                                            fontSize = 14.sp,
                                            color = palette.statusGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = palette.iconColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (bestTime != null) {
                                                    formatTime(bestTime)
                                                } else {
                                                    "暂无历史最快数据"
                                                },
                                                fontSize = 14.sp,
                                                color = palette.statusGray
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(if (compact) 8.dp else 32.dp))
                        } else {
                            Spacer(modifier = Modifier.height(if (compact) 4.dp else 48.dp))
                        }

                        when (val state = uiState) {
                            is HomeUiState.Loading -> {
                                Text(
                                    "加载中...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            is HomeUiState.Ready -> {
                                if (compact) {
                                    // Landscape: buttons side by side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                if (state.hasActiveSession) {
                                                    onContinueGame()
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("当前没有进行中的游戏")
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("继续游戏", fontSize = 16.sp)
                                        }

                                        Button(
                                            onClick = { showDifficultyDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text("开始新游戏", fontSize = 16.sp)
                                        }
                                    }
                                } else {
                                    // Portrait: buttons stacked vertically
                                    OutlinedButton(
                                        onClick = {
                                            if (state.hasActiveSession) {
                                                onContinueGame()
                                            } else {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("当前没有进行中的游戏")
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("继续游戏", fontSize = 18.sp)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { showDifficultyDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Text("开始新游戏", fontSize = 18.sp)
                                    }
                                }
                            }

                            is HomeUiState.Error -> {
                                Text(
                                    state.message,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Profile tab
                ProfileScreen(
                    repository = repository,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateBack = {}
                )
            }
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            onDismiss = { showDifficultyDialog = false },
            onSelect = { difficulty ->
                showDifficultyDialog = false
                onNewGame(difficulty)
            }
        )
    }
}

@Composable
private fun HomeBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.surfaceBackground)
    ) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Wide: icon+label side-by-side pills, spread across full width
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WideTab(
                    icon = Icons.Filled.Home,
                    label = "主页",
                    isSelected = selectedTab == 0,
                    palette = palette,
                    onClick = { onTabSelected(0) }
                )
                WideTab(
                    icon = Icons.Filled.Person,
                    label = "我",
                    isSelected = selectedTab == 1,
                    palette = palette,
                    onClick = { onTabSelected(1) }
                )
            }
        } else {
            // Portrait: standard NavigationBar
            NavigationBar(
                containerColor = palette.surfaceBackground,
                contentColor = palette.iconColor
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "主页",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("主页", fontSize = 14.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.selectedCell,
                        selectedTextColor = palette.selectedCell,
                        unselectedIconColor = palette.statusGray,
                        unselectedTextColor = palette.statusGray,
                        indicatorColor = palette.selectedCell.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "我",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    label = { Text("我", fontSize = 14.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.selectedCell,
                        selectedTextColor = palette.selectedCell,
                        unselectedIconColor = palette.statusGray,
                        unselectedTextColor = palette.statusGray,
                        indicatorColor = palette.selectedCell.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}

@Composable
private fun WideTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    palette: com.zir.sudoku.ui.theme.SudokuColorPalette,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) palette.selectedCell.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) palette.selectedCell else palette.statusGray

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun formatTime(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DifficultyDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val palette = LocalSudokuPalette.current
    val difficulties = listOf("简单", "中等", "困难")
    var selected by remember { mutableStateOf("中等") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surfaceBackground,
        titleContentColor = palette.givenNumber,
        title = {
            Text(
                "选择难度",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = palette.givenNumber
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                difficulties.forEach { difficulty ->
                    val isSelected = difficulty == selected
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.background(palette.selectedCell)
                                } else {
                                    Modifier
                                        .background(palette.surfaceBackground)
                                        .border(1.5.dp, palette.gridLine, RoundedCornerShape(12.dp))
                                }
                            )
                            .clickable { selected = difficulty }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            difficulty,
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) palette.selectedOnHighlight
                            else palette.givenNumber
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelect(selected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.selectedCell,
                    contentColor = palette.selectedOnHighlight
                )
            ) {
                Text("开始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = palette.statusGray)
            }
        }
    )
}
