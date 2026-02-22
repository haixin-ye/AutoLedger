package com.yhx.autoledger.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.AccentBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseTransactionSheet(
    isEditMode: Boolean = false, // 用于区分是新增还是编辑
    initialType: Int = 0,
    initialAmount: String = "",
    initialCategory: String? = null,
    initialIcon: String? = null, // 接收可能的 AI 专属图标
    initialRemark: String = "",
    initialTimestamp: Long? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    // 统一抛出清洗后的标准数据
    onSave: (type: Int, category: String, icon: String, amount: Double, remark: String, timestamp: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories = listOf(
        "餐饮" to "🍱",
        "交通" to "🚗",
        "购物" to "🛒",
        "娱乐" to "🎮",
        "居住" to "🏠",
        "其他" to "⚙️"
    )
    val incomeCategories = listOf(
        "工资" to "💰",
        "理财" to "📈",
        "兼职" to "💼",
        "红包" to "🧧",
        "报销" to "🧾",
        "其他" to "💵"
    )

    // --- 状态管理 ---
    var transactionType by remember { mutableStateOf(initialType) }
    val currentCategories = if (transactionType == 0) expenseCategories else incomeCategories

    // 如果没有初始分类，默认取当前类型的第一项
    var selectedCategory by remember {
        mutableStateOf(
            initialCategory ?: currentCategories[0].first
        )
    }
    var amountText by remember { mutableStateOf(initialAmount) }
    var remarkText by remember { mutableStateOf(initialRemark) }
    var selectedTimestamp by remember {
        mutableLongStateOf(
            initialTimestamp ?: System.currentTimeMillis()
        )
    }

    // 当用户切换收支类型时，如果当前选中的分类不在新列表中，自动重置为新列表的第一项
    LaunchedEffect(transactionType) {
        if (currentCategories.none { it.first == selectedCategory }) {
            selectedCategory = currentCategories[0].first
        }
    }

    // --- UI 渲染 ---
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9FC),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏：仅在编辑模式下显示标题和删除按钮
            if (isEditMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("修改账单", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    if (onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "删除",
                                tint = Color.Red
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 收支切换器
            Row(
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (transactionType == 0) Color.White else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { transactionType = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "支出",
                        fontWeight = if (transactionType == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (transactionType == 0) Color.Black else Color.Gray
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (transactionType == 1) Color.White else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { transactionType = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "收入",
                        fontWeight = if (transactionType == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (transactionType == 1) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 金额输入区
            val symbolColor = if (transactionType == 0) AccentBlue else Color(0xFF4CAF50)
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (transactionType == 0) "- ¥" else "+ ¥",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = symbolColor
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = amountText, onValueChange = { if (it.length <= 8) amountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (amountText.isEmpty()) Text(
                                "0.00",
                                fontSize = 36.sp,
                                color = Color.LightGray
                            ) else inner()
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // 日期选择组件
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    if (isEditMode) "修改日期：" else "交易日期：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                DateSelectorButton(
                    currentTimestamp = selectedTimestamp,
                    onDateSelected = { newTime -> selectedTimestamp = newTime })
            }
            Spacer(Modifier.height(8.dp))

            // 备注输入区
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = remarkText, onValueChange = { remarkText = it },
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        modifier = Modifier.weight(1f), singleLine = true,
                        decorationBox = { inner ->
                            if (remarkText.isEmpty()) Text(
                                "添加备注",
                                fontSize = 15.sp,
                                color = Color.LightGray
                            ) else inner()
                        }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // 分类选择区
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(currentCategories) { (name, stdIcon) ->
                    val isSelected = selectedCategory == name
                    // 💡 核心逻辑：如果在编辑模式下，且当前分类没有被修改，优先展示传入的 AI 专属图标
                    val displayIcon =
                        if (isSelected && selectedCategory == initialCategory && initialIcon != null) initialIcon else stdIcon

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }, // 记住交互状态
                            indication = null // 关键：将点击的视觉反馈设为空，去掉方形阴影
                        ) { selectedCategory = name }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) symbolColor else Color.White,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    displayIcon,
                                    fontSize = 24.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            name,
                            fontSize = 12.sp,
                            color = if (isSelected) symbolColor else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // 保存按钮
            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val finalRemark =
                            if (remarkText.isNotBlank()) remarkText else selectedCategory

                        // 💡 导出图标逻辑：如果分类没变且有初始图标，就保留；否则取列表中对应的新图标
                        val finalIcon =
                            if (selectedCategory == initialCategory && initialIcon != null) {
                                initialIcon
                            } else {
                                currentCategories.find { it.first == selectedCategory }?.second
                                    ?: "⚙️"
                            }

                        onSave(
                            transactionType,
                            selectedCategory,
                            finalIcon,
                            parsedAmount,
                            finalRemark,
                            selectedTimestamp
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = symbolColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (isEditMode) "保存修改" else "保存一笔",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}