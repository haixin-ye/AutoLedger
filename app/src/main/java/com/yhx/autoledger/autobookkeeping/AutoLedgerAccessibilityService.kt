package com.yhx.autoledger.autobookkeeping

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.yhx.autoledger.R
import com.yhx.autoledger.autobookkeeping.core.ParsedBill
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.network.LlmApiService
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.Message
import com.yhx.autoledger.models.ResponseFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class AutoLedgerAccessibilityService : AccessibilityService() {

    @Inject lateinit var repository: LedgerRepository
    @Inject lateinit var apiService: LlmApiService

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val apiKey = "Bearer sk-b93a79d60e6445f89a214968e9273d71"

    // ✨ 防抖机制：防止一次支付被记录多次
    private var lastRecordHash: String = ""
    private var lastRecordTime: Long = 0L

    private val engineManager = BillEngineManager()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val rootNode = rootInActiveWindow ?: return
        val packageName = event?.packageName?.toString() ?: return

        // 1. 获取全屏文字
        val screenText = extractAllText(rootNode).replace("\n", " ").trim()

        // ✨ 2. 交给管家处理（架构完美解耦！）
        val parsedBill = engineManager.processText(packageName, screenText)

        if (parsedBill != null) {
            // 防抖检查与保存逻辑...
            val currentHash = "${parsedBill.amount}_${parsedBill.type}_${parsedBill.source}"
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastRecordTime < 5000 && lastRecordHash == currentHash) {
                return
            }
            lastRecordHash = currentHash
            lastRecordTime = currentTime

            handleParsedBillWithAI(parsedBill)
        }
    }

    override fun onInterrupt() {
        Log.e("AutoLedgerVision", "视觉引擎被系统中断")
    }

    // ✨ 核心算法：递归提取屏幕上所有可见文本
    private fun extractAllText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        val builder = StringBuilder()
        if (node.text != null) builder.append(node.text.toString()).append(" ")
        if (node.contentDescription != null) builder.append(node.contentDescription.toString()).append(" ")

        for (i in 0 until node.childCount) {
            builder.append(extractAllText(node.getChild(i)))
        }
        return builder.toString()
    }

    // 屏幕文本解析逻辑（不再依赖通知，直接看屏幕找钱）
    private fun analyzeScreenText(packageName: String, screenText: String): ParsedBill? {
        if (packageName != "com.tencent.mm") return null

        // 用正则寻找金额，匹配 "¥ 20.00" 或 "￥20.00" 这种红色的巨大数字
        val amountRegex = "([¥￥])\\s?(\\d+\\.\\d+)".toRegex()
        val amountMatch = amountRegex.find(screenText)
        val amount = amountMatch?.groupValues?.get(2)?.toDoubleOrNull()

        if (amount == null || amount <= 0) return null

        // 场景 1：微信商户扫码支付成功界面
        if (screenText.contains("支付成功") && (screenText.contains("收款方") || screenText.contains("商户"))) {
            return ParsedBill(amount, 0, screenText, "WECHAT")
        }

        // 场景 2：微信好友转账成功界面 / 收款界面
        if (screenText.contains("微信转账") && (screenText.contains("已存入零钱") || screenText.contains("转账给朋友"))) {
            // 如果是你收到钱，就是收入1；你转给别人，就是支出0
            val type = if (screenText.contains("已存入零钱") || screenText.contains("收到")) 1 else 0
            return ParsedBill(amount, type, screenText, "WECHAT")
        }

        return null
    }

    // --- 下面的逻辑与之前一样，调用网络脱敏和 AI 处理 ---
    private fun maskSensitiveInfo(text: String): String {
        var masked = text
        masked = masked.replace(Regex("1[3-9]\\d{9}"), "1***手机号***")
        masked = masked.replace(Regex("给(.*?)(转账|发红包)"), "给***$2")
        masked = masked.replace(Regex("收到(.*?)(的转账|的红包)"), "收到***$2")
        // 如果文本太长（整个屏幕），截取包含“¥”前后的一段给 AI 即可，省 Token
        val index = masked.indexOf("¥")
        if (index != -1 && masked.length > 200) {
            val start = Math.max(0, index - 50)
            val end = Math.min(masked.length, index + 50)
            return masked.substring(start, end)
        }
        return masked.take(150) // 最多给 AI 看 150 个字
    }

    private fun handleParsedBillWithAI(bill: ParsedBill) {
        serviceScope.launch {
            var finalCategory = "自动记账"
            var finalNote = "微信支付"

            try {
                val safeNoteForAI = maskSensitiveInfo(bill.note)
                val categories = repository.getAllCategories().firstOrNull()?.map { it.name } ?: emptyList()
                val categoryStr = categories.joinToString(", ")

                val prompt = """
                    你是一个记账助手。这是一段提取自微信支付/转账界面的屏幕文字片段：
                    '$safeNoteForAI'
                    任务1：从列表中选一个分类：[$categoryStr, 其他]。
                    任务2：提取出正在交易的对象（如商户名、转账）。要求极简，不含人名等隐私。
                    返回纯JSON：{"category":"分类", "note":"对象"}
                """.trimIndent()

                val request = ChatRequest(
                    messages = listOf(Message("user", prompt)),
                    temperature = 0.1,
                    responseFormat = ResponseFormat(type = "json_object")
                )

                val response = apiService.getAiCompletion(apiKey, request)
                val jsonStr = response.choices?.firstOrNull()?.message?.content ?: "{}"
                val jsonObject = JSONObject(jsonStr)

                val aiCategory = jsonObject.optString("category", "自动记账")
                val aiNote = jsonObject.optString("note", "")

                if (categories.contains(aiCategory)) finalCategory = aiCategory
                if (aiNote.isNotBlank()) finalNote = aiNote

            } catch (e: Exception) {
                Log.e("AutoLedgerVision", "AI处理失败", e)
            }

            val entity = LedgerEntity(
                amount = bill.amount,
                type = bill.type,
                categoryName = finalCategory,
                categoryIcon = "ic_auto",
                timestamp = System.currentTimeMillis(),
                note = "[视觉] $finalNote",
                source = bill.source
            )
            repository.insertLedger(entity)
            sendSuccessNotification(bill, finalCategory, finalNote)
        }
    }

    private fun sendSuccessNotification(bill: ParsedBill, category: String, cleanNote: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "auto_ledger_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "视觉记账提醒", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val typeStr = if (bill.type == 0) "支出" else "收入"
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_ai_avatar)
            .setContentTitle("视觉引擎记账成功")
            .setContentText("¥${bill.amount} - $category ($cleanNote)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}