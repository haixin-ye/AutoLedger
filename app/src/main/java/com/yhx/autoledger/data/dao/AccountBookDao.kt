package com.yhx.autoledger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yhx.autoledger.data.entity.AccountBookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: AccountBookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBooks(books: List<AccountBookEntity>)

    @Delete
    suspend fun deleteBook(book: AccountBookEntity)

    @Query("SELECT * FROM account_books ORDER BY id ASC")
    fun getAllBooks(): Flow<List<AccountBookEntity>>

    // 在 AccountBookDao.kt 中必须有这个方法：
    @Query("SELECT * FROM account_books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): AccountBookEntity?
}