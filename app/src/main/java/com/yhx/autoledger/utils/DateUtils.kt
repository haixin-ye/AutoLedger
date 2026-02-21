package com.yhx.autoledger.utils

import java.util.Calendar

object DateUtils {
    // 根据偏移量获取该月第一天的零点
    fun getMonthStart(offset: Int = 0): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    // 根据偏移量获取该月最后一天的 23:59:59
    fun getMonthEnd(offset: Int = 0): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    // 获取格式化的年月标题（如 "2024年2月"）
    fun getYearMonthString(offset: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "${year}年${month}月"
    }

    // 获取天数信息用于计算进度和日均 (当前第几天, 该月总天数)
    fun getDaysInfo(offset: Int = 0): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        // 如果是本月，返回今天是第几天；如果是历史月份，相当于过完了，返回总天数
        val currentDay = if (offset == 0) calendar.get(Calendar.DAY_OF_MONTH) else totalDays
        return Pair(currentDay, totalDays)
    }

    // 在 DateUtils
    fun getYearMonthKey(offset: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offset)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "${year}_${String.format("%02d", month)}" // 生成如 "2024_02"
    }
}