package com.yhx.autoledger.ui.components

import androidx.compose.runtime.Composable
import com.yhx.autoledger.data.entity.LedgerEntity

@Composable
fun EditLedgerSheet(
    initialLedger: LedgerEntity, // 传入需要修改的账单
    onDismiss: () -> Unit,
    onSave: (LedgerEntity) -> Unit, // 保存更新
    onDelete: (LedgerEntity) -> Unit // 删除
) {
    // 数据回填清洗：把金额转成字符串（去掉 .0 这种不好看的后缀）
    val initialAmountStr = if (initialLedger.amount % 1.0 == 0.0) {
        initialLedger.amount.toInt().toString()
    } else {
        initialLedger.amount.toString()
    }

    // 如果备注和分类名一样，说明当时没填备注，这里传给 Base 空字符串，让其显示 Hint
    val initialRemark =
        if (initialLedger.note == initialLedger.categoryName) "" else initialLedger.note

    BaseTransactionSheet(
        isEditMode = true,
        initialType = initialLedger.type,
        initialAmount = initialAmountStr,
        initialCategory = initialLedger.categoryName,
        initialIcon = initialLedger.categoryIcon, // 传入实体中保存的图标（可能是 AI 生成的特殊 Emoji）
        initialRemark = initialRemark,
        initialTimestamp = initialLedger.timestamp,
        onDismiss = onDismiss,
        onDelete = { onDelete(initialLedger) },
        onSave = { type, category, icon, amount, remark, timestamp ->
            // 组装修改后的 LedgerEntity (保留原来的 id 和 source)
            val updatedLedger = initialLedger.copy(
                type = type,
                categoryName = category,
                categoryIcon = icon, // 接收 Base 组件智能计算后的最新图标
                amount = amount,
                note = remark,
                timestamp = timestamp
            )
            onSave(updatedLedger)
        }
    )
}