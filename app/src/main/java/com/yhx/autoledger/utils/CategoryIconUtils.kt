package com.yhx.autoledger.utils

object CategoryIconUtils {

    val expenseCategories = listOf(
        "餐饮" to "🍱",
        "交通" to "🚗",
        "购物" to "🛒",
        "娱乐" to "🎮",
        "居住" to "🏠",
        "其他" to "⚙️"
    )

    val incomeCategories = listOf(
        "工资" to "💰",
        "理财" to "📈",
        "兼职" to "💼",
        "红包" to "🧧",
        "报销" to "🧾",
        "其他" to "💵"
    )

    // 在初始化时将其转换为 Map，提升查找性能
    private val expenseMap = expenseCategories.toMap()
    private val incomeMap = incomeCategories.toMap()

    /**
     * 根据分类名称和账单类型获取对应的 Emoji 图标
     * * @param category 分类名称 (如 "餐饮", "其他")
     * @param isExpense 是否为支出 (默认为 true)。用于区分同名的分类，如"其他"
     * @return 对应的 Emoji 字符串。如果没有匹配项，返回一个默认的通用 Emoji
     */
    fun getIconForCategory(category: String, isExpense: Boolean = true): String {
        return if (isExpense) {
            expenseMap[category] ?: "🪙" // 找不到时给个默认的金币兜底
        } else {
            incomeMap[category] ?: "🪙"
        }
    }
}