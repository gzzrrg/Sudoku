/**
 * ## NavGraph + Routes — Compose Navigation 路由图
 *
 * **职责：**
 * - 定义应用三条导航路由及其参数传递方式
 * - 使用 Compose Navigation `NavHost` 管理页面切换和返回栈
 * - 每个路由节点创建对应的 ViewModel（通过 Factory 注入 Repository）
 *
 * **路由定义：**
 * - `home`：首页（Logo + 继续游戏 / 新游戏入口）
 * - `game/{difficulty}`：游戏页，`difficulty` 为路径参数
 *   - 有效值："简单" / "中等" / "困难" → 生成对应难度谜题
 *   - 特殊值："continue" → 加载上次未完成的存档
 * - `settings`：设置页面
 *
 * **ViewModel 创建模式：**
 * 每个 composable 路由内部通过 `viewModel(factory = ...)` 创建 ViewModel，
 * Factory 接收从 [AppContainer] 获取的 [SudokuRepository]
 *
 * @see HomeScreen
 * @see GameScreen
 * @see SettingsScreen
 */
package com.zir.sudoku.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zir.sudoku.data.repository.SudokuRepository
import com.zir.sudoku.ui.screen.game.GameScreen
import com.zir.sudoku.ui.screen.game.GameViewModel
import com.zir.sudoku.ui.screen.home.HomeScreen
import com.zir.sudoku.ui.screen.home.HomeViewModel
import com.zir.sudoku.ui.screen.debug.DebugColorScreen
import com.zir.sudoku.ui.screen.settings.SettingsScreen
import com.zir.sudoku.ui.screen.settings.SettingsViewModel

object Routes {
    const val HOME = "home"
    const val GAME = "game/{difficulty}"
    const val SETTINGS = "settings"
    const val DEBUG_COLORS = "debug_colors"

    fun game(difficulty: String) = "game/$difficulty"
}

@Composable
fun SudokuNavHost(
    navController: NavHostController,
    repository: SudokuRepository
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            HomeScreen(
                viewModel = vm,
                repository = repository,
                onContinueGame = {
                    navController.navigate(Routes.game("continue"))
                },
                onNewGame = { difficulty ->
                    navController.navigate(Routes.game(difficulty))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(Routes.GAME) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "中等"
            val loadSaved = difficulty == "continue"
            val vm: GameViewModel = viewModel(
                factory = GameViewModel.Factory(repository, difficulty, loadSaved)
            )
            GameScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNewGame = { newDifficulty ->
                    navController.navigate(Routes.game(newDifficulty)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onGoHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(repository)
            )
            SettingsScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDebugColors = {
                    navController.navigate(Routes.DEBUG_COLORS)
                }
            )
        }

        composable(Routes.DEBUG_COLORS) {
            DebugColorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
