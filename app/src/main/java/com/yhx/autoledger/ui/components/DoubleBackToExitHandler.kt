package com.yhx.autoledger.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DoubleBackToExitHandler() {
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // BackHandler 会自动拦截系统的侧滑返回或物理返回键
    BackHandler(enabled = true) {
        if (backPressedOnce) {
            // 如果已经在 2 秒内滑过一次了，直接退出 Activity
            (context as? Activity)?.finish()
        } else {
            // 第一次滑动：提示用户，并开启 2 秒的倒计时
            backPressedOnce = true
            Toast.makeText(context, "再按一次退出", Toast.LENGTH_SHORT).show()

            coroutineScope.launch {
                delay(2000L) // 2秒后如果没发生第二次滑动，重置状态
                backPressedOnce = false
            }
        }
    }
}