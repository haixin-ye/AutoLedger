package com.yhx.autoledger.models

import com.google.gson.annotations.SerializedName

// --- 请求模型 ---
data class ChatRequest(
    val model: String = "deepseek-chat", // 使用deepseek 模型
    val messages: List<Message>,
    val temperature: Double = 0.1, // 温度设低一点，让AI输出更理性和稳定（适合提取格式化数据）
    @SerializedName("response_format")
    val responseFormat: ResponseFormat? = ResponseFormat(type = "json_object") // 强制要求输出 JSON
)

data class Message(
    val role: String, // "system" 或 "user"
    val content: String
)

data class ResponseFormat(
    val type: String
)

// --- 响应模型 ---
data class ChatResponse(
    val choices: List<Choice>?
)

data class Choice(
    val message: Message?
)