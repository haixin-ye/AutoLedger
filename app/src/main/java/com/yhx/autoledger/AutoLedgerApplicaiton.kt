package com.yhx.autoledger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // 这个注解极其重要！它告诉 Hilt 从这里开始接管整个应用
class AutoLedgerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 未来如果你有其他的第三方库（比如日志库、崩溃收集库）也可以在这里初始化
    }
}