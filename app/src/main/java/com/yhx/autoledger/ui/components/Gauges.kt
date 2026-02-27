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
import com.yhx.autoledger.ui.theme.AppDesignSystem // ✨ 引入全局主题

@Composable
fun DoubleCircleGauges(
    monthProgress: Float,
    dayProgress: Float,
    monthExpense: Double = 0.0,
    monthBudget: Double = 0.0,
    dayExpense: Double = 0.0,
    dayBudget: Double = 0.0,
    isPreviewMode: Boolean = false,
    customDayCenterText: String? = null
) {
    val safeMonthProgress = monthProgress.coerceIn(0f, 1f)
    val safeDayProgress = dayProgress.coerceIn(0f, 1f)

    // ✨ 使用专属的仪表盘健康度颜色
    val safeColor = AppDesignSystem.colors.gaugeSafe
    val warningColor = AppDesignSystem.colors.gaugeWarning
    val dangerColor = AppDesignSystem.colors.gaugeDanger

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
                    // ✨ 映射专属底轨色
                    trackColor = AppDesignSystem.colors.gaugeTrack,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(safeMonthProgress * 100).toInt()}%",
                        fontSize = if (isPreviewMode) 18.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = monthColor
                    )
                    if (!isPreviewMode) {
                        // ✨ 映射全局次要文本
                        Text("月预算已用", fontSize = 10.sp, color = AppDesignSystem.colors.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (isPreviewMode) {
                // ✨ 映射全局次要文本
                Text("月预算已用", color = AppDesignSystem.colors.textSecondary, fontSize = 12.sp)
            } else {
                Text(
                    "${monthExpense.toInt()} / ${monthBudget.toInt()}",
                    fontSize = 12.sp,
                    // ✨ 映射全局次要文本
                    color = AppDesignSystem.colors.textSecondary,
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
                    color = dayColor,
                    strokeWidth = 8.dp,
                    // ✨ 映射专属底轨色
                    trackColor = AppDesignSystem.colors.gaugeTrack,
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
                        // ✨ 映射全局次要文本
                        Text("日限额已用", fontSize = 10.sp, color = AppDesignSystem.colors.textSecondary)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (isPreviewMode) {
                // ✨ 映射全局次要文本
                Text("日均可用", color = AppDesignSystem.colors.textSecondary, fontSize = 12.sp)
            } else {
                Text(
                    "${dayExpense.toInt()} / ${dayBudget.toInt()}",
                    fontSize = 12.sp,
                    // ✨ 映射全局次要文本
                    color = AppDesignSystem.colors.textSecondary,
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
                // ✨ 映射专属底轨色
                color = AppDesignSystem.colors.gaugeTrack,
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
            // ✨ 必须显式赋予主文本颜色，防止深色模式下变成黑底黑字看不见
            Text(
                "${(progress.value * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppDesignSystem.colors.textPrimary
            )
        }
        Spacer(Modifier.height(8.dp))
        // ✨ 映射全局次要文本
        Text(label, fontSize = 12.sp, color = AppDesignSystem.colors.textSecondary)
    }
}