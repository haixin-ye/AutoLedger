package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.ui.theme.AppDesignSystem
import kotlin.math.max

@Composable
fun getPremiumBrush(index: Int): Brush {
    val palette = AppDesignSystem.colors.donutChartPalette
    val colors = palette[index % palette.size]
    return Brush.linearGradient(listOf(colors.first, colors.second))
}

@Composable
fun getPremiumBaseColor(index: Int): Color {
    val palette = AppDesignSystem.colors.donutChartPalette
    return palette[index % palette.size].first
}

@Composable
fun PremiumDonutChart(data: List<CategoryPercentage>, totalExpense: String) {
    // ✨ 1. 核心逻辑：动态计算缩放比例
    // 假设理想状态下展示 5 个条目是 100% 大小。
    // 如果数据超过 5 个，就开始动态计算缩小比例。设置一个极限最小值防瞎眼（例如最小缩放到 0.55 倍）
    val scale = remember(data.size) {
        if (data.size <= 5) 1f else max(0.55f, 5f / data.size)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // 高度依然死死锁定，保证布局不跳
            .padding(horizontal = 8.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ================= 左侧：霸气圆环 (保持不变) =================
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1.3f)
        ) {
            val trackColor = AppDesignSystem.colors.donutChartTrack
            val brushes = data.mapIndexed { index, _ -> getPremiumBrush(index) }
            val singleBrush = if (data.size == 1) getPremiumBrush(0) else null

            androidx.compose.foundation.Canvas(modifier = Modifier.size(210.dp)) {
                val strokeWidthPx = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidthPx) / 2f
                val circumference = 2f * Math.PI.toFloat() * radius
                val capAngle = (strokeWidthPx / circumference) * 360f
                val visualGapAngle = 2f
                val totalOffsetAngle = capAngle + visualGapAngle

                drawCircle(
                    color = trackColor,
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
                )

                var currentStartAngle = -90f

                if (data.size == 1 && singleBrush != null) {
                    drawArc(
                        brush = singleBrush,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx),
                        topLeft = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                    )
                } else {
                    data.forEachIndexed { index, item ->
                        val rawSweep = item.percentage * 360f
                        if (rawSweep > totalOffsetAngle) {
                            val actualSweep = rawSweep - totalOffsetAngle
                            val actualStart = currentStartAngle + (totalOffsetAngle / 2f)
                            drawArc(
                                brush = brushes[index],
                                startAngle = actualStart,
                                sweepAngle = actualSweep,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                            )
                        } else if (rawSweep > 0f) {
                            drawArc(
                                brush = brushes[index],
                                startAngle = currentStartAngle + (visualGapAngle / 2f),
                                sweepAngle = maxOf(0.5f, rawSweep - visualGapAngle),
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Butt),
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                            )
                        }
                        currentStartAngle += rawSweep
                    }
                }
            }

            // 中心金额文字
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "总支出",
                    fontSize = 12.sp,
                    color = AppDesignSystem.colors.donutChartTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppDesignSystem.colors.donutChartTextPrimary,
                        modifier = Modifier.padding(bottom = 2.dp, end = 2.dp)
                    )
                    Text(
                        text = totalExpense,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AppDesignSystem.colors.donutChartTextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // ================= 右侧：极致自适应的图例列表 =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 24.dp, end = 8.dp),
            // ✨ 2. 改回 SpaceEvenly，让它在固定高度内自动均分剩余空间
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ✨ 3. 为了防极端情况（比如有 20 个分类），我们设一个硬上限（比如最多画 10 个）
            // 剩下的只能截断，因为屏幕物理分辨率真的装不下那么小了。
            val maxDisplayCount = 10
            val displayData = data.take(maxDisplayCount)

            displayData.forEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 圆点随比例缩放 (基础大小 10dp)
                    Box(
                        modifier = Modifier
                            .size((10 * scale).dp)
                            .background(getPremiumBrush(index), shape = androidx.compose.foundation.shape.CircleShape)
                    )

                    // 间距随比例缩放 (基础间距 8dp)
                    Spacer(modifier = Modifier.width((8 * scale).dp))

                    // 分类名字随比例缩放 (基础字号 13sp)
                    Text(
                        text = item.name,
                        fontSize = (13 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = AppDesignSystem.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width((4 * scale).dp))

                    // 百分比随比例缩放 (基础字号 13sp)
                    Text(
                        text = "${(item.percentage * 100).toInt()}%",
                        fontSize = (13 * scale).sp,
                        color = AppDesignSystem.colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 可选：如果被截断了，可以在最下面显示一个细微的提示
            if (data.size > maxDisplayCount) {
                Text(
                    text = "等共 ${data.size} 项",
                    fontSize = (10 * scale).sp,
                    color = AppDesignSystem.colors.textTertiary,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}