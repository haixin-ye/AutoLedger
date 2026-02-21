package com.yhx.autoledger.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.ui.components.bounceClick
import com.yhx.autoledger.ui.theme.AccentBlue
import com.yhx.autoledger.ui.theme.CategoryFood
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AIScreen() {
    val messages = remember { mutableStateListOf(
        ChatMessage("您好，我是 AI 助手。您可以试着对我说：'打车花了 30 元'，我会为您自动记账。", false)
    ) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 模拟背景渐变（Mesh Gradient 效果）
    val meshGradient = Brush.radialGradient(
        colors = listOf(Color(0xFFE0F2F1), Color(0xFFF7F9FC)),
        center = androidx.compose.ui.geometry.Offset(200f, 200f),
        radius = 1000f
    )

    Box(modifier = Modifier.fillMaxSize().background(meshGradient)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部 Header
            AIHeader()

            // 消息区域
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { msg ->
                    AdvancedChatBubble(msg)
                }
            }

            // 底部输入区域
            AdvancedChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        val userText = inputText
                        messages.add(ChatMessage(userText, true))
                        inputText = ""
                        scope.launch {
                            delay(800)
                            // 接口留位：此处接入 AI 逻辑
                            val response = ChatMessage(
                                content = "识别成功！已为您生成账单详情：",
                                isFromUser = false,
                                billPreview = BillPreview(
                                    "餐饮美食",
                                    "15.00",
                                    "2026-02-20",
                                    "🍜",
                                    CategoryFood
                                )
                            )
                            messages.add(response)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AIHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("AI Co-pilot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun AdvancedChatBubble(msg: ChatMessage) {
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
                .then(if (!msg.isFromUser) Modifier.border(0.5.dp, Color.White, RoundedCornerShape(20.dp)) else Modifier)
                .shadow(if (msg.isFromUser) 8.dp else 2.dp, shape = RoundedCornerShape(20.dp), spotColor = AccentBlue)
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
            AnimatedVisibility(visible = true, enter = expandVertically() + fadeIn()) {
                ReceiptCard(preview)
            }
        }
    }
}

@Composable
fun ReceiptCard(preview: BillPreview) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), color = preview.color.copy(alpha = 0.1f), shape = CircleShape) {
                    Box(contentAlignment = Alignment.Center) { Text(preview.icon, fontSize = 20.sp) }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(preview.category, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(preview.date, fontSize = 11.sp, color = Color.Gray)
                }
                Text("¥${preview.amount}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AccentBlue)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("确认归档", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
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
        Box(modifier = Modifier.fillMaxSize().blur(10.dp))

        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) Text("帮我记一笔...", color = Color.Gray.copy(alpha = 0.7f), fontSize = 15.sp)
                    innerTextField()
                }
            )
            IconButton(
                onClick = onSend,
                modifier = Modifier.size(42.dp).background(AccentBlue, CircleShape).bounceClick()
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}