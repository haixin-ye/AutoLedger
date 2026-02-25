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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.models.CategoryPercentage
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题

// ✨ 将获取颜色的逻辑改为从 AppTheme 动态读取
@Composable
fun getPremiumBrush(index: Int): Brush {
    val palette = AppTheme.colors.donutChartPalette
    val colors = palette[index % palette.size]
    return Brush.linearGradient(listOf(colors.first, colors.second))
}

@Composable
fun getPremiumBaseColor(index: Int): Color {
    val palette = AppTheme.colors.donutChartPalette
    return palette[index % palette.size].first
}

@Composable
fun PremiumDonutChart(data: List<CategoryPercentage>, totalExpense: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // 保持高度固定，防跳动
            .padding(horizontal = 8.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ================= 左侧：霸气圆环 =================
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1.3f)
        ) {
            // ✨ 提前读取轨道颜色，避免在 Canvas 作用域报错
            val trackColor = AppTheme.colors.donutChartTrack

            // 提前准备好所有的 Brush，以便在 Canvas 中使用
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
                    color = trackColor, // ✨ 映射底轨色
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
                    color = AppTheme.colors.donutChartTextSecondary, // ✨ 映射次要文字色
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.donutChartTextPrimary, // ✨ 映射主要文字色
                        modifier = Modifier.padding(bottom = 2.dp, end = 2.dp)
                    )
                    Text(
                        text = totalExpense,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AppTheme.colors.donutChartTextPrimary, // ✨ 映射主要文字色
                        letterSpacing = (-0.5).sp
                    )
                }
            }
        }

        // ================= 右侧：填满空间的对齐图例 =================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 36.dp, end = 16.dp),
            // ✨ 修复：改为 SpaceEvenly，让图例在垂直方向上均匀分布，彻底告别高度写死导致的溢出遮挡！
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            data.take(5).forEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. 圆点
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(getPremiumBrush(index), shape = androidx.compose.foundation.shape.CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 2. 分类名字（利用 weight 顶开右侧的百分比）
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary, // ✨ 图例名字映射全局主文字色
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 3. 百分比（自然靠右对齐）
                    Text(
                        text = "${(item.percentage * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = AppTheme.colors.textSecondary, // ✨ 图例百分比映射全局副文字色
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}