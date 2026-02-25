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
import com.yhx.autoledger.ui.theme.AppTheme // ✨ 引入全局主题
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
    onSave: (type: Int, category: String, icon: String, amount: Double, remark: String, timestamp: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val expenseCategories = listOf(
        "餐饮" to "🍱", "交通" to "🚗", "购物" to "🛒",
        "娱乐" to "🎮", "居住" to "🏠", "其他" to "⚙️"
    )
    val incomeCategories = listOf(
        "工资" to "💰", "理财" to "📈", "兼职" to "💼",
        "红包" to "🧧", "报销" to "🧾", "其他" to "💵"
    )

    val pagerState = rememberPagerState(initialPage = initialType) { 2 }
    val currentType = pagerState.currentPage

    var selectedExpenseCategory by remember {
        mutableStateOf(if (initialType == 0 && initialCategory != null) initialCategory else expenseCategories[0].first)
    }
    var selectedIncomeCategory by remember {
        mutableStateOf(if (initialType == 1 && initialCategory != null) initialCategory else incomeCategories[0].first)
    }

    var amountText by remember { mutableStateOf(initialAmount) }
    var remarkText by remember { mutableStateOf(initialRemark) }
    var selectedTimestamp by remember { mutableLongStateOf(initialTimestamp ?: System.currentTimeMillis()) }

    // ✨ 提取主题相关的核心动画颜色（支出 = 品牌蓝/或者你的红，这里我映射为你设定的 expenseColor/incomeColor）
    // 为了和之前的视觉一致，0(支出)用品牌色，1(收入)用绿色。
    val targetAnimColor = if (currentType == 0) AppTheme.colors.brandAccent else AppTheme.colors.incomeColor

    val animatedGlobalSymbolColor by animateColorAsState(
        targetValue = targetAnimColor,
        label = "global_color_anim"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // ✨ 映射弹窗背景色
        containerColor = AppTheme.colors.sheetBackground,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏
            if (isEditMode) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    // ✨ 映射主文本色
                    Text("修改账单", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AppTheme.colors.textPrimary)
                    if (onDelete != null) {
                        IconButton(
                            onClick = {
                                onDelete()
                                onDismiss()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            // ✨ 映射警示红
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除", tint = AppTheme.colors.warningRed)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // 收支切换器
            Row(
                modifier = Modifier
                    .width(200.dp)
                    // ✨ 映射切换器底槽
                    .background(AppTheme.colors.sheetTabBackground, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        // ✨ 映射选中背景
                        .background(if (currentType == 0) AppTheme.colors.sheetTabSelectedBg else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(0) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "支出",
                        fontWeight = if (currentType == 0) FontWeight.Bold else FontWeight.Normal,
                        // ✨ 映射文字颜色
                        color = if (currentType == 0) AppTheme.colors.sheetTabSelectedText else AppTheme.colors.sheetTabUnselectedText
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (currentType == 1) AppTheme.colors.sheetTabSelectedBg else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { coroutineScope.launch { pagerState.animateScrollToPage(1) } }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "收入",
                        fontWeight = if (currentType == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentType == 1) AppTheme.colors.sheetTabSelectedText else AppTheme.colors.sheetTabUnselectedText
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val isExpense = page == 0
                // ✨ 这里复用全局的强视觉色（收入/支出主题色）
                val pageSymbolColor = if (isExpense) AppTheme.colors.brandAccent else AppTheme.colors.incomeColor
                val pageCategories = if (isExpense) expenseCategories else incomeCategories
                val pageSelectedCategory = if (isExpense) selectedExpenseCategory else selectedIncomeCategory

                Column(modifier = Modifier.fillMaxWidth()) {
                    // 金额输入区
                    Surface(
                        // ✨ 映射输入框底色
                        color = AppTheme.colors.sheetInputBackground,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isExpense) "- ¥" else "+ ¥",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = pageSymbolColor
                            )
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = amountText, onValueChange = { if (it.length <= 8) amountText = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                // ✨ 映射金额文字颜色
                                textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = AppTheme.colors.textPrimary),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    // ✨ 映射弱提示文字颜色
                                    if (amountText.isEmpty()) Text("0.00", fontSize = 36.sp, color = AppTheme.colors.textTertiary) else inner()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // 日期选择组件
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            if (isEditMode) "修改日期：" else "交易日期：",
                            style = MaterialTheme.typography.bodyMedium,
                            // ✨ 映射次要文字颜色
                            color = AppTheme.colors.textSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        DateSelectorButton(
                            currentTimestamp = selectedTimestamp,
                            onDateSelected = { newTime -> selectedTimestamp = newTime })
                    }
                    Spacer(Modifier.height(8.dp))

                    // 备注输入区
                    Surface(
                        color = AppTheme.colors.sheetInputBackground, // ✨
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = AppTheme.colors.textTertiary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            BasicTextField(
                                value = remarkText, onValueChange = { remarkText = it },
                                textStyle = TextStyle(fontSize = 15.sp, color = AppTheme.colors.textPrimary), // ✨
                                modifier = Modifier.weight(1f), singleLine = true,
                                decorationBox = { inner ->
                                    if (remarkText.isEmpty()) Text("添加备注", fontSize = 15.sp, color = AppTheme.colors.textTertiary) else inner() // ✨
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
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(pageCategories) { (name, stdIcon) ->
                            val isSelected = pageSelectedCategory == name
                            val displayIcon = if (isSelected && pageSelectedCategory == initialCategory && initialIcon != null) initialIcon else stdIcon

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (isExpense) selectedExpenseCategory = name else selectedIncomeCategory = name
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    // ✨ 映射分类底色：选中时用主题色，未选中时用专门的未选中底色
                                    color = if (isSelected) pageSymbolColor else AppTheme.colors.sheetCategoryBgUnselected,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(displayIcon, fontSize = 24.sp)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    name,
                                    fontSize = 12.sp,
                                    // ✨ 映射分类文字
                                    color = if (isSelected) pageSymbolColor else AppTheme.colors.textSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

                        val finalCategory = if (currentType == 0) selectedExpenseCategory else selectedIncomeCategory
                        val finalCategoriesList = if (currentType == 0) expenseCategories else incomeCategories
                        val finalRemark = if (remarkText.isNotBlank()) remarkText else finalCategory

                        val finalIcon = if (finalCategory == initialCategory && initialIcon != null) {
                            initialIcon
                        } else {
                            finalCategoriesList.find { it.first == finalCategory }?.second ?: "⚙️"
                        }

                        onSave(currentType, finalCategory, finalIcon, parsedAmount, finalRemark, selectedTimestamp)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                // ✨ 按钮底色随滑动动画变色 (品牌蓝 <-> 收入绿)
                colors = ButtonDefaults.buttonColors(containerColor = animatedGlobalSymbolColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    if (isEditMode) "保存修改" else "保存一笔",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textOnAccent // ✨ 确保按钮上的字是白色的
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}