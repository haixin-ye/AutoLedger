package com.yhx.autoledger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,         // 分类名称，如 "餐饮", "交通"
    val iconName: String,     // 图标标识符（建议存字符串，如 "ic_food"，方便Compose动态匹配图标）
    val type: Int,            // 收支类型：0代表支出，1代表收入
    val isSystemDefault: Boolean, // 核心字段：标识是否是系统预设的硬编码款式（预设款式不允许用户删除）

)