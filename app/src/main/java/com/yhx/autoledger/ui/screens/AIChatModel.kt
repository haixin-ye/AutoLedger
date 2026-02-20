package com.yhx.autoledger.ui.screens

import androidx.compose.ui.graphics.Color

// 对话类型：用户发送的内容，或 AI 回复的内容
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val billPreview: BillPreview? = null // AI 特有的：解析出的账单信息
)

// 模拟 AI 解析出的账单结构，方便后续接口对接
data class BillPreview(
    val category: String,
    val amount: String,
    val date: String,
    val icon: String,
    val color: Color
)