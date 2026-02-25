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
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题
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
    val focusManager = LocalFocusManager.current

    var budgetValue by remember { mutableStateOf(currentBudget.toFloat()) }
    var textValue by remember { mutableStateOf(currentBudget.toInt().toString()) }

    var maxSliderRange by remember { mutableStateOf(maxOf(5000f, currentBudget.toFloat())) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // ✨ 映射弹窗背景色
        containerColor = AppTheme.colors.sheetBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✨ 显式映射主文本色，防止深色模式下变成黑底黑字
            Text("调整月预算", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
            Spacer(Modifier.height(24.dp))

            val previewMonthBudget = if (budgetValue > 0) budgetValue else 1f
            val previewDayBudget = previewMonthBudget / 30f
            val previewMonthProgress = (monthExpense / previewMonthBudget).toFloat().coerceIn(0f, 1f)
            val previewDayProgress = (todayExpense / previewDayBudget).toFloat().coerceIn(0f, 1f)

            DoubleCircleGauges(
                monthProgress = previewMonthProgress,
                dayProgress = previewDayProgress,
                isPreviewMode = true,
                customDayCenterText = "¥${previewDayBudget.toInt()}"
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // ✨ 映射品牌强调色
                Text("¥ ", fontSize = 36.sp, fontWeight = FontWeight.Black, color = AppTheme.colors.brandAccent)
                BasicTextField(
                    value = textValue,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        textValue = filtered
                        val parsed = filtered.toFloatOrNull() ?: 0f
                        budgetValue = parsed

                        if (parsed > maxSliderRange) {
                            maxSliderRange = parsed
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    // ✨ 映射品牌强调色
                    textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = AppTheme.colors.brandAccent),
                    modifier = Modifier.width(IntrinsicSize.Min)
                )
            }

            Spacer(Modifier.height(16.dp))

            Slider(
                value = budgetValue.coerceIn(0f, maxSliderRange),
                onValueChange = { rawValue ->
                    val snappedValue = (rawValue / 10).roundToInt() * 10f
                    budgetValue = snappedValue
                    textValue = snappedValue.toInt().toString()
                    focusManager.clearFocus()
                },
                valueRange = 0f..maxSliderRange,
                // ✨ 映射品牌强调色
                colors = SliderDefaults.colors(
                    thumbColor = AppTheme.colors.brandAccent,
                    activeTrackColor = AppTheme.colors.brandAccent.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // ✨ 映射极弱次要文本色
                Text("¥0", color = AppTheme.colors.textTertiary, fontSize = 12.sp)
                Text("¥${maxSliderRange.toInt()}", color = AppTheme.colors.textTertiary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave(budgetValue.toDouble())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                // ✨ 映射品牌强调色
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.brandAccent),
                shape = RoundedCornerShape(20.dp)
            ) {
                // ✨ 显式映射强调色上的白色文字
                Text("确认修改", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textOnAccent)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}