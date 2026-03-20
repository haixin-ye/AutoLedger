package com.yhx.autoledger.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yhx.autoledger.MainActivity
import com.yhx.autoledger.utils.ReminderHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.yhx.autoledger.data.repository.UserPreferencesRepository

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1. 发送通知
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "autoledger_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "记账提醒", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("记账时间到啦！ 📝")
            .setContentText("今天有发生什么开销吗？快来记录一下吧，保持好习惯哦~")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 强力弹窗提醒
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(1001, notification)

        // ✨ 2. 核心逻辑：因为 Exact 闹钟是一次性的，响完之后，我们马上再定明天同一时间的闹钟
        // 这里需要利用协程同步读取一下 DataStore 里的时间
        val prefs = UserPreferencesRepository(context)
        runBlocking {
            val savedTime = prefs.reminderTime.first()
            if (savedTime.isNotEmpty()) {
                ReminderHelper.scheduleReminder(context, savedTime)
            }
        }
    }
}