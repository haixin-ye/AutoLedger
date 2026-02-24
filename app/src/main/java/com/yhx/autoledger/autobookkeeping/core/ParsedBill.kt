package com.yhx.autoledger.autobookkeeping.core

data class ParsedBill(
    val amount: Double,
    val type: Int,       // 0: 支出, 1: 收入 (退款归为收入)
    val note: String,    // 原始通知内容 (将作为 AI 的分析原料)
    val source: String   // "WECHAT" 或 "ALIPAY"
)