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
import com.yhx.autoledger.data.repository.UserPreferencesRepository // ✨ 必须引入
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first // ✨ 必须引入
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val apiService: LlmApiService,
    private val userPrefs: UserPreferencesRepository, // ✨ 注入 DataStore 仓库获取当前账本
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val userVipLevel: Int = 1 // 0:免费版, 1:普通会员, 2:高级会员(带上下文)
    private val context_length = 6
    private val apiKey = "Bearer sk-b93a79d60e6445f89a214968e9273d71"


    // 1. 在 AIViewModel 类的变量声明区（init 上方），新增对指令流的监听：
    private val _customInstructions = MutableStateFlow("")

    // 构造一个支持 Compose Color 的 Gson 解析器
    private val gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, object : JsonSerializer<Color>,
            JsonDeserializer<Color> {
            override fun serialize(src: Color, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                return JsonPrimitive(src.toArgb())
            }
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Color {
                return Color(json.asInt)
            }
        })
        .create()

    private val prefs = context.getSharedPreferences("ai_chat_history", Context.MODE_PRIVATE)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            // 1. 先同步/挂起读取历史记录
            val historyJson = prefs.getString("chat_history", null)
            val initialList = if (historyJson != null) {
                try {
                    val type = object : TypeToken<List<ChatMessage>>() {}.type
                    val history: List<ChatMessage> = gson.fromJson(historyJson, type)
                    // 修正 GSON 的 null 问题
                    history.map { it.copy(billPreviews = it.billPreviews ?: emptyList()) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    prefs.edit { remove("chat_history") }
                    getDefaultWelcome() // 见下方的辅助方法
                }
            } else {
                getDefaultWelcome()
            }

            // 2. 读取完成后，再赋值给 StateFlow
            _messages.value = initialList

            // 3. ✨ 核心修复：延迟监听！并且使用 drop(1) 跳过首次的 value 赋值触发
            _messages.drop(1).collect { msgs ->
                // ✨ 性能优化：序列化长列表是非常耗时的，必须在 IO 线程执行 (当前协程已经是 Dispatchers.IO)
                val json = gson.toJson(msgs)
                prefs.edit { putString("chat_history", json) }
            }
        }

        viewModelScope.launch {
            _messages.collect { msgs ->
                prefs.edit { putString("chat_history", gson.toJson(msgs)) }
            }
        }

        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _availableCategories.value = categories.map { it.name }
            }
        }
    }

    private fun getDefaultWelcome(): List<ChatMessage> {
        return listOf(
            ChatMessage(
                content = "您好，我是 AI 记账助手。您可以对我说：'今天打车花了 35，午饭吃了 20'。",
                isFromUser = false
            )
        )
    }



    private fun loadDefaultWelcome() {
        _messages.value = listOf(
            ChatMessage(
                content = "您好，我是 AI 记账助手。您可以对我说：'今天打车花了 35，午饭吃了 20'。",
                isFromUser = false
            )
        )
    }

    private fun getIntentRouterPrompt(): String {
        return """
        你是一个专注的意图识别引擎，运行于一款专业的记账软件中。请分析用户的输入，精准判断其真实意图。
        
        【分类标准】：
        0: 闲聊、问答或指令（如：打招呼、问天气、寻求建议、询问历史账单等非“新增记账”行为）。
        1: 记账指令（任何试图新增收支记录的行为，包含极其简短或隐晦的输入）。
        
        【核心判定规则】（极其重要 ⚠️）：
        用户在记账时习惯使用极简缩写。只要输入符合“名词/品牌名/场景 + 数字”的结构，或者暗示了资金的变动，都必须绝对识别为 1（记账指令）。
        
        【示例参考】：
        - 输入: "kfc30" -> 意图: 1 (解析为肯德基消费30元)
        - 输入: "兰香子 30" -> 意图: 1 (解析为餐饮消费30元)
        - 输入: "打车15.5" -> 意图: 1
        - 输入: "发工资 8000" -> 意图: 1
        - 输入: "昨天早饭 8" -> 意图: 1
        - 输入: "我这个月花了多少钱？" -> 意图: 0 (这是在查询，不是新增记账)
        - 输入: "帮我写一首诗" -> 意图: 0
        
        【输出格式】：
        只返回纯 JSON 格式，绝对不要包含 ```json 等 markdown 标记，不要返回多余的解释：
        { "intent": 1 }
        """.trimIndent()
    }

    // 3. ✨ 核心魔法：修改 getAccountingPrompt() 方法，注入用户的专属指令！
    private fun getAccountingPrompt(): String {
        val categoryStr = _availableCategories.value.joinToString(", ")
        val userRules = _customInstructions.value

        return """
        你是一个极其聪明的专业记账提取引擎。今天是 ${getCurrentContextInfo()}。
        
        【分类限制与规则】（极其重要 ⚠️）：
        数据库仅支持以下分类：[$categoryStr, 其他]。
        提取的 "category" 必须严格从上述列表中选择。
        
        ${if (userRules.isNotBlank()) "【用户专属自定义规则】（必须严格遵守以下约定）：\n$userRules\n" else ""}
        
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

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _messages.value = _messages.value + ChatMessage(content = text, isFromUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val intent = fetchIntentRecognition(text)
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
        }
    }

    private suspend fun handleChatIntent(text: String) {
        if (userVipLevel == 0) {
            val rejectMsg = ChatMessage(
                content = "💡 闲聊与问答功能是会员专属哦，目前我只能帮您提取记账信息呢~",
                isFromUser = false
            )
            _messages.value = _messages.value + rejectMsg
            return
        }

        val apiMessages = mutableListOf<Message>()
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

        if (userVipLevel == 2) {
            val recentHistory = _messages.value.takeLast(context_length)
            recentHistory.forEach { msg ->
                if (msg.content.isNotBlank()) {
                    val role = if (msg.isFromUser) "user" else "assistant"
                    apiMessages.add(Message(role, msg.content))
                }
            }
        }

        apiMessages.add(Message("user", text))

        val request = ChatRequest(messages = apiMessages)
        val response = apiService.getAiCompletion(apiKey, request)
        val jsonStr = response.choices?.firstOrNull()?.message?.content?.replace("```json", "")
            ?.replace("```", "")?.trim() ?: "{}"

        val replyText = try {
            JSONObject(jsonStr).optString("reply", "哎呀，我走神了，能再说一遍吗？")
        } catch (e: Exception) {
            jsonStr
        }

        _messages.value = _messages.value + ChatMessage(content = replyText, isFromUser = false)
    }

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

    private fun validateCategory(aiCategory: String): String {
        val currentCategories = _availableCategories.value
        return if (currentCategories.contains(aiCategory)) {
            aiCategory
        } else {
            "其他"
        }
    }

    // ✨ 核心修复：确认归档时，必须带上当前的 BookId
    fun confirmAndSaveLedger(msgId: String, preview: BillPreview) {
        viewModelScope.launch {
            try {
                // ✨ 1. 挂起获取当前真实的 bookId
                val activeBookId = userPrefs.currentBookId.first()

                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = format.parse(preview.date)

                val finalTimestamp = if (parsedDate != null) {
                    val currentCalendar = Calendar.getInstance()
                    val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
                    val minute = currentCalendar.get(Calendar.MINUTE)
                    val second = currentCalendar.get(Calendar.SECOND)

                    val targetCalendar = Calendar.getInstance()
                    targetCalendar.time = parsedDate
                    targetCalendar.set(Calendar.HOUR_OF_DAY, hour)
                    targetCalendar.set(Calendar.MINUTE, minute)
                    targetCalendar.set(Calendar.SECOND, second)
                    targetCalendar.timeInMillis
                } else {
                    System.currentTimeMillis()
                }

                // ✨ 2. 组装实体类时，必须注入 bookId
                val newLedger = LedgerEntity(
                    bookId = activeBookId, // ✨ 让 AI 产生的账单归属于当前账本
                    amount = preview.amount.toDoubleOrNull() ?: 0.0,
                    type = preview.type,
                    categoryName = preview.category,
                    categoryIcon = preview.icon,
                    timestamp = finalTimestamp,
                    note = preview.note.ifBlank { preview.category },
                    source = "AI"
                )

                repository.insertLedger(newLedger)

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

    private fun getCurrentContextInfo(): String {
        return SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINESE).format(Date())
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}