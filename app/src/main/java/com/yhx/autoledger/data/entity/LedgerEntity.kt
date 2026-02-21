package com.yhx.autoledger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledgers")
data class LedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double,       // 交易金额
    val type: Int,            // 收支类型：0代表支出，1代表收入 (虽然分类里有，但流水表冗余一份方便快速统计)

    // 这里我们直接把分类的名字和图标存下来（这叫数据快照），
    // 这样即使以后用户删除了某个自定义分类，之前的历史账单依然能显示图标和名字。
    val categoryName: String,
    val categoryIcon: String,

    val timestamp: Long,      // 交易时间（毫秒时间戳）
    val note: String,         // 备注信息
    val source: String        // 来源："MANUAL"(手动), "AI"(语音识别), "WECHAT"(微信自动)
)