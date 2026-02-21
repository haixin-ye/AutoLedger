package com.yhx.autoledger.data.dao

// 用于接收 GROUP BY 聚合查询的结果
data class CategorySum(
    val categoryName: String,
    val categoryIcon: String,
    val totalAmount: Double
)