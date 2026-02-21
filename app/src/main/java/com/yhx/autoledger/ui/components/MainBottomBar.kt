package com.yhx.autoledger.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yhx.autoledger.ui.navigation.bottomNavItems

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    // 使用 Surface 提供一个带阴影和毛玻璃感的底座
    Surface(
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(80.dp)
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(screen.route) },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
//                            modifier = Modifier.bounceClick() // 再次复用你的缩放动效
                        )
                    },
                    label = { Text(screen.title, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF74EBD5), // 使用你的渐变主色
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF74EBD5).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}