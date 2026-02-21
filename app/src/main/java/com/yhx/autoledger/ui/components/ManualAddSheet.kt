package com.yhx.autoledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualAddSheet(
    onDismiss: () -> Unit,
    // ✨ 修改点 1：回调函数增加一个 String 参数，用于传递备注/标题
    onSave: (category: String, amount: String, remark: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf(
        "餐饮" to "🍱", "交通" to "🚗", "购物" to "🛒",
        "娱乐" to "🎮", "居住" to "🏠", "其他" to "⚙️"
    )

    var selectedCategory by remember { mutableStateOf(categories[0].first) }
    var amountText by remember { mutableStateOf("") }
    // ✨ 修改点 2：新增备注状态
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
            Text("手动记账", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))

            // 1. 金额输入区
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¥", fontSize = 32.sp, fontWeight = FontWeight.Black, color = AccentBlue)
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

            // ✨ 修改点 3：新增备注输入区 (保持和金额输入框一致的 UI 风格)
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
                                Text("添加备注（如：星巴克、打车费）", fontSize = 15.sp, color = Color.LightGray)
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. 分类选择区 (网格布局)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                items(categories) { (name, icon) ->
                    val isSelected = selectedCategory == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.bounceClick().clickable { selectedCategory = name }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) AccentBlue else Color.White,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(icon, fontSize = 24.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(name, fontSize = 12.sp, color = if (isSelected) AccentBlue else Color.Gray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 3. 保存按钮
            Button(
                onClick = {
                    if (amountText.isNotBlank()) {
                        // ✨ 修改点 4：如果用户没填备注，就默认用分类名作为标题（比如用户选了餐饮没写备注，那标题就是"餐饮"）
                        val finalRemark = if (remarkText.isNotBlank()) remarkText else selectedCategory
                        onSave(selectedCategory, amountText, finalRemark)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("保存一笔", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}