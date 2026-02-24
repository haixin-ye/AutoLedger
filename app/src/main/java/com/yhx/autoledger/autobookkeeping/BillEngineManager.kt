package com.yhx.autoledger.autobookkeeping

import com.yhx.autoledger.autobookkeeping.core.ParsedBill
import com.yhx.autoledger.autobookkeeping.parser.WeChatScreenParser // 名字变了

class BillEngineManager {
    // 注册无障碍屏幕解析器
    private val parsers = listOf(
        WeChatScreenParser()
    )

    // 方法名从 processNotification 改为 processText
    fun processText(packageName: String, screenText: String): ParsedBill? {
        for (parser in parsers) {
            // 标题在这里用不到了，传个空字符串就行
            val result = parser.parse(packageName, "", screenText)
            if (result != null) return result
        }
        return null
    }
}