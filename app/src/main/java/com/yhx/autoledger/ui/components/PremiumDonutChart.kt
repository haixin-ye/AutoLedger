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

// ✨ 将高级渐变色板统一抽取到组件内部，使其内聚
val PremiumColors = listOf(
    Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4)), // 清新薄荷 -> 晴空蓝
    Pair(Color(0xFFA18CD1), Color(0xFFFBC2EB)), // 梦幻紫 -> 浅樱粉
    Pair(Color(0xFFFFECD2), Color(0xFFFCB69F)), // 活力蜜桃
    Pair(Color(0xFF4FACFE), Color(0xFF00F2FE)), // 科技亮蓝
    Pair(Color(0xFFF6D365), Color(0xFFFDA085)), // 暖阳橙黄
    Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC)), // 晚霞灰紫
    Pair(Color(0xFFFFAA85), Color(0xFFB3315F))  // 树莓红
)

fun getPremiumBrush(index: Int): Brush {
    val colors = PremiumColors[index % PremiumColors.size]
    return Brush.linearGradient(listOf(colors.first, colors.second))
}

fun getPremiumBaseColor(index: Int): Color {
    return PremiumColors[index % PremiumColors.size].first
}

// ✨ 纯粹图表组件
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
            androidx.compose.foundation.Canvas(modifier = Modifier.size(210.dp)) {
                val strokeWidthPx = 20.dp.toPx()
                val radius = (size.minDimension - strokeWidthPx) / 2f
                val circumference = 2f * Math.PI.toFloat() * radius
                val capAngle = (strokeWidthPx / circumference) * 360f
                val visualGapAngle = 2f
                val totalOffsetAngle = capAngle + visualGapAngle

                drawCircle(
                    color = Color(0xFFF1F3F6),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx)
                )

                var currentStartAngle = -90f

                if (data.size == 1) {
                    drawArc(
                        brush = getPremiumBrush(0),
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
                                brush = getPremiumBrush(index),
                                startAngle = actualStart,
                                sweepAngle = actualSweep,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthPx, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                topLeft = androidx.compose.ui.geometry.Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                            )
                        } else if (rawSweep > 0f) {
                            drawArc(
                                brush = getPremiumBrush(index),
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
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(bottom = 2.dp, end = 2.dp)
                    )
                    Text(
                        text = totalExpense,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1F2937),
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
                // ✨ 核心修改点：加大 start padding (例如 36.dp 或 40.dp)
                // 这会把整个列表向右挤，同时压缩宽度，让名字和百分比靠近
                .padding(start = 36.dp, end = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            data.take(5).forEachIndexed { index, item ->
                // 去掉了多余的嵌套 Row，直接一层搞定
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
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
                        color = Color(0xFF2D3436),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 3. 百分比（自然靠右对齐）
                    Text(
                        text = "${(item.percentage * 100).toInt()}%",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}