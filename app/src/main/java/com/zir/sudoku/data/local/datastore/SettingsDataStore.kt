/**
 * ## SettingsDataStore — 用户偏好设置存储 + 游戏统计
 *
 * **职责：**
 * - 基于 Jetpack Preferences DataStore 管理用户偏好配置和游戏统计数据
 * - 所有读取暴露为 `Flow<UserSettings>` 和 `Flow<DifficultyStats>`，写入为挂起函数
 * - 存储项：主题模式、冲突提示、音效、震动、得分动画、错误上限、默认难度
 * - 统计项：各难度游戏开始数、当前连胜、最高连胜
 *
 * **设计要点：**
 * - 使用顶层扩展属性 `Context.dataStore` 确保全局单例
 * - `UserSettings` / `DifficultyStats` 内嵌数据类提供默认值，避免 null 处理
 * - `clearAll()` 清除所有偏好键值（含统计数据）
 */
package com.zir.sudoku.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        // Settings keys
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_HIGHLIGHT_CONFLICTS = booleanPreferencesKey("highlight_conflicts")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val KEY_SCORE_ANIMATION_ENABLED = booleanPreferencesKey("score_animation_enabled")
        private val KEY_ERROR_LIMIT_ENABLED = booleanPreferencesKey("error_limit_enabled")
        private val KEY_DEFAULT_DIFFICULTY = stringPreferencesKey("default_difficulty")

        // Stats keys per difficulty
        private fun gamesStartedKey(difficulty: String) = intPreferencesKey("games_started_$difficulty")
        private fun currentStreakKey(difficulty: String) = intPreferencesKey("current_streak_$difficulty")
        private fun maxStreakKey(difficulty: String) = intPreferencesKey("max_streak_$difficulty")

        val DIFFICULTIES = listOf("简单", "中等", "困难")
    }

    data class UserSettings(
        val themeMode: String = "system",
        val highlightConflicts: Boolean = true,
        val soundEnabled: Boolean = true,
        val vibrationEnabled: Boolean = true,
        val scoreAnimationEnabled: Boolean = true,
        val errorLimitEnabled: Boolean = true,
        val defaultDifficulty: String = "中等"
    )

    data class DifficultyStats(
        val gamesStarted: Int = 0,
        val currentWinStreak: Int = 0,
        val maxWinStreak: Int = 0
    )

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            themeMode = prefs[KEY_THEME_MODE] ?: "system",
            highlightConflicts = prefs[KEY_HIGHLIGHT_CONFLICTS] ?: true,
            soundEnabled = prefs[KEY_SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[KEY_VIBRATION_ENABLED] ?: true,
            scoreAnimationEnabled = prefs[KEY_SCORE_ANIMATION_ENABLED] ?: true,
            errorLimitEnabled = prefs[KEY_ERROR_LIMIT_ENABLED] ?: true,
            defaultDifficulty = prefs[KEY_DEFAULT_DIFFICULTY] ?: "中等"
        )
    }

    fun getStats(difficulty: String): Flow<DifficultyStats> = context.dataStore.data.map { prefs ->
        DifficultyStats(
            gamesStarted = prefs[gamesStartedKey(difficulty)] ?: 0,
            currentWinStreak = prefs[currentStreakKey(difficulty)] ?: 0,
            maxWinStreak = prefs[maxStreakKey(difficulty)] ?: 0
        )
    }

    // ---- Settings writers ----

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setHighlightConflicts(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HIGHLIGHT_CONFLICTS] = enabled }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VIBRATION_ENABLED] = enabled }
    }

    suspend fun setScoreAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SCORE_ANIMATION_ENABLED] = enabled }
    }

    suspend fun setErrorLimitEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ERROR_LIMIT_ENABLED] = enabled }
    }

    suspend fun setDefaultDifficulty(difficulty: String) {
        context.dataStore.edit { it[KEY_DEFAULT_DIFFICULTY] = difficulty }
    }

    // ---- Stats writers ----

    suspend fun incrementGamesStarted(difficulty: String) {
        context.dataStore.edit { prefs ->
            val key = gamesStartedKey(difficulty)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun recordGameWin(difficulty: String) {
        context.dataStore.edit { prefs ->
            val currKey = currentStreakKey(difficulty)
            val maxKey = maxStreakKey(difficulty)
            val newStreak = (prefs[currKey] ?: 0) + 1
            prefs[currKey] = newStreak
            val currentMax = prefs[maxKey] ?: 0
            if (newStreak > currentMax) {
                prefs[maxKey] = newStreak
            }
        }
    }

    suspend fun recordGameLoss(difficulty: String) {
        context.dataStore.edit { prefs ->
            prefs[currentStreakKey(difficulty)] = 0
        }
    }

    suspend fun resetStats(difficulty: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(gamesStartedKey(difficulty))
            prefs.remove(currentStreakKey(difficulty))
            prefs.remove(maxStreakKey(difficulty))
        }
    }

    suspend fun resetAllStats() {
        context.dataStore.edit { prefs ->
            for (d in DIFFICULTIES) {
                prefs.remove(gamesStartedKey(d))
                prefs.remove(currentStreakKey(d))
                prefs.remove(maxStreakKey(d))
            }
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
