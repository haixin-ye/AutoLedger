package com.yhx.autoledger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.models.CategoryPercentage

// 高级色板提取
val PremiumColors = listOf(
    Pair(Color(0xFF84FAB0), Color(0xFF8FD3F4)),
    Pair(Color(0xFFA18CD1), Color(0xFFFBC2EB)),
    Pair(Color(0xFFFFECD2), Color(0xFFFCB69F)),
    Pair(Color(0xFF4FACFE), Color(0xFF00F2FE)),
    Pair(Color(0xFFF6D365), Color(0xFFFDA085)),
    Pair(Color(0xFFE0C3FC), Color(0xFF8EC5FC)),
    Pair(Color(0xFFFFAA85), Color(0xFFB3315F))
)

fun getPremiumBrush(index: Int): Brush {
    val colors = PremiumColors[index % PremiumColors.size]
    return Brush.linearGradient(listOf(colors.first, colors.second))
}

fun getPremiumBaseColor(index: Int): Color {
    return PremiumColors[index % PremiumColors.size].first
}


//详情页面圆环图
@Composable
fun PremiumDonutChart(data: List<CategoryPercentage>, totalExpense: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().height(280.dp).padding(vertical = 16.dp)
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidthPx = 22.dp.toPx()
            val radius = (size.minDimension - strokeWidthPx) / 2f
            val circumference = 2f * Math.PI.toFloat() * radius
            val capAngle = (strokeWidthPx / circumference) * 360f
            val visualGapAngle = 2f
            val totalOffsetAngle = capAngle + visualGapAngle

            drawCircle(
                color = Color(0xFFF1F3F6),
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            var currentStartAngle = -90f

            if (data.size == 1) {
                drawArc(
                    brush = getPremiumBrush(0),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx),
                    topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                    size = Size(radius * 2f, radius * 2f)
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
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                            topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                            size = Size(radius * 2f, radius * 2f)
                        )
                    } else if (rawSweep > 0f) {
                        drawArc(
                            brush = getPremiumBrush(index),
                            startAngle = currentStartAngle + (visualGapAngle / 2f),
                            sweepAngle = maxOf(0.5f, rawSweep - visualGapAngle),
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                            topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f),
                            size = Size(radius * 2f, radius * 2f)
                        )
                    }
                    currentStartAngle += rawSweep
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "本月总支出",
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 4.dp, end = 2.dp)
                )
                Text(
                    text = totalExpense,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1F2937),
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}