package com.yhx.autoledger.data.repository

import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.CategorySum
import com.yhx.autoledger.data.dao.LedgerDao
import com.yhx.autoledger.data.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// @Inject constructor 告诉 Hilt 这个大管家需要哪些工具（Dao），Hilt 会自动送过来
class LedgerRepository @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao
) {

    // ================== 分类相关 ==================

    // 获取所有的分类（UI 的分类选择器需要用到）
    fun getAllCategories() = categoryDao.getAllCategories()


    // ================== 账单读写相关 ==================

    // 记一笔新账（或者更新某笔账）
    suspend fun insertLedger(ledger: LedgerEntity) {
        ledgerDao.insertLedger(ledger)
    }

    // ✨ 补上这个缺失的更新方法！
    suspend fun updateLedger(ledger: LedgerEntity) {
        ledgerDao.updateLedger(ledger)
    }

    // 删除一笔账（侧滑删除时调用）
    suspend fun deleteLedger(ledger: LedgerEntity) {
        ledgerDao.deleteLedger(ledger)
    }

    // 批量删除
    suspend fun deleteLedgersByIds(ids: List<Long>) {
        ledgerDao.deleteLedgersByIds(ids)
    }

    // 获取所有账单流（首页最近账单需要用到）
    fun getAllLedgersDesc(): Flow<List<LedgerEntity>> {
        return ledgerDao.getAllLedgersDesc()
    }


    // ================== 统计分析相关 (明细页需要) ==================

    // 获取某段时间内的所有账单（用于明细页按月筛选）
    fun getLedgersBetween(startTime: Long, endTime: Long): Flow<List<LedgerEntity>> {
        return ledgerDao.getLedgersBetween(startTime, endTime)
    }

    // 获取某段时间内的总支出或总收入（type: 0是支出, 1是收入）
    fun getTotalAmountBetween(startTime: Long, endTime: Long, type: Int): Flow<Double?> {
        return ledgerDao.getTotalAmountBetween(startTime, endTime, type)
    }

    // 获取某段时间内各分类的支出总和（用于画饼图）
    fun getCategorySumBetween(startTime: Long, endTime: Long, type: Int = 0): Flow<List<CategorySum>> {
        return ledgerDao.getCategorySumBetween(startTime, endTime, type)
    }
}