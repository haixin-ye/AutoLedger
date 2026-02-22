package com.yhx.autoledger.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AccentBlue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingSheet(
    currentBudget: Double,
    monthExpense: Double,
    todayExpense: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var budgetValue by remember { mutableStateOf(currentBudget.toFloat()) }
    var textValue by remember { mutableStateOf(currentBudget.toInt().toString()) }

    // ✨ 修复 1：上限默认5000，或者等于你之前设置的更大的预算。滑动时它绝对不会变！
    var maxSliderRange by remember { mutableStateOf(maxOf(5000f, currentBudget.toFloat())) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("调整月预算", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            // 预览仪表盘逻辑
            val previewMonthBudget = if (budgetValue > 0) budgetValue else 1f
            val previewDayBudget = previewMonthBudget / 30f
            val previewMonthProgress = (monthExpense / previewMonthBudget).toFloat().coerceIn(0f, 1f)
            //增加一个日环的数值变化情况
            val previewDayProgress=(todayExpense/previewDayBudget).toFloat().coerceIn(0f, 1f)
            // ✨ 优化：高级的三色渐变算法 (绿 -> 黄 -> 红)
            val safeColor = Color(0xFF2ED573)    // 活力绿
            val warningColor = Color(0xFFFFC107) // 警告黄
            val dangerColor = Color(0xFFFF4757)  // 西瓜红

            val previewColor = when {
                previewMonthProgress <= 0.5f -> {
                    // 0% ~ 50% 阶段：绿色到黄色渐变。把进度乘以2，映射到 0~1 的区间
                    androidx.compose.ui.graphics.lerp(safeColor, warningColor, previewMonthProgress * 2f)
                }
                else -> {
                    // 50% ~ 100% 阶段：黄色到红色渐变。
                    androidx.compose.ui.graphics.lerp(warningColor, dangerColor, (previewMonthProgress - 0.5f) * 2f)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // 左侧月环预览
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        CircularProgressIndicator(progress = { previewMonthProgress }, modifier = Modifier.fillMaxSize(), color = previewColor, strokeWidth = 8.dp, trackColor = Color.LightGray.copy(alpha = 0.3f))
                        // ✨ 修复 2：给百分比加上 color = previewColor，让它随圆环一起渐变变色！
                        Text("${(previewMonthProgress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = previewColor)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("月预算", color = Color.Gray, fontSize = 12.sp)
                }

                // 右侧日环预览
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                        // ✨ 修复 3：把 progress 从 0f 改为 1f，让预览图显示为一个完整的绿环
                        CircularProgressIndicator(progress = { previewDayProgress }, modifier = Modifier.fillMaxSize(), color = safeColor, strokeWidth = 8.dp, trackColor = Color.LightGray.copy(alpha = 0.3f))
                        Text("¥${previewDayBudget.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = safeColor)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("日均可用", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 键盘输入区
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¥ ", fontSize = 36.sp, fontWeight = FontWeight.Black, color = AccentBlue)
                BasicTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        textValue = filtered
                        val parsed = filtered.toFloatOrNull() ?: 0f
                        budgetValue = parsed

                        // ✨ 修复 2：只有当用户手动输入的数字超过了当前的上限，才把上限撑大
                        if (parsed > maxSliderRange) {
                            maxSliderRange = parsed
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = AccentBlue),
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ✨ 修复 3：平滑稳定的 Slider
            Slider(
                // 强制限制 budgetValue 不超过上限，防止滑块崩溃
                value = budgetValue.coerceIn(0f, maxSliderRange),
                onValueChange = { rawValue ->
                    val snappedValue = (rawValue / 10).roundToInt() * 10f
                    budgetValue = snappedValue
                    textValue = snappedValue.toInt().toString()
                },
                valueRange = 0f..maxSliderRange,
                colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("¥0", color = Color.LightGray, fontSize = 12.sp)
                Text("¥${maxSliderRange.toInt()}", color = Color.LightGray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { onSave(budgetValue.toDouble()); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("确认修改", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}