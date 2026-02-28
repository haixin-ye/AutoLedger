package com.yhx.autoledger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account_books")
data class AccountBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val coverColor: Int, // 存储主题颜色 (ARGB Int)，例如 0xFF42A5F5.toInt()
    val isSystemDefault: Boolean = false // 标识是否为初始预设的账本，预设账本不允许删除
)