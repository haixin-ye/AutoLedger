package com.yhx.autoledger.ui.components

import androidx.compose.runtime.Composable

@Composable
fun ManualAddSheet(
    onDismiss: () -> Unit,
    onSave: (type: Int, category: String, amount: String, remark: String, timestamp: Long) -> Unit
) {
    // 像一张白纸一样调用 BaseTransactionSheet
    BaseTransactionSheet(
        isEditMode = false,
        onDismiss = onDismiss,
        onSave = { type, category, _, amount, remark, timestamp ->
            // 把 Double 转换回原来 ManualAddSheet 抛出的 String 格式
            val amountStr =
                if (amount % 1.0 == 0.0) amount.toInt().toString() else amount.toString()
            onSave(type, category, amountStr, remark, timestamp)
        }
    )
}