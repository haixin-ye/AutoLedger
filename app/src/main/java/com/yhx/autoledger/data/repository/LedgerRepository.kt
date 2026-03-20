package com.yhx.autoledger.data.repository

import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.CategorySum
import com.yhx.autoledger.data.dao.LedgerDao
import com.yhx.autoledger.data.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LedgerRepository @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao // ✨ 重新加回分类 Dao
) {

    // ================== 分类相关 ==================
    // ✨ 核心修复：找回丢失的获取全部分类的方法！
    fun getAllCategories() = categoryDao.getAllCategories()


    // ================== 账单读写相关 ==================
    suspend fun insertLedger(ledger: LedgerEntity) {
        ledgerDao.insertLedger(ledger)
    }

    suspend fun updateLedger(ledger: LedgerEntity) {
        ledgerDao.updateLedger(ledger)
    }

    suspend fun deleteLedger(ledger: LedgerEntity) {
        ledgerDao.deleteLedger(ledger)
    }

    suspend fun deleteLedgersByIds(ids: List<Long>) {
        ledgerDao.deleteLedgersByIds(ids)
    }

    fun getAllLedgersDesc(bookId: Long): Flow<List<LedgerEntity>> {
        return ledgerDao.getAllLedgersDesc(bookId)
    }

    // ================== 统计分析相关 ==================
    // 注意：以下方法都必须带有 bookId 参数
    fun getLedgersBetween(bookId: Long, startTime: Long, endTime: Long): Flow<List<LedgerEntity>> {
        return ledgerDao.getLedgersBetween(bookId, startTime, endTime)
    }

    fun getTotalAmountBetween(bookId: Long, startTime: Long, endTime: Long, type: Int): Flow<Double?> {
        return ledgerDao.getTotalAmountBetween(bookId, startTime, endTime, type)
    }

    fun getCategorySumBetween(bookId: Long, startTime: Long, endTime: Long, type: Int = 0): Flow<List<CategorySum>> {
        return ledgerDao.getCategorySumBetween(bookId, startTime, endTime, type)
    }
}