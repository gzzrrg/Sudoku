/**
 * ## CompletionDialog — 通关庆祝弹窗
 *
 * **职责：**
 * - 在用户完成数独后全屏居中展示通关祝贺界面
 * - 显示用时（大号 MM:SS 格式）、难度标签
 * - 若为新纪录显示 "🏆 新纪录！" 标识
 * - 提供"再来一局"和"返回首页"两个后续操作按钮
 *
 * **交互限制：**
 * - `dismissOnBackPress = false`：不可通过返回键关闭
 * - `dismissOnClickOutside = false`：不可通过点击外部关闭
 * - 必须通过按钮明确选择下一步操作
 *
 * **视觉设计：**
 * - 圆角卡片（24dp）+ 白色/深色表面背景
 * - 🎉 大号庆祝 emoji（64sp）
 * - 主题色标题 + 大号等宽字体计时显示（48sp）
 * - 新纪录使用 tertiary 色文字突出显示
 */
package com.zir.sudoku.ui.screen.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CompletionDialog(
    timeSeconds: Int,
    isNewBest: Boolean,
    difficulty: String,
    onNewGame: () -> Unit,
    onGoHome: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🎉",
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "恭喜完成！",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$difficulty 难度",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                val minutes = timeSeconds / 60
                val seconds = timeSeconds % 60

                Text(
                    text = "用时",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isNewBest) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🏆 新纪录！",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onNewGame,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("再来一局", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("返回首页", fontSize = 16.sp)
                }
            }
        }
    }
}
