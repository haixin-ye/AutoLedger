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
    monthExpense: Double = 0.0,
    monthBudget: Double = 0.0,
    dayExpense: Double = 0.0,
    dayBudget: Double = 0.0,

    // ✨ 新增：是否是“精简/预览”模式 (给 BudgetSettingSheet 用)
    isPreviewMode: Boolean = false,

    // ✨ 新增：自定义日环中心文字 (用来显示日均可用金额 ¥xxx)
    customDayCenterText: String? = null
) {
    val safeMonthProgress = monthProgress.coerceIn(0f, 1f)
    val safeDayProgress = dayProgress.coerceIn(0f, 1f)

    val safeColor = Color(0xFF2ED573)    // 活力绿
    val warningColor = Color(0xFFFFC107) // 警告黄
    val dangerColor = Color(0xFFFF4757)  // 西瓜红

    // 月份颜色逻辑
    val monthColor = when {
        safeMonthProgress <= 0.5f -> safeColor
        safeMonthProgress <= 0.75f -> {
            val t = (safeMonthProgress - 0.5f) / 0.25f
            lerp(safeColor, warningColor, t)
        }

        else -> {
            val t = (safeMonthProgress - 0.75f) / 0.25f
            lerp(warningColor, dangerColor, t)
        }
    }

// 天数颜色逻辑
    val dayColor = when {
        safeDayProgress <= 0.5f -> safeColor
        safeDayProgress <= 0.75f -> {
            val t = (safeDayProgress - 0.5f) / 0.25f
            lerp(safeColor, warningColor, t)
        }

        else -> {
            val t = (safeDayProgress - 0.75f) / 0.25f
            lerp(warningColor, dangerColor, t)
        }
    }

    // 根据模式调整圆环尺寸
    val circleSize = if (isPreviewMode) 100.dp else 110.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // ========== 左侧：月预算仪表盘 ==========
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(circleSize)) {
                CircularProgressIndicator(
                    progress = { safeMonthProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = monthColor,
                    strokeWidth = 8.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(safeMonthProgress * 100).toInt()}%",
                        fontSize = if (isPreviewMode) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = monthColor
                    )
                    // 预览模式下不显示这行小字
                    if (!isPreviewMode) {
                        Text("月预算已用", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            // 底部文字动态切换
            if (isPreviewMode) {
                Text("月预算", color = Color.Gray, fontSize = 12.sp)
            } else {
                Text(
                    "${monthExpense.toInt()} / ${monthBudget.toInt()}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ========== 右侧：日限额仪表盘 ==========
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(circleSize)) {
                CircularProgressIndicator(
                    progress = { safeDayProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = dayColor, // ✨ 正确应用动态色！
                    strokeWidth = 8.dp,
                    trackColor = Color.LightGray.copy(alpha = 0.3f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val textToShow = customDayCenterText ?: "${(safeDayProgress * 100).toInt()}%"
                    Text(
                        text = textToShow,
                        fontSize = if (isPreviewMode) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = dayColor
                    )
                    if (!isPreviewMode) {
                        Text("日限额已用", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (isPreviewMode) {
                Text("日均可用", color = Color.Gray, fontSize = 12.sp)
            } else {
                Text(
                    "${dayExpense.toInt()} / ${dayBudget.toInt()}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
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
            Text(
                "${(progress.value * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}