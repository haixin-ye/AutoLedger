package com.yhx.autoledger.ui.components
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectorButton(
    currentTimestamp: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 控制弹窗的显示与隐藏
    var showDialog by remember { mutableStateOf(false) }

    // 初始化 Material 3 的 DatePicker 状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentTimestamp
    )

    // 格式化当前时间用于按钮显示
    val displayDate = remember(currentTimestamp) {
        val format = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        format.format(Date(currentTimestamp))
    }

    // 触发按钮
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Icon(Icons.Default.DateRange, contentDescription = "选择日期", modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = displayDate)
    }

    // 真正的日期选择弹窗
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    // 将选中的 UTC 时间戳传回上层，若未选则用当前时间
                    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    onDateSelected(selectedMillis)
                    showDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}