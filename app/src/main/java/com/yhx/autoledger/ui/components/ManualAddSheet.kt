package com.yhx.autoledger.ui.components

import androidx.compose.runtime.Composable

@Composable
fun ManualAddSheet(
    onDismiss: () -> Unit,
    // ✨ 新增 icon 参数
    onSave: (type: Int, category: String, icon: String, amount: String, remark: String, timestamp: Long) -> Unit
) {
    BaseTransactionSheet(
        isEditMode = false,
        onDismiss = onDismiss,
        onSave = { type, category, icon, amount, remark, timestamp -> // 接收 icon
            val amountStr = if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()
            onSave(type, category, icon, amountStr, remark, timestamp) // 传递 icon
        }
    )
}