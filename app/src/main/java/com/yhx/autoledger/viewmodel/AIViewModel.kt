package com.yhx.autoledger.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
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
import com.yhx.autoledger.data.repository.AIPersonaRepository
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.data.repository.UserPreferencesRepository
import com.yhx.autoledger.model.AIResponseData
import com.yhx.autoledger.model.PresentationStrategyType
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.Message
import com.yhx.autoledger.utils.AIPromptManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val userPrefs: UserPreferencesRepository,
    private val aiPromptManager: AIPromptManager,
    private val aiPersonaRepository: AIPersonaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // ✨ 恢复为你原本代码中的变量名，彻底解决飘红！
    private val userVipLevel: Int = 1 // 0:免费版, 1:普通会员, 2:高级会员(带上下文)
    private val context_length = 6
    private val apiKey = "Bearer sk-b93a79d60e6445f89a214968e9273d71"

    private val _currentPersonaId = MutableStateFlow("professional_butler")
    private val _customInstructions = MutableStateFlow("")

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
        // 1. 恢复历史记录
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val historyJson = prefs.getString("chat_history", null)
            val initialList = if (historyJson != null) {
                try {
                    val type = object : TypeToken<List<ChatMessage>>() {}.type
                    // 👇 改成这样：直接返回，不需要再多声明一个变量
                    gson.fromJson<List<ChatMessage>>(historyJson, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                    prefs.edit { remove("chat_history") }
                    getDefaultWelcome()
                }
            } else {
                getDefaultWelcome()
            }

            // 👇 这里也不需要 as List<ChatMessage> 强转了，因为 initialList 现在肯定是 List
            _messages.value = initialList

            _messages.drop(1).collect { msgs ->
                prefs.edit { putString("chat_history", gson.toJson(msgs)) }
            }
        }

        // 2. 监听数据库分类
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _availableCategories.value = categories.map { it.name }
            }
        }

        // 3. 监听 DataStore 中的人设设置和专属记忆
        viewModelScope.launch {
            userPrefs.aiPersonaId.collect { id ->
                _currentPersonaId.value = id
            }
        }
        viewModelScope.launch {
            userPrefs.aiCustomInstructions.collect { instructions ->
                _customInstructions.value = instructions
            }
        }
    }

    private fun getDefaultWelcome(): List<ChatMessage> {
        return listOf(ChatMessage(content = "您好，我是您的 AI 财务助手。您可以直接告诉我您的开支，如：'刚才买奶茶花了 15 元'。", isFromUser = false))
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(content = text, isFromUser = true)
        _messages.value = _messages.value + userMsg

        viewModelScope.launch {
            _isLoading.value = true
            try {
                processUserMessage(text)
            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value = _messages.value + ChatMessage(content = "抱歉，由于网络波动，我暂时无法处理您的请求。", isFromUser = false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * ✨ 核心引擎 1：处理消息、管理上下文并调用 LLM
     */
    private suspend fun processUserMessage(text: String) {
        val currentPersona = aiPersonaRepository.getPersonaById(_currentPersonaId.value)
        val customRules = _customInstructions.value
        val categoryStr = _availableCategories.value.joinToString(", ")
        val isVip = userVipLevel > 0

        val finalPrompt = aiPromptManager.buildUnifiedPrompt(
            persona = currentPersona,
            isVip = isVip,
            categories = categoryStr,
            customRules = customRules,
            todayDate = getCurrentContextInfo()
        )

        val apiMessages = mutableListOf<Message>()
        apiMessages.add(Message("system", finalPrompt))

        // ✨ 尊享版携带上下文逻辑，并使用你原来的变量名 context_length
        if (userVipLevel >= 2) {
            val currentMsgs = _messages.value
            val history = currentMsgs
                .filter { it.billPreviews.isNullOrEmpty() }
                .takeLast(context_length)

            history.forEach { msg ->
                apiMessages.add(Message(if (msg.isFromUser) "user" else "assistant", msg.content))
            }
        }

        apiMessages.add(Message("user", text))

        val request = ChatRequest(messages = apiMessages)
        val response = apiService.getAiCompletion(apiKey, request)
        val content = response.choices?.firstOrNull()?.message?.content ?: "{}"

        val cleanJson = content.replace("```json", "").replace("```", "").trim()

        val aiData = try {
            Gson().fromJson(cleanJson, AIResponseData::class.java)
        } catch (e: Exception) {
            AIResponseData(intent = "CHAT", reply_text = cleanJson)
        }

        applyPresentationStrategy(currentPersona.strategyType, aiData)
    }

    /**
     * ✨ 核心引擎 2：UI 策略分发
     */
    private fun applyPresentationStrategy(strategy: PresentationStrategyType, data: AIResponseData) {
        val billPreviews = data.bills.map { bill ->
            BillPreview(
                category = validateCategory(bill.category),
                amount = bill.amount.toString(),
                date = bill.date.ifBlank { getCurrentDate() },
                icon = bill.icon.ifBlank { "📝" },
                note = bill.note,
                type = bill.type,
                color = Color(0xFF74EBD5)
            )
        }

        when (strategy) {
            PresentationStrategyType.BUTLER -> {
                _messages.value += ChatMessage(content = data.reply_text, isFromUser = false, billPreviews = billPreviews)
                if (data.intent == "ACCOUNTING") {
                    _messages.value += ChatMessage(content = "💼 系统提示：以上账目已准备就绪，请核对后存入账本。", isFromUser = false)
                }
            }
            PresentationStrategyType.TSUNDERE -> {
                _messages.value += ChatMessage(content = data.reply_text, isFromUser = false, billPreviews = billPreviews)
            }
            PresentationStrategyType.FRIEND -> {
                _messages.value += ChatMessage(content = data.reply_text, isFromUser = false, billPreviews = billPreviews)
                if (data.intent == "ACCOUNTING") {
                    _messages.value += ChatMessage(content = "帮你在本子上记好啦，记得经常来看看哦！✨", isFromUser = false)
                }
            }
        }
    }

    private fun validateCategory(aiCategory: String): String {
        return if (_availableCategories.value.contains(aiCategory)) aiCategory else "其他"
    }

    fun confirmAndSaveLedger(msgId: String, preview: BillPreview) {
        viewModelScope.launch {
            try {
                val activeBookId = userPrefs.currentBookId.first()
                val newLedger = LedgerEntity(
                    bookId = activeBookId,
                    amount = preview.amount.toDoubleOrNull() ?: 0.0,
                    type = preview.type,
                    categoryName = preview.category,
                    categoryIcon = preview.icon,
                    timestamp = parseDateToTimestamp(preview.date),
                    note = preview.note.ifBlank { preview.category },
                    source = "AI"
                )

                repository.insertLedger(newLedger)

                _messages.value = _messages.value.map { msg ->
                    if (msg.id == msgId) {
                        msg.copy(billPreviews = msg.billPreviews.map {
                            if (it.id == preview.id) it.copy(isSaved = true) else it
                        })
                    } else msg
                }

                _messages.value += ChatMessage(content = "✅ 归档成功", isFromUser = false)
            } catch (e: Exception) {
                _messages.value += ChatMessage(content = "❌ 存储失败，请稍后重试", isFromUser = false)
            }
        }
    }

    private fun parseDateToTimestamp(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getCurrentContextInfo(): String = SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINESE).format(Date())
    private fun getCurrentDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

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
}