package com.yhx.autoledger.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.viewmodel.DailyRecord
import java.time.YearMonth

@Composable
fun DailyTrendChart(month: YearMonth, dailyMap: Map<Int, DailyRecord>, budget: Double) {
    var selectedStyle by remember { mutableStateOf(ChartStyle.BURNDOWN) }
    val daysInMonth = month.lengthOfMonth()
    val expenses = (1..daysInMonth).map { dailyMap[it]?.expense?.toFloat() ?: 0f }
    val maxExpense = expenses.maxOrNull()?.takeIf { it > 0 } ?: 100f

    // 预算消耗逻辑：从预算起始，每日递减
    val burnDownPoints = remember(dailyMap, budget) {
        val points = mutableListOf<Float>()
        var current = budget.toFloat()
        for (e in expenses) {
            current -= e
            points.add(current)
        }
        points
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 自定义 Header
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("日消费趋势", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F3F6))
                    .padding(2.dp)
            ) {
                listOf(ChartStyle.BURNDOWN, ChartStyle.CURVE, ChartStyle.BAR).forEach { style ->
                    val isSelected = selectedStyle == style
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { selectedStyle = style }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            style.label,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.Black else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(Modifier
            .fillMaxWidth()
            .height(200.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val leftPadding = 80f
                val bottomPadding = 40f
                val drawW = size.width - leftPadding
                val drawH = size.height - bottomPadding
                val stepX = drawW / (daysInMonth - 1)

                // 绘制 Y 轴刻度 (0, 50%, 100%)
                val topVal =
                    if (selectedStyle == ChartStyle.BURNDOWN) budget.toInt() else maxExpense.toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    topVal.toString(),
                    0f,
                    30f,
                    Paint().apply { color = android.graphics.Color.LTGRAY; textSize = 26f })

                // 绘制 X 轴日期 (每 10 天显示一个)
                listOf(1, 10, 20, daysInMonth).forEach { day ->
                    val x = leftPadding + (day - 1) * stepX
                    drawContext.canvas.nativeCanvas.drawText(
                        "${day}日",
                        x,
                        size.height - 10f,
                        Paint().apply {
                            color = android.graphics.Color.LTGRAY; textSize = 24f; textAlign =
                            Paint.Align.CENTER
                        })
                }

                when (selectedStyle) {
                    ChartStyle.BURNDOWN -> {
                        val path = Path()
                        burnDownPoints.forEachIndexed { i, v ->
                            val x = leftPadding + i * stepX
                            // 消耗图从顶部（预算全额）向下掉
                            val y = drawH - (v.coerceAtLeast(0f) / budget.toFloat()) * drawH
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        // 消耗图使用较粗的渐变线条，并在 0 刻度画一条预警红线
                        drawPath(
                            path,
                            brush = ChartPremiumGradient,
                            style = Stroke(8f, cap = StrokeCap.Round)
                        )
                        drawLine(
                            Color.Red.copy(0.2f),
                            Offset(leftPadding, drawH),
                            Offset(size.width, drawH),
                            2f
                        )
                    }

                    ChartStyle.CURVE -> {
                        val path = Path()
                        expenses.forEachIndexed { i, v ->
                            val x = leftPadding + i * stepX
                            val y = drawH - (v / maxExpense) * drawH
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path,
                            brush = ChartPremiumGradient,
                            style = Stroke(6f, cap = StrokeCap.Round)
                        )
                    }

                    ChartStyle.BAR -> {
                        expenses.forEachIndexed { i, v ->
                            val barH = (v / maxExpense) * drawH
                            drawRoundRect(
                                brush = ChartPremiumGradient, // ✨ 使用你要求的渐变
                                topLeft = Offset(leftPadding + i * stepX - 10f, drawH - barH),
                                size = Size(20f, barH),
                                cornerRadius = CornerRadius(10f)
                            )
                        }
                    }


                }
            }
        }
    }
}

enum class ChartStyle(val label: String) {
    BURNDOWN("消耗"), CURVE("曲线"), BAR("柱状")
}