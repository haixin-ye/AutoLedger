package com.yhx.autoledger.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.network.LlmApiService
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(

    private val repository: LedgerRepository,
    private val apiService: LlmApiService,
    @ApplicationContext private val context: Context

) : ViewModel() {
    private val userVipLevel: Int = 1// 0:免费版, 1:普通会员, 2:高级会员(带上下文)
    private val context_length=6
    private val apiKey = "Bearer sk-b93a79d60e6445f89a214968e9273d71"

    // ✨ 构造一个支持 Compose Color 的 Gson 解析器
    private val gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, object : JsonSerializer<Color>,
            JsonDeserializer<Color> {
            override fun serialize(
                src: Color,
                typeOfSrc: Type,
                context: JsonSerializationContext
            ): JsonElement {
                return JsonPrimitive(src.toArgb()) // 颜色转数字
            }

            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Color {
                return Color(json.asInt) // 数字还原为颜色
            }
        })
        .create()

    private val prefs = context.getSharedPreferences("ai_chat_history", Context.MODE_PRIVATE)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ✨ 新增：维护当前数据库中所有可用的分类名称
    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())

    init {

        // ✨ 1. 启动时从本地读取聊天记录
        val historyJson = prefs.getString("chat_history", null)
        if (historyJson != null) {
            try {
                val type = object : TypeToken<List<ChatMessage>>() {}.type
                val history: List<ChatMessage> = gson.fromJson(historyJson, type)

                // ✨ 重点：通过 map 重新映射一遍，确保 GSON 注入的 null 被修正为 emptyList
                _messages.value = history.map {
                    it.copy(billPreviews = it.billPreviews ?: emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 解析失败（比如字段类型全变了），建议清空缓存
                prefs.edit { remove("chat_history") }
                loadDefaultWelcome()
            }
        } else {
            loadDefaultWelcome()
        }

        // ✨ 2. 核心魔法：只要 _messages 发生变化，就自动静默存入本地！
        viewModelScope.launch {
            _messages.collect { msgs ->
                prefs.edit { putString("chat_history", gson.toJson(msgs)) }
            }
        }
        // ✨ ViewModel 初始化时，立刻去监听数据库里的分类表
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                // 提取所有的名字，比如 ["餐饮", "交通", "购物", "娱乐", "居家", "工资", "理财"]
                _availableCategories.value = categories.map { it.name }
            }
        }
    }

    private fun loadDefaultWelcome() {
        _messages.value = listOf(
            ChatMessage(
                content = "您好，我是 AI 记账助手。您可以对我说：'今天打车花了 35，午饭吃了 20'。",
                isFromUser = false
            )
        )
    }

    // =========================================================================
    // 🧠 引擎 0：意图识别大模型 (Router)
    // =========================================================================
    private fun getIntentRouterPrompt(): String {
        return """
        你是一个意图识别引擎。请分析用户的输入，判断其真实意图。
        【分类标准】：
        0: 闲聊、问答、非记账类的日常对话。
        1: 记账指令（包含了消费、花钱、收入、买东西等明确或隐晦的财务行为）。
        
        【输出格式】：
        只返回纯JSON格式，绝对不要包含 ```json 等markdown标记：
        { "intent": 1 }
        """.trimIndent()
    }

    // =========================================================================
    // 🧠 引擎 1：专属记账大模型 (Accounting Expert)
    // =========================================================================
    private fun getAccountingPrompt(): String {
        val categoryStr = _availableCategories.value.joinToString(", ")
        return """
        你是一个极其聪明的专业记账提取引擎。今天是 ${getCurrentContextInfo()}。
        
        【分类限制与规则】（极其重要 ⚠️）：
        数据库仅支持以下分类：[$categoryStr, 其他]。
        提取的 "category" 必须严格从上述列表中选择。
        
        【推理规则】：
        1. 多账单拆分：如果用户一句话包含多笔花销（如“午饭20，打车30”），请必须拆分为多个对象。
        2. 时间推算：如果提到“昨天”、“周五”，请结合今天日期推算 yyyy-MM-dd。未提及默认今天。
        3. 收支类型：支出为0，收入为1。
        4. 客套话：请在 reply 字段顺着用户的话客套一句（例如："好的，已为您提取以下账单："）。
        5. 金额类型：人民币，如果是其他金额，请换算为人民币。

        【输出格式】：
        绝对不要输出 markdown 标记。直接输出以下纯 JSON：
        {
          "reply": "客套话",
          "bills": [
             {
               "category": "必须从限制列表中选择",
               "amount": "提取的金额（纯数字）",
               "date": "推算的日期（yyyy-MM-dd）",
               "icon": "匹配1个Emoji",
               "type": 0,
               "note": "完善后的备注"
             }
          ]
        }
        """.trimIndent()
    }

    // =========================================================================
    // 🧠 引擎 2：专属闲聊大模型 (Chat Expert)
    // =========================================================================
    private fun getChatPrompt(): String {
        return "你是一个聪明、幽默且富有亲和力的记账管家。请以朋友的口吻，简短地回复用户的闲聊内容。"
    }


    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _messages.value = _messages.value + ChatMessage(content = text, isFromUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 步骤 1：调用路由大模型，判断意图
                val intent = fetchIntentRecognition(text)

                // 步骤 2：拦截器与分发 (你可以在这里完美拓展 VIP 限制！)
                if (intent == 0) {
                    handleChatIntent(text)
                } else {
                    handleAccountingIntent(text)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + ChatMessage(
                    content = "网络或解析异常：${e.localizedMessage}",
                    isFromUser = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- 子方法：获取意图 ---
    private suspend fun fetchIntentRecognition(text: String): Int {
        val request = ChatRequest(
            messages = listOf(
                Message("system", getIntentRouterPrompt()),
                Message("user", text)
            )
        )
        val response = apiService.getAiCompletion(apiKey, request)
        val jsonStr = response.choices?.firstOrNull()?.message?.content?.replace("```json", "")
            ?.replace("```", "")?.trim() ?: "{}"
        return try {
            JSONObject(jsonStr).optInt("intent", 1)
        } catch (e: Exception) {
            1
        } // 默认走记账
    }


    // --- 子方法：处理闲聊/问答 ---
    private suspend fun handleChatIntent(text: String) {
        // 假设从本地或服务器获取的用户等级


        // 第一种情况：无会员，直接拦截拒绝
        if (userVipLevel == 0) {
            val rejectMsg = ChatMessage(
                content = "💡 闲聊与问答功能是会员专属哦，目前我只能帮您提取记账信息呢~",
                isFromUser = false
            )
            _messages.value = _messages.value + rejectMsg
            return
        }

        // 准备发给 API 的消息列表
        val apiMessages = mutableListOf<Message>()

        // 1. 永远需要先塞入系统人设
        apiMessages.add(
            Message(
                "system", """
            你是一个聪明、幽默且富有亲和力的万能助手。请以朋友的口吻，简短地回复用户的提问或闲聊。
            【极其重要：输出格式要求】
            必须且只能返回纯 JSON 格式（不要带 ```json 标记），格式如下：
            {
              "reply": "你的回复内容"
            }
        """.trimIndent()
            )
        )

        // 第三种情况：SVIP 高级会员，携带最近的上下文记忆
        if (userVipLevel == 2) {
            // 提取最近的 context_length 条聊天记录（控制 Token 成本，别全传）作为记忆
            val recentHistory = _messages.value.takeLast(context_length)
            recentHistory.forEach { msg ->
                // 注意：只传纯文本对话，不要把复杂的账单 JSON 也传过去扰乱它
                if (msg.content.isNotBlank()) {
                    val role = if (msg.isFromUser) "user" else "assistant"
                    apiMessages.add(Message(role, msg.content))
                }
            }
        }
        // 第二种情况：普通会员 (userVipLevel == 1)，不执行上面那段代码，直接跳到这里。

        // 2. 塞入用户当前说的话
        apiMessages.add(Message("user", text))

        // 3. 发送网络请求
        val request = ChatRequest(messages = apiMessages)
        val response = apiService.getAiCompletion(apiKey, request)
        val jsonStr = response.choices?.firstOrNull()?.message?.content?.replace("```json", "")
            ?.replace("```", "")?.trim() ?: "{}"

        val replyText = try {
            JSONObject(jsonStr).optString("reply", "哎呀，我走神了，能再说一遍吗？")
        } catch (e: Exception) {
            jsonStr // 兜底防线：万一它真的没按 JSON 回复，直接输出原话，防止应用崩溃
        }

        _messages.value = _messages.value + ChatMessage(content = replyText, isFromUser = false)
    }

    // --- 子方法：处理记账 (提取多账单数组) ---
    private suspend fun handleAccountingIntent(text: String) {
        val request = ChatRequest(
            messages = listOf(
                Message("system", getAccountingPrompt()),
                Message("user", text)
            )
        )
        val response = apiService.getAiCompletion(apiKey, request)
        val jsonStr = response.choices?.firstOrNull()?.message?.content?.replace("```json", "")
            ?.replace("```", "")?.trim() ?: "{}"

        val jsonObject = JSONObject(jsonStr)
        val replyText = jsonObject.optString("reply", "好的，已为您提取以下账单：")
        val billList = mutableListOf<BillPreview>()

        if (jsonObject.has("bills")) {
            val billsArray = jsonObject.optJSONArray("bills")
            if (billsArray != null) {
                for (i in 0 until billsArray.length()) {
                    val billObj = billsArray.getJSONObject(i)
                    billList.add(
                        BillPreview(
                            category = validateCategory(billObj.optString("category", "其他")),
                            amount = billObj.optString("amount", "0.0"),
                            date = billObj.optString("date", getCurrentDate()),
                            icon = billObj.optString("icon", "📝"),
                            color = Color(0xFF74EBD5),
                            note = billObj.optString("note", ""),
                            type = billObj.optInt("type", 0)
                        )
                    )
                }
            }
        }
        _messages.value += ChatMessage(
            content = replyText,
            isFromUser = false,
            billPreviews = billList
        )
    }


    // ✨ 安全防线：防止 AI 的“幻觉”破坏数据库结构
    private fun validateCategory(aiCategory: String): String {
        val currentCategories = _availableCategories.value
        return if (currentCategories.contains(aiCategory)) {
            aiCategory
        } else {
            "其他" // 强制兜底
        }
    }

    // ⚠️修复点2：补全了用户点击"确认归档"时调用的入库逻辑
    fun confirmAndSaveLedger(msgId: String, preview: BillPreview) {
        viewModelScope.launch {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = format.parse(preview.date)

                val finalTimestamp = if (parsedDate != null) {
                    val currentCalendar = java.util.Calendar.getInstance()
                    val hour = currentCalendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val minute = currentCalendar.get(java.util.Calendar.MINUTE)
                    val second = currentCalendar.get(java.util.Calendar.SECOND)

                    val targetCalendar = java.util.Calendar.getInstance()
                    targetCalendar.time = parsedDate
                    targetCalendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                    targetCalendar.set(java.util.Calendar.MINUTE, minute)
                    targetCalendar.set(java.util.Calendar.SECOND, second)
                    targetCalendar.timeInMillis
                } else {
                    System.currentTimeMillis()
                }

                val newLedger = LedgerEntity(
                    amount = preview.amount.toDoubleOrNull() ?: 0.0,
                    type = preview.type,
                    categoryName = preview.category,
                    categoryIcon = preview.icon,
                    timestamp = finalTimestamp,
                    note = preview.note.ifBlank { preview.category },
                    source = "AI"
                )

                repository.insertLedger(newLedger)

                // ✨ 修复 2：去多账单数组里找到这笔账单，标记为已保存
                _messages.value = _messages.value.map { currentMsg ->
                    if (currentMsg.id == msgId) {
                        val updatedBills = currentMsg.billPreviews.map { bill ->
                            if (bill.id == preview.id) bill.copy(isSaved = true) else bill
                        }
                        currentMsg.copy(billPreviews = updatedBills)
                    } else {
                        currentMsg
                    }
                }

                _messages.value = _messages.value + ChatMessage(
                    content = "✅ 账单已成功入库！您可以前往明细页查看。",
                    isFromUser = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + ChatMessage(
                    content = "❌ 账单归档失败：${e.localizedMessage}",
                    isFromUser = false
                )
            }
        }
    }


    // ✨ 修复 3：更新账单也必须精确到具体的 bill ID
    fun updateMessagePreview(msgId: String, updatedPreview: BillPreview) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == msgId) {
                val updatedBills = msg.billPreviews.map { bill ->
                    if (bill.id == updatedPreview.id) updatedPreview else bill
                }
                msg.copy(billPreviews = updatedBills)
            } else {
                msg
            }
        }
    }

    // 获取当前日期
    private fun getCurrentContextInfo(): String {
        return SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINESE).format(Date())
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }


}