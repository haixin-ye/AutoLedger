package com.yhx.autoledger.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.CategoryEntity
import com.yhx.autoledger.ui.theme.AppDesignSystem // ✨ 引入全局主题
import com.yhx.autoledger.viewmodel.CategoryManageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BaseTransactionSheet(
    isEditMode: Boolean = false,
    initialType: Int = 0,
    initialAmount: String = "",
    initialCategory: String? = null,
    initialIcon: String? = null,
    initialRemark: String = "",
    initialTimestamp: Long? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (type: Int, category: String, icon: String, amount: Double, remark: String, timestamp: Long) -> Unit,
    categoryViewModel: CategoryManageViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // 1. 动态加载数据库中的真实分类流
    val expenseList by categoryViewModel.expenseCategories.collectAsState(initial = emptyList())
    val incomeList by categoryViewModel.incomeCategories.collectAsState(initial = emptyList())

    val pagerState = rememberPagerState(initialPage = initialType) { 2 }
    val currentType = pagerState.currentPage

    // 获取当前页面(支出/收入)对应的分类列表
    val currentCategories = if (currentType == 0) expenseList else incomeList

    // 2. 状态管理改为基于 CategoryEntity 对象，抛弃原有的硬编码变量
    var selectedCategoryEntity by remember { mutableStateOf<CategoryEntity?>(null) }

    // 当列表数据加载完成或切换 Tab 时，自动选中默认项（用于回显或默认选中第一项）
    LaunchedEffect(currentCategories, currentType) {
        if (currentCategories.isNotEmpty()) {
            selectedCategoryEntity = currentCategories.find { it.name == initialCategory } ?: currentCategories.first()
        }
    }

    var amountText by remember { mutableStateOf(initialAmount) }
    var remarkText by remember { mutableStateOf(initialRemark) }
    var selectedTimestamp by remember { mutableLongStateOf(initialTimestamp ?: System.currentTimeMillis()) }

    // ✨ 提取主题相关的核心动画颜色
    val targetAnimColor = if (currentType == 0) AppDesignSystem.colors.brandAccent else AppDesignSystem.colors.incomeColor
    val animatedGlobalSymbolColor by animateColorAsState(targetValue = targetAnimColor, label = "global_color_anim")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // ✨ 映射弹窗背景色
        containerColor = AppDesignSystem.colors.sheetBackground,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏 (编辑模式)
            if (isEditMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("修改账单", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppDesignSystem.colors.textPrimary)
                    if (onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除", tint = AppDesignSystem.colors.warningRed)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 收支切换器
            Row(
                modifier = Modifier
                    .width(200.dp)
                    .background(AppDesignSystem.colors.sheetTabBackground, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (currentType == 0) AppDesignSystem.colors.sheetTabSelectedBg else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("支出", fontWeight = if (currentType == 0) FontWeight.Bold else FontWeight.Normal, color = if (currentType == 0) AppDesignSystem.colors.sheetTabSelectedText else AppDesignSystem.colors.sheetTabUnselectedText)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (currentType == 1) AppDesignSystem.colors.sheetTabSelectedBg else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("收入", fontWeight = if (currentType == 1) FontWeight.Bold else FontWeight.Normal, color = if (currentType == 1) AppDesignSystem.colors.sheetTabSelectedText else AppDesignSystem.colors.sheetTabUnselectedText)
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val isExpense = page == 0
                val pageSymbolColor = if (isExpense) AppDesignSystem.colors.brandAccent else AppDesignSystem.colors.incomeColor

                Column(modifier = Modifier.fillMaxWidth()) {
                    // 金额输入区
                    Surface(
                        color = AppDesignSystem.colors.sheetInputBackground,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isExpense) "- ¥" else "+ ¥", fontSize = 28.sp, fontWeight = FontWeight.Black, color = pageSymbolColor)
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = amountText, onValueChange = { if (it.length <= 8) amountText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = AppDesignSystem.colors.textPrimary),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (amountText.isEmpty()) Text("0.00", fontSize = 36.sp, color = AppDesignSystem.colors.textTertiary) else inner()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // 日期选择组件
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(if (isEditMode) "修改日期：" else "交易日期：", style = MaterialTheme.typography.bodyMedium, color = AppDesignSystem.colors.textSecondary)
                        Spacer(modifier = Modifier.weight(1f))
                        DateSelectorButton(currentTimestamp = selectedTimestamp, onDateSelected = { newTime -> selectedTimestamp = newTime })
                    }
                    Spacer(Modifier.height(8.dp))

                    // 备注输入区
                    Surface(
                        color = AppDesignSystem.colors.sheetInputBackground,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = AppDesignSystem.colors.textTertiary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = remarkText, onValueChange = { remarkText = it },
                                textStyle = TextStyle(fontSize = 15.sp, color = AppDesignSystem.colors.textPrimary),
                                modifier = Modifier.weight(1f), singleLine = true,
                                decorationBox = { inner ->
                                    if (remarkText.isEmpty()) Text("添加备注", fontSize = 15.sp, color = AppDesignSystem.colors.textTertiary) else inner()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // ✨ 分类选择区 (全面挂靠数据库流)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(currentCategories) { category ->
                            // 判断当前遍历的分类是否被选中
                            val isSelected = selectedCategoryEntity?.id == category.id

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() }, indication = null
                                ) {
                                    selectedCategoryEntity = category
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    // ✨ 修复颜色变量错误，统一使用 AppDesignSystem
                                    color = if (isSelected) pageSymbolColor else AppDesignSystem.colors.sheetCategoryBgUnselected,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // ✨ 关键点：使用 CategoryIcon 来智能渲染 Emoji 或 图片资源
                                        CategoryIcon(
                                            iconName = category.iconName,
                                            modifier = Modifier.size(26.dp),
                                            tint = if (isSelected) Color.White else AppDesignSystem.colors.textPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = category.name,
                                    fontSize = 12.sp,
                                    color = if (isSelected) pageSymbolColor else AppDesignSystem.colors.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // ✨ 保存逻辑重构
            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

                        // 从目前选中的 Entity 中取出数据，抛弃旧的映射表
                        selectedCategoryEntity?.let { entity ->
                            val finalRemark = if (remarkText.isNotBlank()) remarkText else entity.name

                            onSave(
                                currentType,
                                entity.name,
                                entity.iconName, // 将真实的图标标识符抛出
                                parsedAmount,
                                finalRemark,
                                selectedTimestamp
                            )
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = animatedGlobalSymbolColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (isEditMode) "保存修改" else "保存一笔",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppDesignSystem.colors.textOnAccent
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}