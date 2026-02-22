package com.yhx.autoledger.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.R
import com.yhx.autoledger.ui.theme.AccentBlue

@Composable
fun AdvancedChatInput(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        color = Color.White,
        shape = CircleShape,
        shadowElevation = 12.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = Color(0xFF1D1D1F)),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("输入语音或文字记一笔...", color = Color(0xFFC7C7CC), fontSize = 15.sp)
                    }
                    innerTextField()
                }
            )

            val isInputEmpty = text.trim().isEmpty()
            IconButton(
                onClick = onSend,
                enabled = !isInputEmpty,
                modifier = Modifier
                    .size(40.dp)
                    .background(color = if (isInputEmpty) Color(0xFFF2F2F7) else AccentBlue, shape = CircleShape)
                    .bounceClick()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (isInputEmpty) Color(0xFFC7C7CC) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatAvatar(isFromUser: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isFromUser) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
        modifier = Modifier.size(34.dp).shadow(2.dp, CircleShape).border(1.dp, Color.White, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = if (isFromUser) R.drawable.ic_user_avatar else R.drawable.ic_ai_avatar),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}