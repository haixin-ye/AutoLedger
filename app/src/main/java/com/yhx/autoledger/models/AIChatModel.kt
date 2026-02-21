package com.yhx.autoledger.models

import androidx.compose.ui.graphics.Color
import java.util.UUID

// 对话类型：用户发送的内容，或 AI 回复的内容
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val billPreview: BillPreview? = null,// AI 特有的：解析出的账单信息
    val isSaved: Boolean = false // ✨ 数据层面的状态锁，再怎么切屏都不会丢！
)

// 模拟 AI 解析出的账单结构，方便后续接口对接
data class BillPreview(
    val category: String,
    val amount: String,
    val date: String,
    val icon: String,
    val color: Color,
    val note: String = "", // ✨ 接收 AI 解析的备注
    val type: Int = 0      // ✨ 接收 AI 判断的收支类型
)