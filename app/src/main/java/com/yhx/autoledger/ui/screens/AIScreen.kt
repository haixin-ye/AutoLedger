package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.viewmodel.AIViewModel

@Composable
fun AIScreen(viewModel: AIViewModel = hiltViewModel()) {

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    // ✨ 状态：记录当前正在被编辑的消息，如果为 null 则不显示弹窗
    var editingMsg by remember { mutableStateOf<ChatMessage?>(null) }

    // 模拟背景渐变（Mesh Gradient 效果）
    val meshGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFE0F2F1), Color(0xFFF7F9FC)),
        center = androidx.compose.ui.geometry.Offset(200f, 200f),
        radius = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(meshGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部 Header
            AIHeader()

            // 消息区域
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { msg ->
                    // ✨ 修复飘红：现在我们传递的是完整的 msg，而不是 preview
                    AdvancedChatBubble(
                        msg = msg,
                        onSave = { currentMsg -> viewModel.confirmAndSaveLedger(currentMsg) },
                        // ✨ 当用户点击卡片右上角的修改按钮时，记录这条消息
                        onEdit = { currentMsg -> editingMsg = currentMsg }
                    )
                }

                // 加载中的 UI 反馈
                if (isLoading) {
                    item {
                        Text(
                            "AI 正在思考中...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            // 底部输入区域
            AdvancedChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                }
            )
        }

        // ✨ 在 Box 的最顶层，监听如果有需要编辑的消息，就弹出 BottomSheet
        editingMsg?.let { msgToEdit ->
            msgToEdit.billPreview?.let { preview ->
                EditAIBillSheet(
                    preview = preview,
                    onDismiss = { editingMsg = null }, // 关闭弹窗
                    onSave = { updatedPreview ->
                        // 调用 ViewModel 去刷新这条消息的预览数据
                        viewModel.updateMessagePreview(msgToEdit.id, updatedPreview)
                        // 关闭弹窗
                        editingMsg = null
                    }
                )
            }
        }
    }
}

@Composable
fun AIHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "AI Co-pilot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AdvancedChatBubble(msg: ChatMessage, onSave: (ChatMessage) -> Unit, onEdit: (ChatMessage) -> Unit) {
    val alignment = if (msg.isFromUser) Alignment.End else Alignment.Start

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        // 玻璃感气泡
        Surface(
            color = if (msg.isFromUser) AccentBlue else Color.White.copy(alpha = 0.7f),
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (msg.isFromUser) 20.dp else 4.dp,
                bottomEnd = if (msg.isFromUser) 4.dp else 20.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .then(
                    if (!msg.isFromUser) Modifier.border(
                        0.5.dp,
                        Color.White,
                        RoundedCornerShape(20.dp)
                    ) else Modifier
                )
                .shadow(
                    if (msg.isFromUser) 8.dp else 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = AccentBlue
                )
        ) {
            Text(
                text = msg.content,
                modifier = Modifier.padding(14.dp, 10.dp),
                color = if (msg.isFromUser) Color.White else Color.Black.copy(alpha = 0.8f),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }

        // 结构化结果展示（像一张精致的小收据）
        msg.billPreview?.let { preview ->
            Spacer(Modifier.height(10.dp))
            AnimatedVisibility(visible = true) {
                // ✨ 确保这里传给 ReceiptCard 的是完整的 msg
                ReceiptCard(msg = msg, onConfirm = { onSave(msg) }, onEdit = { onEdit(msg) })
            }
        }
    }
}

@Composable
fun ReceiptCard(msg: ChatMessage, onConfirm: () -> Unit, onEdit: () -> Unit) {
    val preview = msg.billPreview ?: return

    // ✨ 从真正的数据流里读取是否已保存，再也不会因为切屏丢失！
    val isConfirmed = msg.isSaved

    // ✨ 智能日期格式化：将 "2026-02-21" 转为 "2026-02-21 (周六)"
    val displayDate = remember(preview.date) {
        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateObj = sdf.parse(preview.date)
            // 使用 EEEE 获取星期几
            val outSdf = java.text.SimpleDateFormat("yyyy-MM-dd (EEEE)", java.util.Locale.CHINESE)
            if (dateObj != null) outSdf.format(dateObj) else preview.date
        } catch (e: Exception) {
            preview.date
        }
    }

    Box(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // ✨ 右上角的编辑按钮：只要还没确认归档，就可以点！
        if (!isConfirmed) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = "修改", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }

        Column {
            // 顶部：图标与金额大字
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = preview.color.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(preview.icon, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(if (preview.type == 0) "支出" else "收入", fontSize = 12.sp, color = Color.Gray)
                    Text("¥ ${preview.amount}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ✨ 中部：直观的详情列表区（小票样式）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F9FC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // 备注
                val finalNote = preview.note.ifBlank { preview.category }
                DetailRow(label = "备注", value = finalNote)

                // 分类
                DetailRow(label = "分类", value = preview.category)

                // 日期 + 星期
                DetailRow(label = "日期", value = displayDate)
            }

            Spacer(Modifier.height(16.dp))

            // 底部：保存按钮 (严格对接 isConfirmed)
            Button(
                onClick = onConfirm,
                enabled = !isConfirmed, // 按钮置灰控制
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    disabledContainerColor = Color(0xFFD0D0D0)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (isConfirmed) "✅ 已归档" else "确认归档",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConfirmed) Color.White else Color.White
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(text = value, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AdvancedChatInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(60.dp),
        color = Color.White.copy(alpha = 0.6f),
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
        shadowElevation = 0.dp
    ) {
        // 毛玻璃模糊底层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) Text(
                        "帮我记一笔...",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                    innerTextField()
                }
            )
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(42.dp)
                    .background(AccentBlue, CircleShape)
                    .bounceClick()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAIBillSheet(
    preview: BillPreview,
    onDismiss: () -> Unit,
    onSave: (BillPreview) -> Unit
) {
    // 提取原有的状态，供用户修改
    var amountText by remember { mutableStateOf(preview.amount) }
    var categoryText by remember { mutableStateOf(preview.category) }
    var noteText by remember { mutableStateOf(preview.note) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text("微调账单信息", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(20.dp))

            // 修改金额
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("金额") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // 修改分类
            OutlinedTextField(
                value = categoryText,
                onValueChange = { categoryText = it },
                label = { Text("分类") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // 修改备注
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))

            // 保存修改按钮
            Button(
                onClick = {
                    // 把用户修改后的值 copy 回去传给上层
                    val updatedPreview = preview.copy(
                        amount = amountText,
                        category = categoryText,
                        note = noteText
                    )
                    onSave(updatedPreview)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("保存修改", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}