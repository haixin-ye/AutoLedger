package com.yhx.autoledger.autobookkeeping.core

interface IBillParser {
    // 尝试解析通知，解析成功返回账单对象，失败（不是账单）返回 null
    fun parse(packageName: String, title: String, text: String): ParsedBill?
}