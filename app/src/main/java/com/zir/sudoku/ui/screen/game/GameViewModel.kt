/**
 * ## GameViewModel — 游戏核心 ViewModel（约 500 行）
 *
 * **职责：** 数独游戏的全部业务逻辑中枢，管理棋盘状态、用户交互、计时存档、胜负判定。
 *
 * **核心数据结构：**
 * - `puzzle[9][9]`：初始谜题（给定数字）
 * - `solution[9][9]`：完整解答（用于提示和错误检测）
 * - `currentBoard[9][9]`：用户当前的填数状态
 * - `notes[9][9]`：每个格子的笔记（候选数集合）
 * - `undoStack` / `redoStack`：操作历史双栈
 *
 * **关键操作：**
 * - `selectCell(row, col)`：选中格子，更新高亮和关联格
 * - `inputNumber(num)`：正常模式填入数字 / 笔记模式切换候选数
 * - `erase()`：清除当前格数字和笔记
 * - `undo()` / `redo()`：撤销/重做，操作栈弹出/压入
 * - `hint()`：消耗提示次数，自动填入正确答案（优先选中格）
 * - `pause()` / `resume()`：暂停/恢复游戏，控制计时器
 *
 * **自动流程：**
 * - 每次操作 → `updateActiveState()` 重建 BoardState → 计算冲突 → 计数错误 → 检查完成/失败
 * - 每次操作 → `autoSave()` 序列化全量状态写入 Room GameSession
 * - 计时器协程每秒递增 elapsedSeconds 并推送 UI
 * - `onCleared()` 取消计时器协程
 *
 * **初始化：**
 * ```
 * init → load settings (highlightConflicts)
 *     → loadSavedGame=true  → loadGame() 从 Room 恢复
 *     → loadSavedGame=false → startNewGame() 调用 API/本地生成
 * ```
 *
 * **Factory：**
 * `GameViewModel.Factory(repository, difficulty, loadSavedGame)` 用于 Compose viewModel() 注入
 *
 * @see SudokuRepository
 * @see SudokuValidator
 * @see SudokuGenerator
 */
package com.zir.sudoku.ui.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zir.sudoku.data.local.entity.GameRecord
import com.zir.sudoku.data.local.entity.GameSession
import com.zir.sudoku.data.repository.SudokuRepository
import com.zir.sudoku.domain.engine.SudokuValidator
import com.zir.sudoku.domain.model.BoardState
import com.zir.sudoku.domain.model.Cell
import com.zir.sudoku.domain.model.Operation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: SudokuRepository,
    private val difficulty: String,
    private val loadSavedGame: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    // Core board data
    private var puzzle = Array(9) { IntArray(9) { 0 } }
    private var solution = Array(9) { IntArray(9) { 0 } }
    private var currentBoard = Array(9) { IntArray(9) { 0 } }
    private var notes = Array(9) { Array(9) { mutableSetOf<Int>() } }

    // UI state
    private var selectedRow = -1
    private var selectedCol = -1
    private var isNoteMode = false
    private var highlightConflicts = true
    private var errorLimitEnabled = true

    // History
    private val undoStack = mutableListOf<Operation>()
    private val redoStack = mutableListOf<Operation>()

    // Game state
    private var displayDifficulty = difficulty
    private var elapsedSeconds = 0
    private var hintCount = 3
    private val maxErrors = 3
    private val errorCells = mutableSetOf<Long>() // cumulative errors, never decrease
    private var timerJob: Job? = null
    private var timerStarted = false
    private var isPaused = false

    init {
        viewModelScope.launch {
            // Load settings
            repository.getSettings().first().let { settings ->
                highlightConflicts = settings.highlightConflicts
                errorLimitEnabled = settings.errorLimitEnabled
            }
            if (loadSavedGame) {
                loadGame()
            } else {
                startNewGame()
            }
        }
    }

    private suspend fun startNewGame() {
        _uiState.value = GameUiState.Loading
        val result = repository.fetchPuzzle(difficulty)
        result.fold(
            onSuccess = { (newPuzzle, newSolution) ->
                puzzle = newPuzzle
                solution = newSolution
                currentBoard = puzzle.map { it.copyOf() }.toTypedArray()
                notes = Array(9) { Array(9) { mutableSetOf() } }
                undoStack.clear()
                redoStack.clear()
                errorCells.clear()
                displayDifficulty = difficulty
                elapsedSeconds = 0
                hintCount = 3
                isPaused = false
                timerStarted = false
                // Record game start for statistics
                repository.incrementGamesStarted(displayDifficulty)
                updateActiveState()
            },
            onFailure = { e ->
                _uiState.value = GameUiState.Error(e.message ?: "生成谜题失败")
            }
        )
    }

    private suspend fun loadGame() {
        _uiState.value = GameUiState.Loading
        val session = repository.getActiveSessionOnce()
        if (session != null) {
            try {
                puzzle = repository.jsonToBoard(session.puzzleJson)
                solution = repository.jsonToBoard(session.solutionJson)
                currentBoard = repository.jsonToBoard(session.currentBoardJson)
                notes = repository.jsonToNotes(session.notesJson)
                    .map { row -> row.map { it.toMutableSet() }.toTypedArray() }
                    .toTypedArray()
                displayDifficulty = session.difficulty
                elapsedSeconds = session.elapsedSeconds
                hintCount = session.hintCount
                isPaused = session.isPaused

                // Deserialize operation stacks
                val opListType = object : TypeToken<List<Operation>>() {}.type
                val ops: List<Operation> = gson.fromJson(session.operationHistoryJson, opListType)
                undoStack.clear()
                undoStack.addAll(ops)

                val redoOps: List<Operation> = gson.fromJson(session.redoStackJson, opListType)
                redoStack.clear()
                redoStack.addAll(redoOps)

                // Rebuild cumulative error set from current board
                errorCells.clear()
                for (r in 0..8) {
                    for (c in 0..8) {
                        if (currentBoard[r][c] != 0 && currentBoard[r][c] != solution[r][c]) {
                            errorCells.add(r * 9L + c)
                        }
                    }
                }

                if (isPaused) {
                    val bs = buildBoardState()
                    _uiState.value = GameUiState.Paused(bs, elapsedSeconds)
                } else {
                    updateActiveState()
                    if (elapsedSeconds > 0 || undoStack.isNotEmpty()) {
                        startTimer()
                    }
                }
            } catch (e: Exception) {
                // Corrupted save, start new game
                startNewGame()
            }
        } else {
            // No saved game, start new
            val fallbackDifficulty = if (difficulty == "continue") "中等" else difficulty
            val result = repository.fetchPuzzle(fallbackDifficulty)
            result.fold(
                onSuccess = { (newPuzzle, newSolution) ->
                    puzzle = newPuzzle
                    solution = newSolution
                    currentBoard = puzzle.map { it.copyOf() }.toTypedArray()
                    errorCells.clear()
                    displayDifficulty = fallbackDifficulty
                    updateActiveState()
                },
                onFailure = { e ->
                    _uiState.value = GameUiState.Error(e.message ?: "生成谜题失败")
                }
            )
        }
    }

    // ---- User Actions ----

    fun selectCell(row: Int, col: Int) {
        if (_uiState.value !is GameUiState.Active) return
        selectedRow = row
        selectedCol = col
        updateActiveState()
    }

    fun inputNumber(number: Int) {
        val state = _uiState.value
        if (state !is GameUiState.Active) return
        if (selectedRow < 0 || selectedCol < 0) return
        if (puzzle[selectedRow][selectedCol] != 0) return // Can't modify given cells

        if (!timerStarted) {
            startTimer()
        }

        if (isNoteMode) {
            // Toggle note
            val previousNotes = notes[selectedRow][selectedCol].toSet()
            if (notes[selectedRow][selectedCol].contains(number)) {
                notes[selectedRow][selectedCol].remove(number)
            } else {
                // Clear the cell value if any and add note
                if (currentBoard[selectedRow][selectedCol] != 0) {
                    currentBoard[selectedRow][selectedCol] = 0
                }
                notes[selectedRow][selectedCol].add(number)
            }
            val newNotes = notes[selectedRow][selectedCol].toSet()
            undoStack.add(
                Operation(
                    row = selectedRow, col = selectedCol,
                    previousValue = currentBoard[selectedRow][selectedCol],
                    newValue = currentBoard[selectedRow][selectedCol],
                    previousNotes = previousNotes,
                    newNotes = newNotes
                )
            )
            redoStack.clear()
        } else {
            // Normal mode: fill number
            val previousValue = currentBoard[selectedRow][selectedCol]
            if (previousValue == number) return // Same number, no-op
            val previousNotes = notes[selectedRow][selectedCol].toSet()

            currentBoard[selectedRow][selectedCol] = number
            notes[selectedRow][selectedCol].clear()

            // Track cumulative error — never decreases on undo/erase
            if (number != solution[selectedRow][selectedCol]) {
                errorCells.add(selectedRow * 9L + selectedCol)
            }

            undoStack.add(
                Operation(
                    row = selectedRow, col = selectedCol,
                    previousValue = previousValue,
                    newValue = number,
                    previousNotes = previousNotes,
                    newNotes = emptySet()
                )
            )
            redoStack.clear()
        }

        updateActiveState()
        checkCompletion()
        autoSave()
    }

    fun erase() {
        val state = _uiState.value
        if (state !is GameUiState.Active) return
        if (selectedRow < 0 || selectedCol < 0) return
        if (puzzle[selectedRow][selectedCol] != 0) return

        val previousValue = currentBoard[selectedRow][selectedCol]
        val previousNotes = notes[selectedRow][selectedCol].toSet()

        if (previousValue == 0 && previousNotes.isEmpty()) return // Nothing to erase

        currentBoard[selectedRow][selectedCol] = 0
        notes[selectedRow][selectedCol].clear()

        undoStack.add(
            Operation(
                row = selectedRow, col = selectedCol,
                previousValue = previousValue,
                newValue = 0,
                previousNotes = previousNotes,
                newNotes = emptySet()
            )
        )
        redoStack.clear()

        if (!timerStarted) {
            startTimer()
        }

        updateActiveState()
        autoSave()
    }

    fun toggleNoteMode() {
        isNoteMode = !isNoteMode
        updateActiveState()
    }

    fun undo() {
        val state = _uiState.value
        if (state !is GameUiState.Active) return
        if (undoStack.isEmpty()) return

        val op = undoStack.removeLast()
        redoStack.add(
            Operation(
                row = op.row, col = op.col,
                previousValue = currentBoard[op.row][op.col],
                newValue = op.previousValue,
                previousNotes = notes[op.row][op.col].toSet(),
                newNotes = op.previousNotes
            )
        )

        currentBoard[op.row][op.col] = op.previousValue
        notes[op.row][op.col] = op.previousNotes.toMutableSet()

        updateActiveState()
        autoSave()
    }

    fun redo() {
        val state = _uiState.value
        if (state !is GameUiState.Active) return
        if (redoStack.isEmpty()) return

        val op = redoStack.removeLast()
        undoStack.add(
            Operation(
                row = op.row, col = op.col,
                previousValue = currentBoard[op.row][op.col],
                newValue = op.newValue,
                previousNotes = notes[op.row][op.col].toSet(),
                newNotes = op.newNotes
            )
        )

        currentBoard[op.row][op.col] = op.newValue
        notes[op.row][op.col] = op.newNotes.toMutableSet()

        updateActiveState()
        autoSave()
    }

    fun hint() {
        val state = _uiState.value
        if (state !is GameUiState.Active) return
        if (hintCount <= 0) return

        // Find an empty or wrong cell, prefer selected cell
        var targetRow: Int
        var targetCol: Int

        if (selectedRow in 0..8 && selectedCol in 0..8 &&
            currentBoard[selectedRow][selectedCol] != solution[selectedRow][selectedCol]
        ) {
            targetRow = selectedRow
            targetCol = selectedCol
        } else {
            // Find first empty/wrong cell
            var found = false
            targetRow = 0
            targetCol = 0
            for (r in 0..8) {
                for (c in 0..8) {
                    if (currentBoard[r][c] != solution[r][c]) {
                        targetRow = r
                        targetCol = c
                        found = true
                        break
                    }
                }
                if (found) break
            }
            if (!found) return // No empty/wrong cells (shouldn't happen)
        }

        val previousValue = currentBoard[targetRow][targetCol]
        val previousNotes = notes[targetRow][targetCol].toSet()
        val correctValue = solution[targetRow][targetCol]

        currentBoard[targetRow][targetCol] = correctValue
        notes[targetRow][targetCol].clear()

        undoStack.add(
            Operation(
                row = targetRow, col = targetCol,
                previousValue = previousValue,
                newValue = correctValue,
                previousNotes = previousNotes,
                newNotes = emptySet()
            )
        )
        redoStack.clear()

        hintCount--
        selectedRow = targetRow
        selectedCol = targetCol

        if (!timerStarted) {
            startTimer()
        }

        updateActiveState()
        checkCompletion()
        autoSave()
    }

    fun pause() {
        timerJob?.cancel()
        isPaused = true
        val bs = buildBoardState()
        _uiState.value = GameUiState.Paused(bs, elapsedSeconds)
        autoSave()
    }

    fun resume() {
        isPaused = false
        updateActiveState()
        if (timerStarted) {
            startTimer()
        }
    }

    fun saveAndExit() {
        // Don't save if game already ended — session was already deleted
        val currentState = _uiState.value
        if (currentState is GameUiState.Completed || currentState is GameUiState.Failed) {
            return
        }
        timerJob?.cancel()
        viewModelScope.launch {
            autoSave()
        }
    }

    // ---- Internal ----

    private fun startTimer() {
        timerJob?.cancel()
        timerStarted = true
        timerJob = viewModelScope.launch {
            try {
                while (true) {
                    delay(1000)
                    if (!isPaused) {
                        elapsedSeconds++
                        val s = _uiState.value
                        if (s is GameUiState.Active) {
                            _uiState.value = s.copy(elapsedSeconds = elapsedSeconds)
                        }
                    }
                }
            } catch (_: kotlinx.coroutines.CancellationException) { }
        }
    }

    private fun updateActiveState() {
        val errorCount = countErrors()
        val boardState = buildBoardState()
        _uiState.value = GameUiState.Active(
            boardState = boardState,
            difficulty = displayDifficulty,
            elapsedSeconds = elapsedSeconds,
            hintCount = hintCount,
            errorCount = errorCount,
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        )

        // Check for game over (only when error limit is enabled)
        if (errorLimitEnabled && errorCount >= maxErrors) {
            timerJob?.cancel()
            _uiState.value = GameUiState.Failed(
                timeSeconds = elapsedSeconds,
                difficulty = displayDifficulty,
                errorCount = errorCount,
                maxErrors = maxErrors
            )
            viewModelScope.launch {
                repository.recordGameLoss(displayDifficulty)
                repository.deleteActiveSession()
            }
        }
    }

    private fun countErrors(): Int = errorCells.size

    private fun buildBoardState(): BoardState {
        val conflicts = if (highlightConflicts) {
            SudokuValidator.findConflicts(currentBoard)
        } else {
            emptySet()
        }

        val sameValueHighlights = if (selectedRow in 0..8 && selectedCol in 0..8) {
            SudokuValidator.findSameValuePositions(currentBoard, selectedRow, selectedCol)
        } else {
            emptySet()
        }

        val cells = (0..8).map { row ->
            (0..8).map { col ->
                Cell(
                    row = row,
                    col = col,
                    value = currentBoard[row][col],
                    isGiven = puzzle[row][col] != 0,
                    notes = notes[row][col].toSet()
                )
            }
        }

        // Compute wrong-but-not-conflicted cells (text-error style)
        val wrongCells = mutableSetOf<Pair<Int, Int>>()
        for (r in 0..8) {
            for (c in 0..8) {
                val v = currentBoard[r][c]
                if (v != 0 && puzzle[r][c] == 0 && v != solution[r][c]) {
                    wrongCells.add(Pair(r, c))
                }
            }
        }

        return BoardState(
            cells = cells,
            selectedRow = selectedRow,
            selectedCol = selectedCol,
            conflicts = conflicts,
            sameValueHighlights = sameValueHighlights,
            wrongCells = wrongCells,
            isNoteMode = isNoteMode
        )
    }

    private fun checkCompletion() {
        if (SudokuValidator.isComplete(currentBoard)) {
            timerJob?.cancel()

            viewModelScope.launch {
                // Save record
                val record = GameRecord(
                    difficulty = difficulty,
                    timeSeconds = elapsedSeconds,
                    errorCount = errorCells.size
                )
                repository.saveRecord(record)

                // Record win for statistics
                repository.recordGameWin(difficulty)

                // Delete active session
                repository.deleteActiveSession()

                // Check if it's a new best
                val bestTime = repository.getBestTime(difficulty).first()
                val isNewBest = bestTime == null || elapsedSeconds <= bestTime

                _uiState.value = GameUiState.Completed(
                    timeSeconds = elapsedSeconds,
                    isNewBest = isNewBest,
                    difficulty = difficulty
                )
            }
        }
    }

    private fun autoSave() {
        viewModelScope.launch {
            try {
                val session = GameSession(
                    id = 1,
                    puzzleJson = repository.boardToJson(puzzle),
                    solutionJson = repository.boardToJson(solution),
                    currentBoardJson = repository.boardToJson(currentBoard),
                    notesJson = repository.notesToJson(
                        notes.map { row -> row.map { it.toSet() }.toTypedArray() }.toTypedArray()
                    ),
                    difficulty = displayDifficulty,
                    elapsedSeconds = elapsedSeconds,
                    hintCount = hintCount,
                    operationHistoryJson = gson.toJson(undoStack.toList()),
                    redoStackJson = gson.toJson(redoStack.toList()),
                    isPaused = isPaused
                )
                repository.saveSession(session)
            } catch (_: Exception) {
                // Silently fail on auto-save
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val repository: SudokuRepository,
        private val difficulty: String,
        private val loadSavedGame: Boolean = false
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameViewModel(repository, difficulty, loadSavedGame) as T
        }
    }
}
