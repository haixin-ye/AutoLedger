package com.yhx.autoledger.model // ✨ 修正：直接使用你现有的 model 包名！

// 1. 大模型返回的标准化 JSON 数据结构
data class AIResponseData(
    val intent: String, // "ACCOUNTING", "CHAT", "DENIED"
    val reply_text: String,
    val bills: List<BillData> = emptyList()
) {
    data class BillData(
        val category: String,
        val amount: Double,
        val date: String,
        val icon: String,
        val type: Int, // 0支出, 1收入
        val note: String
    )
}

// 2. AI 人设实体类
data class AIPersona(
    val id: String,
    val name: String,
    val description: String,
    val avatar: String,
    val systemPrompt: String,
    val strategyType: PresentationStrategyType
)

// 3. 策略枚举
enum class PresentationStrategyType {
    BUTLER,   // 管家排版
    TSUNDERE, // 傲娇排版
    FRIEND    // 朋友排版
}