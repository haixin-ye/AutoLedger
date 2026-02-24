package com.yhx.autoledger.autobookkeeping.parser

import com.yhx.autoledger.autobookkeeping.core.IBillParser
import com.yhx.autoledger.autobookkeeping.core.ParsedBill

class WeChatScreenParser : IBillParser {

    // 匹配屏幕上红色的巨大金额
    private val amountRegex = "([¥￥])\\s?(\\d+\\.\\d+)".toRegex()

    override fun parse(packageName: String, title: String, text: String): ParsedBill? {
        // 1. 包名过滤
        if (packageName != "com.tencent.mm") return null

        // 2. 提取金额
        val amountMatch = amountRegex.find(text)
        val amount = amountMatch?.groupValues?.get(2)?.toDoubleOrNull()

        if (amount == null || amount <= 0) return null

        // 3. 场景 1：微信商户扫码支付成功界面
        if (text.contains("支付成功") && (text.contains("收款方") || text.contains("商户"))) {
            return ParsedBill(amount, 0, text, "WECHAT")
        }

        // 4. 场景 2：微信好友转账成功界面 / 收款界面
        if (text.contains("微信转账") && (text.contains("已存入零钱") || text.contains("转账给朋友"))) {
            val type = if (text.contains("已存入零钱") || text.contains("收到")) 1 else 0
            return ParsedBill(amount, type, text, "WECHAT")
        }

        return null
    }
}