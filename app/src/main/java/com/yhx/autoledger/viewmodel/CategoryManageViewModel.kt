package com.yhx.autoledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.dao.CategoryDao
import com.yhx.autoledger.data.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.map


@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val categoryDao: CategoryDao
) : ViewModel() {

    // 1. 监听所有分类数据
    private val allCategories = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. 派生状态：自动过滤出“支出 (0)”和“收入 (1)”的列表
    // ✨ 2. 核心修改：使用 sortedBy 强行把“其他”踢到列表最后面
    val expenseCategories = allCategories.map { list ->
        list.filter { it.type == 0 }
            .sortedBy { if (it.name == "其他") 1 else 0 }
    }

    val incomeCategories = allCategories.map { list ->
        list.filter { it.type == 1 }
            .sortedBy { if (it.name == "其他") 1 else 0 }
    }

    // ✨ 3. 添加自定义分类 (强制绑定 isSystemDefault = false)
    fun addCustomCategory(name: String, iconName: String, type: Int) {
        if (name.isBlank() || iconName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.insertCategory(
                CategoryEntity(
                    name = name,
                    iconName = iconName, // 这里存入用户选中的 Emoji 字符串
                    type = type,
                    isSystemDefault = false // 核心：用户自己建的绝对不是系统预设
                )
            )
        }
    }

    // ✨ 4. 删除分类 (基于 isSystemDefault 字段进行二次拦截)
    fun deleteCategory(category: CategoryEntity) {
        // 安全拦截：如果是系统默认分类，直接拒绝执行删除操作！
        if (category.isSystemDefault) return

        viewModelScope.launch(Dispatchers.IO) {
            // 需要确保您的 CategoryDao 中有 @Delete suspend fun deleteCategory(category: CategoryEntity) 方法
            categoryDao.deleteCategory(category)
        }
    }
}