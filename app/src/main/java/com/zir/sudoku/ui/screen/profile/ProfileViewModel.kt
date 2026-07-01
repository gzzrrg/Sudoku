package com.zir.sudoku.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zir.sudoku.data.local.datastore.SettingsDataStore
import com.zir.sudoku.data.repository.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileUiState(
    val selectedDifficulty: String = "简单",
    val stats: Map<String, DifficultyStatsUi> = emptyMap(),
    val isLoading: Boolean = true,
    val totalGamesAllDifficulties: Int = 0
)

data class DifficultyStatsUi(
    val gamesStarted: Int = 0,
    val gamesWon: Int = 0,
    val winRate: Float = 0f,
    val errorFreeWins: Int = 0,
    val currentWinStreak: Int = 0,
    val maxWinStreak: Int = 0,
    val bestTime: Int? = null,
    val averageTime: Double? = null
)

class ProfileViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadStats("简单")
    }

    fun selectDifficulty(difficulty: String) {
        loadStats(difficulty)
    }

    private fun loadStats(difficulty: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedDifficulty = difficulty)

            // Collect base stats from DataStore + Room
            combine(
                repository.getStats(difficulty),
                repository.getCountByDifficulty(difficulty),
                repository.getErrorFreeWinCount(difficulty)
            ) { stats, winCount, errorFree ->
                Triple(stats, winCount, errorFree)
            }.collect { (stats, winCount, errorFree) ->
                // Fetch best time, average time
                val bestTime = repository.getBestTime(difficulty).first()
                val avgTime = repository.getAverageTime(difficulty).first()
                // Compute total games started across all difficulties
                val easyStats = repository.getStats("简单").first()
                val mediumStats = repository.getStats("中等").first()
                val hardStats = repository.getStats("困难").first()
                val totalStarted = easyStats.gamesStarted + mediumStats.gamesStarted + hardStats.gamesStarted

                val started = stats.gamesStarted
                val winRate = if (started > 0) winCount.toFloat() / started else 0f
                val statsUi = DifficultyStatsUi(
                    gamesStarted = started,
                    gamesWon = winCount,
                    winRate = winRate,
                    errorFreeWins = errorFree,
                    currentWinStreak = stats.currentWinStreak,
                    maxWinStreak = stats.maxWinStreak,
                    bestTime = bestTime,
                    averageTime = avgTime
                )
                val currentMap = _uiState.value.stats.toMutableMap()
                currentMap[difficulty] = statsUi
                _uiState.value = _uiState.value.copy(
                    stats = currentMap,
                    isLoading = false,
                    totalGamesAllDifficulties = totalStarted
                )
            }
        }
    }

    fun resetStats(difficulty: String) {
        viewModelScope.launch {
            repository.resetStats(difficulty)
            loadStats(difficulty)
        }
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(repository) as T
        }
    }
}
