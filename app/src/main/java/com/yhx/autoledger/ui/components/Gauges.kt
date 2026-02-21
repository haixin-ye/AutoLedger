package com.yhx.autoledger.ui.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DoubleCircleGauges(
    monthProgress: Float,
    dayProgress: Float,
    // ✨ 新增参数：接收真实花费和预算数据
    monthExpense: Double = 0.0,
    monthBudget: Double = 0.0,
    dayExpense: Double = 0.0,
    dayBudget: Double = 0.0
) {
    // 确保进度在 0~1 之间，防止颜色计算越界
    val safeMonthProgress = monthProgress.coerceIn(0f, 1f)
    val safeDayProgress = dayProgress.coerceIn(0f, 1f)

    // ✨ 颜色渐变魔法：根据进度从 绿色 丝滑过渡到 红色
    val safeColor = Color(0xFF2ED573)   // 活力薄荷绿
    val dangerColor = Color(0xFFFF4757) // 现代西瓜红
    val monthColor = lerp(safeColor, dangerColor, safeMonthProgress)
    val dayColor = lerp(safeColor, dangerColor, safeDayProgress)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // ========== 左侧：月预算仪表盘 ==========
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                CircularProgressIndicator(
                    progress = { safeMonthProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = monthColor, // 应用渐变色
                    strokeWidth = 8.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(safeMonthProgress * 100).toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = monthColor)
                    Text("月预算已用", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
            // ✨ 底部数据明细：花费 / 总额
            Text("${monthExpense.toInt()} / ${monthBudget.toInt()}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }

        // ========== 右侧：日限额仪表盘 ==========
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                CircularProgressIndicator(
                    progress = { safeDayProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = dayColor, // 应用渐变色
                    strokeWidth = 8.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(safeDayProgress * 100).toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dayColor)
                    Text("日限额已用", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
            // ✨ 底部数据明细：花费 / 每日限额
            Text("${dayExpense.toInt()} / ${dayBudget.toInt()}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AnimatedCircleItem(label: String, targetProgress: Float, color: Color) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(targetProgress) {
        progress.animateTo(targetProgress, tween(1500, easing = FastOutSlowInEasing))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
            // 底色圈
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray.copy(alpha = 0.2f),
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round,
            )
            // 进度圈
            CircularProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round,
            )
            Text("${(progress.value * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}