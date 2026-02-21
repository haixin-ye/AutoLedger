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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.ui.theme.AccentBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLedgerSheet(
    initialLedger: LedgerEntity, // 传入需要修改的账单
    onDismiss: () -> Unit,
    onSave: (LedgerEntity) -> Unit, // 保存更新
    onDelete: (LedgerEntity) -> Unit // 删除
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val expenseCategories = listOf("餐饮" to "🍱", "交通" to "🚗", "购物" to "🛒", "娱乐" to "🎮", "居住" to "🏠", "其他" to "⚙️")
    val incomeCategories = listOf("工资" to "💰", "理财" to "📈", "兼职" to "💼", "红包" to "🧧", "报销" to "🧾", "其他" to "💵")

    // 数据回填
    var transactionType by remember { mutableStateOf(initialLedger.type) }
    val currentCategories = if (transactionType == 0) expenseCategories else incomeCategories
    var selectedCategory by remember { mutableStateOf(initialLedger.categoryName) }

    // 把金额转成字符串（去掉 .0 这种不好看的后缀）
    val initialAmountStr = if (initialLedger.amount % 1.0 == 0.0) initialLedger.amount.toInt().toString() else initialLedger.amount.toString()
    var amountText by remember { mutableStateOf(initialAmountStr) }

    // 如果备注和分类名一样，说明当时没填备注，这里就留空
    val initialRemark = if (initialLedger.note == initialLedger.categoryName) "" else initialLedger.note
    var remarkText by remember { mutableStateOf(initialRemark) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF7F9FC)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏：包含 删除按钮 和 标题
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("修改账单", fontSize = 18.sp, fontWeight = FontWeight.Black)
                // 垃圾桶删除按钮
                IconButton(
                    onClick = {
                        onDelete(initialLedger)
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除", tint = Color.Red)
                }
            }
            Spacer(Modifier.height(16.dp))

            // 收支切换器
            Row(modifier = Modifier.width(200.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)).padding(4.dp)) {
                Box(modifier = Modifier.weight(1f).background(if (transactionType == 0) Color.White else Color.Transparent, RoundedCornerShape(12.dp)).clickable { transactionType = 0 }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text("支出", fontWeight = if (transactionType == 0) FontWeight.Bold else FontWeight.Normal, color = if (transactionType == 0) Color.Black else Color.Gray)
                }
                Box(modifier = Modifier.weight(1f).background(if (transactionType == 1) Color.White else Color.Transparent, RoundedCornerShape(12.dp)).clickable { transactionType = 1 }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text("收入", fontWeight = if (transactionType == 1) FontWeight.Bold else FontWeight.Normal, color = if (transactionType == 1) Color.Black else Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 1. 金额输入区
            val symbolColor = if (transactionType == 0) AccentBlue else Color(0xFF4CAF50)
            Surface(color = Color.White, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().height(80.dp)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (transactionType == 0) "- ¥" else "+ ¥", fontSize = 28.sp, fontWeight = FontWeight.Black, color = symbolColor)
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = amountText, onValueChange = { if (it.length <= 8) amountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color.Black),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner -> if (amountText.isEmpty()) Text("0.00", fontSize = 36.sp, color = Color.LightGray) else inner() }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // 2. 备注输入区
            Surface(color = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = remarkText, onValueChange = { remarkText = it },
                        textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                        modifier = Modifier.weight(1f), singleLine = true,
                        decorationBox = { inner -> if (remarkText.isEmpty()) Text("添加备注", fontSize = 15.sp, color = Color.LightGray) else inner() }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // 3. 分类选择区
            LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().height(160.dp)) {
                items(currentCategories) { (name, icon) ->
                    val isSelected = selectedCategory == name
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedCategory = name }) {
                        Surface(shape = CircleShape, color = if (isSelected) symbolColor else Color.White, modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 24.sp) }
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
                        val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                        val finalRemark = if (remarkText.isNotBlank()) remarkText else selectedCategory
                        // 提取选中的图标
                        val icon = currentCategories.find { it.first == selectedCategory }?.second ?: "⚙️"

                        // 组装修改后的 LedgerEntity (保留原来的 id 和 timestamp)
                        val updatedLedger = initialLedger.copy(
                            amount = parsedAmount,
                            type = transactionType,
                            categoryName = selectedCategory,
                            categoryIcon = icon,
                            note = finalRemark
                        )
                        onSave(updatedLedger)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = symbolColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("保存修改", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}