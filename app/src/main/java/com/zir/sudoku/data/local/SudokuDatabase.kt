/**
 * ## SudokuDatabase — Room 数据库
 *
 * **职责：**
 * - 定义 Room 数据库，包含 [GameSession] 和 [GameRecord] 两张表
 * - 提供 DAO 访问方法：`gameSessionDao()` 和 `gameRecordDao()`
 * - 使用双重检查锁（DCL）单例模式确保全局唯一实例
 *
 * **版本管理：**
 * - exportSchema = false（开发阶段不导出 schema 文件）
 * - 实体变更时需手动升级 version 或使用 destructive migration
 *
 * @see GameSessionDao
 * @see GameRecordDao
 */
package com.zir.sudoku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zir.sudoku.data.local.dao.GameRecordDao
import com.zir.sudoku.data.local.dao.GameSessionDao
import com.zir.sudoku.data.local.entity.GameRecord
import com.zir.sudoku.data.local.entity.GameSession

@Database(
    entities = [GameSession::class, GameRecord::class],
    version = 2,
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {

    abstract fun gameSessionDao(): GameSessionDao
    abstract fun gameRecordDao(): GameRecordDao

    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getInstance(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "sudoku_database"
                ).fallbackToDestructiveMigration(true)
                .build().also { INSTANCE = it }
            }
        }
    }
}
