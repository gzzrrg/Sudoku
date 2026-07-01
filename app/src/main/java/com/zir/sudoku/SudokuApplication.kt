/**
 * ## SudokuApplication — 应用级入口
 *
 * **职责：**
 * - 继承 [Application]，在 [onCreate] 中初始化 [AppContainer] 依赖注入容器
 * - 使 Repository 等核心依赖在应用整个生命周期内以单例形式存在
 * - 在 AndroidManifest.xml 中通过 `android:name=".SudokuApplication"` 注册
 *
 * **使用方式：**
 * Activity 中通过 `(application as SudokuApplication).appContainer.repository` 获取依赖
 *
 * @see AppContainer
 */
package com.zir.sudoku

import android.app.Application
import com.zir.sudoku.di.AppContainer

class SudokuApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
