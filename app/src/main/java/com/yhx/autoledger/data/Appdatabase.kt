package com.yhx.autoledger.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.dao.LedgerDao
import com.yhx.autoledger.data.entity.CategoryEntity
import com.yhx.autoledger.data.entity.LedgerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [LedgerEntity::class, CategoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ledgerDao(): LedgerDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 供 Hilt 调用的获取数据库实例的方法
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "autoledger_db"
                )
                    // 核心：添加回调，在数据库首次创建时注入默认数据
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // 数据库第一次被创建时触发
                INSTANCE?.let { database ->
                    // 必须在后台协程中执行插入操作
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.categoryDao()
                        // 预设的经典款式
                        val defaultCategories = listOf(
                            CategoryEntity(name = "餐饮", iconName = "ic_food", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "交通", iconName = "ic_transport", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "购物", iconName = "ic_shopping", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "娱乐", iconName = "ic_entertainment", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "居家", iconName = "ic_home", type = 0, isSystemDefault = true),
                            CategoryEntity(name = "工资", iconName = "ic_salary", type = 1, isSystemDefault = true),
                            CategoryEntity(name = "理财", iconName = "ic_investment", type = 1, isSystemDefault = true)
                        )
                        dao.insertAll(defaultCategories)
                    }
                }
            }
        }
    }
}