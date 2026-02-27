package com.yhx.autoledger.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AppTheme
import com.yhx.autoledger.viewmodel.MonthlyRecord
import kotlin.math.roundToInt

@Composable
fun MonthlyTrendChart(year: Int, monthlyMap: Map<Int, MonthlyRecord>, budget: Double) {
    var selectedStyle by remember { mutableStateOf(ChartStyle.BAR) } // 复用枚举

    val displayMonths = 12
    // ✨ 核心逻辑：数据提取为真实结余（Income - Expense）
    val balances = (1..displayMonths).map {
        val r = monthlyMap[it] ?: MonthlyRecord()
        (r.income - r.expense).toFloat()
    }
    val maxAbs = balances.maxOfOrNull { Math.abs(it) }?.takeIf { it > 0f } ?: 100f

    var isDragging by remember { mutableStateOf(false) }
    var activeIndex by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val textPrimaryColor = AppTheme.colors.textPrimary
    val gridLineColor = AppTheme.colors.chartGridLine
    val axisTextColor = AppTheme.colors.chartAxisText.toArgb()

    val tooltipLineColor = AppTheme.colors.chartTooltipLine
    val tooltipBubbleBg = AppTheme.colors.chartTooltipBubbleBg
    val tooltipBubbleText = AppTheme.colors.chartTooltipBubbleText.toArgb()

    val incomeColor = AppTheme.colors.incomeColor
    val expenseColor = AppTheme.colors.expenseColor
    val brandAccent=AppTheme.colors.brandAccent
    val chartTooltipCircleOuter=AppTheme.colors.chartTooltipCircleOuter
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("月结余趋势", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor)
            Row(Modifier.clip(RoundedCornerShape(8.dp)).background(AppTheme.colors.chartToggleBg).padding(2.dp)) {
                // ✨ 结余图表不需要“消耗”模式，只留柱状和曲线
                listOf(ChartStyle.BAR, ChartStyle.CURVE).forEach { style ->
                    val isSelected = selectedStyle == style
                    Box(
                        Modifier.clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AppTheme.colors.chartToggleSelectedBg else Color.Transparent)
                            .clickable { selectedStyle = style }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(style.label, fontSize = 11.sp, color = if (isSelected) AppTheme.colors.chartToggleSelectedText else AppTheme.colors.chartToggleUnselectedText)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxWidth().height(200.dp)) {
            val leftPadding = 100f
            val bottomPadding = 40f

            Canvas(
                Modifier.fillMaxSize().pointerInput(displayMonths) {
                    val drawW = size.width - leftPadding
                    val stepX = if (displayMonths > 1) drawW / (displayMonths - 1) else drawW
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            isDragging = true
                            activeIndex = ((offset.x - leftPadding) / stepX).roundToInt().coerceIn(0, displayMonths - 1)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val newIndex = ((change.position.x - leftPadding) / stepX).roundToInt().coerceIn(0, displayMonths - 1)
                            if (newIndex != activeIndex) {
                                activeIndex = newIndex
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    )
                }
            ) {
                val drawW = size.width - leftPadding
                val drawH = size.height - bottomPadding
                val stepX = if (displayMonths > 1) drawW / (displayMonths - 1) else drawW

                // ✨ 新的 Y 轴中心：0结余线
                val centerY = drawH / 2f
                val availableH = drawH / 2f - 10f

                // Y轴画三条线：最大正结余、0、最大负结余
                val ySegments = listOf(
                    Pair(maxAbs, drawH / 2f - availableH),
                    Pair(0f, centerY),
                    Pair(-maxAbs, drawH / 2f + availableH)
                )

                ySegments.forEach { (value, yPos) ->
                    val yValueStr = if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
                    drawLine(color = gridLineColor, start = Offset(leftPadding, yPos), end = Offset(size.width, yPos), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    drawContext.canvas.nativeCanvas.drawText(yValueStr, 0f, yPos + 10f, Paint().apply { color = axisTextColor; textSize = 24f })
                }

                (1..12).forEach { m ->
                    if (m == 1 || m % 2 == 0) {
                        val x = leftPadding + (m - 1) * stepX
                        drawContext.canvas.nativeCanvas.drawText("${m}月", x, size.height - 5f, Paint().apply { color = axisTextColor; textSize = 22f; textAlign = Paint.Align.CENTER })
                    }
                }

                // --- 绘制双向柱状图 / 曲线 ---
                if (selectedStyle == ChartStyle.CURVE) {
                    val path = Path()
                    balances.forEachIndexed { i, v ->
                        val x = leftPadding + i * stepX
                        val y = centerY - (v / maxAbs) * availableH
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = brandAccent, style = Stroke(6f, cap = StrokeCap.Round))
                } else if (selectedStyle == ChartStyle.BAR) {
                    balances.forEachIndexed { i, v ->
                        val barH = (Math.abs(v) / maxAbs) * availableH
                        val barWidth = (stepX * 0.6f).coerceAtMost(40f)
                        val barColor = if (v >= 0) incomeColor else expenseColor
                        // ✨ 柱子从 centerY 开始画
                        val topLeftY = if (v >= 0) centerY - barH else centerY
                        drawRoundRect(color = barColor.copy(alpha = 0.8f), topLeft = Offset(leftPadding + i * stepX - barWidth / 2, topLeftY), size = Size(barWidth, barH), cornerRadius = CornerRadius(10f))
                    }
                }
            }

            val tooltipAlpha by animateFloatAsState(targetValue = if (isDragging) 1f else 0f, animationSpec = tween(durationMillis = 200))
            if (tooltipAlpha > 0f) {
                Canvas(Modifier.fillMaxSize()) {
                    val drawW = size.width - leftPadding
                    val stepX = drawW / 11
                    val targetX = leftPadding + activeIndex * stepX

                    val highlightVal = balances[activeIndex]
                    val centerY = (size.height - bottomPadding) / 2f
                    val availableH = (size.height - bottomPadding) / 2f - 10f
                    val targetY = centerY - (highlightVal / maxAbs) * availableH

                    drawLine(color = tooltipLineColor.copy(alpha = 0.5f * tooltipAlpha), start = Offset(targetX, 0f), end = Offset(targetX, size.height - bottomPadding), strokeWidth = 4f)

                    val dotColor = if (highlightVal >= 0) incomeColor else expenseColor
                    drawCircle(color = chartTooltipCircleOuter.copy(alpha = tooltipAlpha), radius = 14f, center = Offset(targetX, targetY))
                    drawCircle(color = dotColor.copy(alpha = tooltipAlpha), radius = 8f, center = Offset(targetX, targetY))

                    val valStr = if (highlightVal % 1 == 0f) highlightVal.toInt().toString() else String.format("%.1f", highlightVal)
                    val prefix = if (highlightVal > 0) "+" else ""
                    val tooltipText = "${activeIndex + 1}月结余: $prefix$valStr"

                    val paint = Paint().apply {
                        textSize = 32f; color = tooltipBubbleText; alpha = (255 * tooltipAlpha).toInt(); textAlign = Paint.Align.CENTER; isFakeBoldText = true
                    }
                    val textBounds = android.graphics.Rect()
                    paint.getTextBounds(tooltipText, 0, tooltipText.length, textBounds)
                    val boxWidth = textBounds.width() + 40f
                    val boxHeight = textBounds.height() + 30f

                    var boxX = targetX
                    if (boxX - boxWidth / 2 < leftPadding) boxX = leftPadding + boxWidth / 2
                    if (boxX + boxWidth / 2 > size.width) boxX = size.width - boxWidth / 2
                    var boxY = targetY - 60f
                    if (boxY - boxHeight < 0) boxY = targetY + boxHeight + 30f

                    drawRoundRect(color = tooltipBubbleBg.copy(alpha = 0.9f * tooltipAlpha), topLeft = Offset(boxX - boxWidth / 2, boxY - boxHeight), size = Size(boxWidth, boxHeight), cornerRadius = CornerRadius(16f))
                    drawContext.canvas.nativeCanvas.drawText(tooltipText, boxX, boxY - 15f, paint)
                }
            }
        }
    }
}