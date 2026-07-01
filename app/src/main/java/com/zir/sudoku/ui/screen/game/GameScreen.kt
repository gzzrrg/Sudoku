/**
 * ## GameScreen — 游戏主页面（应用最复杂界面）
 *
 * **职责：**
 * - 组装游戏页全部 UI 组件并管理 6 种状态的界面渲染
 * - 适配手机竖屏（单栏）、手机横屏 & 平板（双栏）三种布局
 * - 处理暂停遮罩、通关弹窗、游戏结束提示
 *
 * **页面结构（手机竖屏，<600dp 宽）：**
 * ```
 * ┌──────────────────────────┐
 * │ ← TopAppBar      ⏸  ⚙️   │  ← 返回、暂停、设置
 * ├──────────────────────────┤
 * │ 简单    错误: 1/3   06:10 │  ← StatusBar（难度/错误/计时）
 * ├──────────────────────────┤
 * │                          │
 * │    ┌──┬──┬──┰──┬──┬──┐   │
 * │    │5 │3 │  ┃ 7│  │  │   │  ← SudokuBoard（9×9）
 * │    ├──┼──┼──╂──┼──┼──┤   │     thick border 每 3×3
 * │    │6 │  │  ┃ 1│9 │5 │   │
 * │    └──┴──┴──┸──┴──┴──┘   │
 * │                          │
 * ├──────────────────────────┤
 * │ 擦除 撤回 重做 笔记 提示    │ ← GameToolBar（5 按钮）
 * ├──────────────────────────┤
 * │ 1(7) 2(6) 3(8) ... 9(5)  │ ← NumberPad（1-9 横向）
 * └──────────────────────────┘
 * ```
 *
 * **页面结构（手机横屏 & 平板，≥600dp 宽）：**
 * ```
 * ┌────────────────────────┬──────────────────────────┐
 * │ ← TopAppBar            │ 简单 错误:1/3 06:10  ⏸ ⚙️ │ ← StatusBar 并入 TopAppBar
 * │                        ├──────────────────────────┤
 * │  ┌──┬──┬──┰──┬──┬──┐   │  🧹擦除  ↩撤回  ↪重做      │  ← GameToolBar
 * │  │5 │3 │  ┃ 7│  │  │   ├──────────────────────────┤
 * │  ├──┼──┼──╂──┼──┼──┤   │  ✏️笔记       💡提示      │
 * │  │6 │  │  ┃ 1│9 │5 │   ├──────────────────────────┤
 * │  └──┴──┴──┸──┴──┴──┘   │  1(7) 2(3) ... 5(6)      │ ← NumberPad（两行）
 * │                        ├──────────────────────────┤
 * │                        │     6(8) ... 9(5)        │
 * └────────────────────────┴──────────────────────────┘
 * ```
 *
 * @see ActiveGameLayout
 * @see StatusBar
 */
package com.zir.sudoku.ui.screen.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zir.sudoku.ui.screen.game.components.CompletionDialog
import com.zir.sudoku.ui.screen.game.components.GameToolBar
import com.zir.sudoku.ui.screen.game.components.NumberPad
import com.zir.sudoku.ui.screen.game.components.SudokuBoard
import com.zir.sudoku.ui.theme.LocalSudokuPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNewGame: (String) -> Unit,
    onGoHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalSudokuPalette.current

    DisposableEffect(Unit) {
        onDispose { viewModel.saveAndExit() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        when (val state = uiState) {
            is GameUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "生成谜题中...",
                        fontSize = 18.sp,
                        color = palette.givenNumber
                    )
                }
            }

            is GameUiState.Active -> {
                ActiveGameLayout(
                    boardState = state.boardState,
                    difficulty = state.difficulty,
                    elapsedSeconds = state.elapsedSeconds,
                    hintCount = state.hintCount,
                    errorCount = state.errorCount,
                    maxErrors = state.maxErrors,
                    isWide = isWide,
                    canUndo = state.canUndo,
                    canRedo = state.canRedo,
                    onCellClick = viewModel::selectCell,
                    onNumberClick = viewModel::inputNumber,
                    onHint = viewModel::hint,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onToggleNoteMode = viewModel::toggleNoteMode,
                    onErase = viewModel::erase,
                    onPause = viewModel::pause,
                    onBack = {
                        viewModel.saveAndExit()
                        onNavigateBack()
                    },
                    onSettings = onNavigateToSettings
                )
            }

            is GameUiState.Paused -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    ActiveGameLayout(
                        boardState = state.boardState,
                        difficulty = "",
                        elapsedSeconds = state.elapsedSeconds,
                        hintCount = 0,
                        errorCount = 0,
                        maxErrors = 3,
                        isWide = isWide,
                        canUndo = false,
                        canRedo = false,
                        onCellClick = { _, _ -> },
                        onNumberClick = {},
                        onHint = {},
                        onUndo = {},
                        onRedo = {},
                        onToggleNoteMode = {},
                        onErase = {},
                        onPause = {},
                        onBack = {
                            viewModel.saveAndExit()
                            onNavigateBack()
                        },
                        onSettings = onNavigateToSettings,
                        enabled = false
                    )

                    // Pause overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.pauseOverlay),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "游戏已暂停",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { viewModel.resume() }) {
                                Text("继续游戏", fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            is GameUiState.Completed -> {
                CompletionDialog(
                    timeSeconds = state.timeSeconds,
                    isNewBest = state.isNewBest,
                    difficulty = state.difficulty,
                    onNewGame = onGoHome,
                    onGoHome = onGoHome
                )
            }

            is GameUiState.Failed -> {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val compact = maxHeight < 500.dp
                    val minutes = state.timeSeconds / 60
                    val seconds = state.timeSeconds % 60
                    val timerText = String.format("%02d:%02d", minutes, seconds)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (compact) 0.65f else 0.85f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.surfaceBackground
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (compact) 20.dp else 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Trophy icon — gold color, doubled size
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "奖杯",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(if (compact) 96.dp else 128.dp)
                            )

                            Spacer(modifier = Modifier.height(if (compact) 8.dp else 16.dp))

                            // Failure title
                            Text(
                                "失败",
                                fontSize = if (compact) 22.sp else 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.conflictBackground
                            )

                            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

                            // Difficulty
                            Text(
                                text = state.difficulty,
                                fontSize = if (compact) 15.sp else 18.sp,
                                color = palette.givenNumber
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Time
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = palette.statusGray,
                                    modifier = Modifier.size(if (compact) 16.dp else 20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "用时 $timerText",
                                    fontSize = if (compact) 14.sp else 16.sp,
                                    color = palette.statusGray
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Error count
                            Text(
                                text = "错误：${state.errorCount}/${state.maxErrors}",
                                fontSize = if (compact) 14.sp else 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = palette.errorCountText
                            )

                            Spacer(modifier = Modifier.height(if (compact) 16.dp else 24.dp))

                            // Return button — use noteBtn for deeper color across themes
                            Button(
                                onClick = onGoHome,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = palette.noteBtn,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    "返回首页",
                                    fontSize = if (compact) 15.sp else 17.sp
                                )
                            }
                        }
                    }
                }
            }

            is GameUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.message,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("返回首页")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveGameLayout(
    boardState: com.zir.sudoku.domain.model.BoardState,
    difficulty: String,
    elapsedSeconds: Int,
    hintCount: Int,
    errorCount: Int,
    maxErrors: Int,
    isWide: Boolean,
    canUndo: Boolean = true,
    canRedo: Boolean = false,
    onCellClick: (Int, Int) -> Unit,
    onNumberClick: (Int) -> Unit,
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit = {},
    onToggleNoteMode: () -> Unit,
    onErase: () -> Unit,
    onPause: () -> Unit,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    enabled: Boolean = true
) {
    val palette = LocalSudokuPalette.current
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = palette.appBackground,
        topBar = {
            if (!isWide) {
                // Phone portrait: simple TopAppBar with empty title
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = palette.iconColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onPause) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "暂停",
                                tint = palette.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(onClick = onSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "设置",
                                tint = palette.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.appBackground
                    )
                )
            }
        }
    ) { padding ->
        if (isWide) {
            // Wide: three-column layout
            // Left: vertical status column — Center: Board — Right: GameToolBar + NumberPad
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 2.dp)
            ) {
                // Left: Vertical status bar — spread top to bottom
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top group: back
                    IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = palette.iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Difficulty
                    Text(text = difficulty, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = palette.statusGray)

                    // Error count
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "错误", fontSize = 13.sp, color = palette.statusGray)
                        Text(
                            text = "$errorCount/$maxErrors",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (errorCount > 0) palette.errorCountText else palette.statusGray
                        )
                    }

                    // Timer
                    Text(
                        text = timerText,
                        fontSize = 18.sp,
                        color = palette.givenNumber,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    // Bottom group: pause + settings
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = onPause, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Pause,
                                contentDescription = "暂停",
                                tint = palette.iconColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = onSettings, modifier = Modifier.size(44.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "设置",
                                tint = palette.iconColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Center: Board — fills remaining width
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val boardSize = minOf(maxWidth, maxHeight)
                    SudokuBoard(
                        boardState = boardState,
                        onCellClick = { r, c -> if (enabled) onCellClick(r, c) },
                        modifier = Modifier.size(boardSize)
                    )
                }

                // Right: Controls panel (weighted proportionally)
                Column(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Toolbar (2-row layout)
                    GameToolBar(
                        hintCount = hintCount,
                        isNoteMode = boardState.isNoteMode,
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onHint = { if (enabled) onHint() },
                        onUndo = { if (enabled) onUndo() },
                        onRedo = { if (enabled) onRedo() },
                        onToggleNoteMode = { if (enabled) onToggleNoteMode() },
                        onErase = { if (enabled) onErase() },
                        isWide = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Number pad (two rows)
                    NumberPad(
                        boardState = boardState,
                        onNumberClick = { n -> if (enabled) onNumberClick(n) },
                        isWide = true
                    )
                }
            }
        } else {
            // Phone portrait: single-column layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(start = 8.dp, end = 16.dp)
            ) {
                // Status bar
                StatusBar(
                    difficulty = difficulty,
                    errorCount = errorCount,
                    maxErrors = maxErrors,
                    timerText = timerText
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Board + toolbar + numpad — offset upward as a group by ~2 number button heights
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .offset(y = (-56).dp)
                ) {
                    // Board
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        SudokuBoard(
                            boardState = boardState,
                            onCellClick = { r, c -> if (enabled) onCellClick(r, c) },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Function buttons — centered with inward margins
                    GameToolBar(
                        hintCount = hintCount,
                        isNoteMode = boardState.isNoteMode,
                        canUndo = canUndo,
                        canRedo = canRedo,
                        onHint = { if (enabled) onHint() },
                        onUndo = { if (enabled) onUndo() },
                        onRedo = { if (enabled) onRedo() },
                        onToggleNoteMode = { if (enabled) onToggleNoteMode() },
                        onErase = { if (enabled) onErase() },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Number pad — centered with inward margins
                    NumberPad(
                        boardState = boardState,
                        onNumberClick = { n -> if (enabled) onNumberClick(n) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 0.dp).then(
                            Modifier.padding(bottom = 4.dp)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    difficulty: String,
    errorCount: Int,
    maxErrors: Int,
    timerText: String
) {
    val palette = LocalSudokuPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: difficulty label
        Text(
            text = difficulty,
            fontSize = 18.sp,
            color = palette.statusGray
        )

        // Center: error count
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "错误: ",
                fontSize = 20.sp,
                color = palette.statusGray
            )
            Text(
                text = "$errorCount/$maxErrors",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (errorCount > 0) palette.errorCountText else palette.statusGray
            )
        }

        // Right: timer
        Text(
            text = timerText,
            fontSize = 20.sp,
            color = palette.givenNumber,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

