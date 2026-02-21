package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun ManualAddSheet(
    onDismiss: () -> Unit,
    // ✨ 修改点 1：回调函数增加一个 Int 类型的 type (0代表支出，1代表收入)
    onSave: (type: Int, category: String, amount: String, remark: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ✨ 修改点 2：拆分支出和收入的分类列表
    val expenseCategories = listOf(
        "餐饮" to "🍱", "交通" to "🚗", "购物" to "🛒",
        "娱乐" to "🎮", "居住" to "🏠", "其他" to "⚙️"
    )
    val incomeCategories = listOf(
        "工资" to "💰", "理财" to "📈", "兼职" to "💼",
        "红包" to "🧧", "报销" to "🧾", "其他" to "💵"
    )

    // ✨ 修改点 3：记录当前选择的是支出(0)还是收入(1)
    var transactionType by remember { mutableStateOf(0) }

    // 根据当前的收支类型，动态决定显示哪个分类列表
    val currentCategories = if (transactionType == 0) expenseCategories else incomeCategories

    // 每次切换收支类型时，自动选中列表里的第一个分类
    var selectedCategory by remember(transactionType) { mutableStateOf(currentCategories[0].first) }
    var amountText by remember { mutableStateOf("") }
    var remarkText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9FC),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✨ 修改点 4：顶部的 支出/收入 优雅切换器
            Row(
                modifier = Modifier
                    .width(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                // 支出按钮
                Box(
                    modifier = Modifier.weight(1f)
                        .background(if (transactionType == 0) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { transactionType = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("支出", fontWeight = if (transactionType == 0) FontWeight.Bold else FontWeight.Normal, color = if (transactionType == 0) Color.Black else Color.Gray)
                }
                // 收入按钮
                Box(
                    modifier = Modifier.weight(1f)
                        .background(if (transactionType == 1) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { transactionType = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("收入", fontWeight = if (transactionType == 1) FontWeight.Bold else FontWeight.Normal, color = if (transactionType == 1) Color.Black else Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 1. 金额输入区 (根据收支改变符号颜色)
            val symbolColor = if (transactionType == 0) AccentBlue else Color(0xFF4CAF50) // 支出蓝色，收入绿色
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (transactionType == 0) "- ¥" else "+ ¥", fontSize = 28.sp, fontWeight = FontWeight.Black, color = symbolColor)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.length <= 8) amountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.Black),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (amountText.isEmpty()) {
                                Text("0.00", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.LightGray)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. 备注输入区
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = remarkText,
                        onValueChange = { remarkText = it },
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (remarkText.isEmpty()) {
                                Text("添加备注", fontSize = 15.sp, color = Color.LightGray)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. 分类选择区 (动态使用 currentCategories)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                items(currentCategories) { (name, icon) ->
                    val isSelected = selectedCategory == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedCategory = name }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) symbolColor else Color.White,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 24.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(name, fontSize = 12.sp, color = if (isSelected) symbolColor else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 4. 保存按钮
            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        val finalRemark = if (remarkText.isNotBlank()) remarkText else selectedCategory
                        // ✨ 传出 type 参数
                        onSave(transactionType, selectedCategory, amountText, finalRemark)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = symbolColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("保存一笔", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}