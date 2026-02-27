package com.yhx.autoledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.data.entity.CategoryEntity
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AppDesignSystem
import com.yhx.autoledger.utils.CategoryIconUtils
import com.yhx.autoledger.viewmodel.CategoryManageViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onBack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0:支出, 1:收入
    var showAddSheet by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    // 收集数据流
    val expenseList by viewModel.expenseCategories.collectAsState(initial = emptyList())
    val incomeList by viewModel.incomeCategories.collectAsState(initial = emptyList())
    val currentList = if (selectedTab == 0) expenseList else incomeList

    Scaffold(
        containerColor = AppDesignSystem.colors.appBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = AppDesignSystem.colors.brandAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.bounceClick()
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "新增分类",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            // 1. 顶部导航与分段控件
            HeaderWithTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onBack = onBack
            )

            // 2. 分类网格展示
            LazyVerticalGrid(
                columns = GridCells.Fixed(4), // 每行 4 个图标
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentList) { category ->
                    CategoryItemGrid(
                        category = category,
                        onDeleteClick = { categoryToDelete = category }
                    )
                }
            }
        }

        // 3. 删除确认弹窗
        if (categoryToDelete != null) {
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                containerColor = AppDesignSystem.colors.cardBackground,
                title = {
                    Text(
                        "删除分类",
                        color = AppDesignSystem.colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "确定要删除 [${categoryToDelete?.name}] 吗？",
                        color = AppDesignSystem.colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCategory(categoryToDelete!!)
                        categoryToDelete = null
                    }) { Text("删除", color = AppDesignSystem.colors.warningRed) }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) {
                        Text(
                            "取消",
                            color = AppDesignSystem.colors.textPrimary
                        )
                    }
                }
            )
        }

        // 4. 新增分类的底部抽屉
        if (showAddSheet) {
            AddCategorySheet(
                currentType = selectedTab,
                onDismiss = { showAddSheet = false },
                onSave = { name, icon ->
                    viewModel.addCustomCategory(name, icon, selectedTab)
                    showAddSheet = false
                }
            )
        }
    }
}

// ============== 内部专属 UI 组件 ==============

@Composable
private fun HeaderWithTabs(selectedTab: Int, onTabSelected: (Int) -> Unit, onBack: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(AppDesignSystem.colors.cardBackground)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    contentDescription = null,
                    tint = AppDesignSystem.colors.textPrimary
                )
            }
            Text(
                "分类管理",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = AppDesignSystem.colors.textPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 高级感 Tab 切换槽
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(AppDesignSystem.colors.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("支出", "收入").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AppDesignSystem.colors.cardBackground else Color.Transparent)
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (isSelected) AppDesignSystem.colors.textPrimary else AppDesignSystem.colors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItemGrid(category: CategoryEntity, onDeleteClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 图标底框
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = AppDesignSystem.colors.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // ✨ 映射到您的 iconName 字段
                    Text(text = category.iconName, fontSize = 28.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 分类名称
            Text(
                text = category.name,
                fontSize = 12.sp,
                color = AppDesignSystem.colors.textPrimary,
                maxLines = 1
            )
        }

        // ✨ 核心逻辑映射：依据您的 isSystemDefault 判断是否显示“删除小红叉”
        if (!category.isSystemDefault) {
            Surface(
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 6.dp, y = (-4).dp)
                    .clickable { onDeleteClick() },
                shape = CircleShape,
                color = AppDesignSystem.colors.warningRed,
                shadowElevation = 2.dp
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "删除",
                    tint = Color.White,
                    modifier = Modifier.padding(3.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategorySheet(
    currentType: Int,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🍱") }

    // 🔥 修复点 1：强制跳过“半展开”状态，直接完全展开，解决下方显示不全的问题！
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState, // 应用展开状态
        containerColor = AppDesignSystem.colors.sheetBackground, // (如果飘红请改为 AppTheme.colors)
        dragHandle = { BottomSheetDefaults.DragHandle() },
        // 🔥 修复点 2：将独立 Window 的边距控制权交给系统软键盘！
        windowInsets = WindowInsets.ime
    ) {
        // 外部大容器
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // 避开底部手势小白条
                .imePadding()            // 配合 WindowInsets.ime，实现丝滑上推
        ) {
            // 内部滚动内容区 (带权重，键盘弹起时自动压缩此区域的高度)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false) // 🔥 修复点 3：fill=false 允许被挤压
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = if (currentType == 0) "新增支出分类" else "新增收入分类",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppDesignSystem.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))

                // 输入框
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { if (it.length <= 6) categoryName = it },
                    placeholder = { Text("分类名称 (最多6个字)", color = AppDesignSystem.colors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppDesignSystem.colors.brandAccent,
                        unfocusedBorderColor = AppDesignSystem.colors.dividerColor,
                        focusedTextColor = AppDesignSystem.colors.textPrimary,
                        unfocusedTextColor = AppDesignSystem.colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("选择图标", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppDesignSystem.colors.textSecondary)
                Spacer(modifier = Modifier.height(12.dp))

                // 图标选择网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(200.dp), // 固定网格高度，避免太长
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(CategoryIconUtils.availableIcons) { icon ->
                        val isSelected = selectedIcon == icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) AppDesignSystem.colors.brandAccent.copy(alpha = 0.2f) else AppDesignSystem.colors.surfaceVariant)
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 24.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 吸底保存按钮区 (永远在最下方，键盘弹起时被整体上推)
            Button(
                onClick = { onSave(categoryName, selectedIcon) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
                    .height(50.dp)
                    .bounceClick(),
                enabled = categoryName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppDesignSystem.colors.brandAccent,
                    disabledContainerColor = AppDesignSystem.colors.dividerColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存分类", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}