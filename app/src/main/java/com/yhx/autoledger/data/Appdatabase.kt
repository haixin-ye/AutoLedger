package com.yhx.autoledger.data

import android.R.attr.name
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yhx.autoledger.data.dao.AccountBookDao
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.LedgerDao
import com.yhx.autoledger.data.entity.AccountBookEntity
import com.yhx.autoledger.data.entity.CategoryEntity
import com.yhx.autoledger.data.entity.LedgerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [LedgerEntity::class, CategoryEntity::class, AccountBookEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ledgerDao(): LedgerDao
    abstract fun categoryDao(): CategoryDao

    abstract fun accountBookDao(): AccountBookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoledger_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.categoryDao()
                        // ✨ 核心修改：将 ic_xx 直接改为 Emoji 字符串
                        val defaultCategories = listOf(
                            // 支出类 (type = 0)
                            CategoryEntity(name = "餐饮", iconName = "🍱", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "交通", iconName = "🚗", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "购物", iconName = "🛒", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "娱乐", iconName = "🎮", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "居家", iconName = "🏠", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "还款", iconName = "💳", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "医疗", iconName = "💊", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "人情", iconName = "🧧", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "通讯", iconName = "📱", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "零食", iconName = "🍫", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "学习", iconName = "📚", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "宠物", iconName = "🐾", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "其它", iconName = "⚙️", type = 0, isSystemDefault = true),

                            // 收入类 (type = 1)
                            CategoryEntity(name = "工资", iconName = "💰", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "理财", iconName = "📈", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "兼职", iconName = "💼", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "红包", iconName = "🧧", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "报销", iconName = "🧾", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "退款", iconName = "🔄", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "奖金", iconName = "🏆", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "其它", iconName = "⚙️", type = 1, isSystemDefault = true)
                        )
                        dao.insertAll(defaultCategories)

                        val bookDao = database.accountBookDao()
                        val defaultBooks = listOf(
                            AccountBookEntity(
                                id = 1L,
                                name = "日常账本",
                                coverColor = 0xFF42A5F5.toInt(),
                                isSystemDefault = true
                            ),
                            AccountBookEntity(
                                id = 2L,
                                name = "生意账本",
                                coverColor = 0xFFFFA726.toInt(),
                                isSystemDefault = true
                            ),
                            AccountBookEntity(
                                id = 3L,
                                name = "旅行账本",
                                coverColor = 0xFF66BB6A.toInt(),
                                isSystemDefault = true
                            )
                        )
                        bookDao.insertAllBooks(defaultBooks)

                    }
                }
            }
        }
    }
}