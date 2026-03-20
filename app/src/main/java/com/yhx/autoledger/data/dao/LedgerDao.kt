package com.yhx.autoledger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yhx.autoledger.data.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    // 1. 基础写入与删除
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: LedgerEntity)

    @Delete
    suspend fun deleteLedger(ledger: LedgerEntity)

    //更新Dao
    @Update
    suspend fun updateLedger(ledger: LedgerEntity)

    // 1. 批量删除
    @Query("DELETE FROM ledgers WHERE id IN (:ids)")
    suspend fun deleteLedgersByIds(ids: List<Long>)

    // 2. 首页展示：获取所有账单（按时间倒序排列，最新的在最上面）
    @Query("SELECT * FROM ledgers WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getAllLedgersDesc(bookId: Long): Flow<List<LedgerEntity>>

    // 3. 明细页展示：获取特定时间段（某个月）的所有账单
    @Query("SELECT * FROM ledgers WHERE bookId = :bookId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLedgersBetween(bookId: Long, startTime: Long, endTime: Long): Flow<List<LedgerEntity>>

    // 4. 统计功能：计算特定时间段的总支出 (type = 0) 或 总收入 (type = 1)
    // 返回 Double? 是因为如果该月没有数据，SUM() 会返回 NULL
    @Query("SELECT SUM(amount) FROM ledgers WHERE bookId = :bookId AND type = :type AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalAmountBetween(bookId: Long, startTime: Long, endTime: Long, type: Int): Flow<Double?>

    // 5. 饼图核心：按分类统计某段时间的支出总和 (GROUP BY)
    @Query("""
        SELECT categoryName, categoryIcon, SUM(amount) as totalAmount 
        FROM ledgers 
        WHERE bookId = :bookId AND type = :type AND timestamp >= :startTime AND timestamp <= :endTime 
        GROUP BY categoryName 
        ORDER BY totalAmount DESC
    """)
    fun getCategorySumBetween(bookId: Long, startTime: Long, endTime: Long, type: Int = 0): Flow<List<CategorySum>>
}