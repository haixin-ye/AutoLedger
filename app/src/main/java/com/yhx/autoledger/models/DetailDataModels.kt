package com.yhx.autoledger.models

import androidx.compose.ui.graphics.Color

/**
 * 月度概要数据接口
 */
data class MonthlyStats(
    val totalExpense: String,
    val totalIncome: String,
    val balance: String,
    val dailyAvg: String
)

/**
 * 分类占比数据接口
 */
data class CategoryPercentage(
    val name: String,
    val amount: String,
    val percentage: Float, // 0.0 ~ 1.0
    val icon: String,
    val color: Color
)