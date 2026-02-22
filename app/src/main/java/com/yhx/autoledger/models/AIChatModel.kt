package com.yhx.autoledger.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

// 对话类型：用户发送的内容，或 AI 回复的内容
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val billPreviews: List<BillPreview> = emptyList()

)

// 模拟 AI 解析出的账单结构，方便后续接口对接
data class BillPreview(
    val id: String = UUID.randomUUID().toString(), // ✨ 给每个账单发一个身份证
    val category: String,
    val amount: String,
    val date: String,
    val icon: String,
    val color: Color,
    val note: String = "", // ✨ 接收 AI 解析的备注
    val type: Int = 0,
    val isSaved: Boolean = false

)