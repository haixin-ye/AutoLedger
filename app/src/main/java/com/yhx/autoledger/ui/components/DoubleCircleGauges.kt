package com.yhx.autoledger.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    // ✨ 新增：获取当前的焦点管理器
    val focusManager = LocalFocusManager.current

    var budgetValue by remember { mutableStateOf(currentBudget.toFloat()) }
    var textValue by remember { mutableStateOf(currentBudget.toInt().toString()) }

    // 修复 1：上限默认5000，或者等于你之前设置的更大的预算。滑动时它绝对不会变！
    var maxSliderRange by remember { mutableStateOf(maxOf(5000f, currentBudget.toFloat())) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                // ✨ 新增：点击空白区域时清除焦点，收起键盘并取消光标闪烁
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("调整月预算", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            // 预览仪表盘逻辑
            val previewMonthBudget = if (budgetValue > 0) budgetValue else 1f
            val previewDayBudget = previewMonthBudget / 30f
            val previewMonthProgress = (monthExpense / previewMonthBudget).toFloat().coerceIn(0f, 1f)
            val previewDayProgress = (todayExpense / previewDayBudget).toFloat().coerceIn(0f, 1f)

            // 这里一调用，颜色硬编码导致的不变色 Bug 就彻底解决了。
            DoubleCircleGauges(
                monthProgress = previewMonthProgress,
                dayProgress = previewDayProgress,
                isPreviewMode = true,
                customDayCenterText = "¥${previewDayBudget.toInt()}"
            )

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

                        // 修复 2：只有当用户手动输入的数字超过了当前的上限，才把上限撑大
                        if (parsed > maxSliderRange) {
                            maxSliderRange = parsed
                        }
                    },
                    // ✨ 修改：将键盘的右下角按钮设置为 "完成 (Done)"
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    // ✨ 新增：监听 "完成" 按钮的点击事件，清除焦点
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = AccentBlue),
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }

            Spacer(Modifier.height(16.dp))

            // 修复 3：平滑稳定的 Slider
            Slider(
                // 强制限制 budgetValue 不超过上限，防止滑块崩溃
                value = budgetValue.coerceIn(0f, maxSliderRange),
                onValueChange = { rawValue ->
                    val snappedValue = (rawValue / 10).roundToInt() * 10f
                    budgetValue = snappedValue
                    textValue = snappedValue.toInt().toString()

                    // ✨ 优化：滑动 Slider 时也自动清除输入框的焦点
                    focusManager.clearFocus()
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
                onClick = {
                    // ✨ 优化：点击保存前也确保清除焦点
                    focusManager.clearFocus()
                    onSave(budgetValue.toDouble())
                    onDismiss()
                },
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