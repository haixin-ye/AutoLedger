package com.yhx.autoledger.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yhx.autoledger.data.entity.LedgerEntity
import com.yhx.autoledger.data.network.LlmApiService
import com.yhx.autoledger.data.repository.LedgerRepository
import com.yhx.autoledger.models.BillPreview
import com.yhx.autoledger.models.ChatMessage
import com.yhx.autoledger.models.ChatRequest
import com.yhx.autoledger.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val repository: LedgerRepository,
    private val apiService: LlmApiService
) : ViewModel() {

    private val apiKey = "Bearer sk-b93a79d60e6445f89a214968e9273d71"

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "您好，我是 AI 记账助手。您可以对我说：'今天早上打车花了 35 元'。",
                isFromUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // ✨ 新增：维护当前数据库中所有可用的分类名称
    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())

    init {
        // ✨ ViewModel 初始化时，立刻去监听数据库里的分类表
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                // 提取所有的名字，比如 ["餐饮", "交通", "购物", "娱乐", "居家", "工资", "理财"]
                _availableCategories.value = categories.map { it.name }
            }
        }
    }

    // ✨ 核心重构：将静态属性改为动态方法，每次请求时动态生成
    private fun getDynamicSystemPrompt(): String {
        // 将列表拼接成字符串，例如："餐饮, 交通, 购物, 娱乐, 居家, 工资, 理财"
        val categoryStr = _availableCategories.value.joinToString(", ")

        return """
        你是一个极其聪明的专业记账助手。
        【当前时间上下文】：今天是 ${getCurrentContextInfo()}。
        
        【分类限制与规则】（极其重要 ⚠️）：
        当前系统数据库仅支持以下分类：[$categoryStr, 其他]。
        你提取的 "category" 字段 **必须且只能** 是上述列表中的某一个词。绝不允许创造新词汇！
        - 语义就近原则：如果用户输入“买药”，请归入“购物”或“其他”；输入“瑞幸”，请归入“餐饮”；输入“打车”，请归入“交通”。
        - 兜底原则：如果实在无法归入现有分类，请统一填入 "其他"。
        
        【推理规则】：
        1. 时间推算：如果用户说“昨天”、“周五”等，请务必结合当前时间推算确切的 yyyy-MM-dd 日期。若未提及，默认今天。
        2. 收支类型：默认判断为支出(type: 0)。如果明确表达收入（如“发工资”、“收红包”），type 填 1。
        3. 图标匹配：请根据你选定的分类，挑选 1 个最贴切的 Emoji 作为 icon。
        
        【输出格式要求】：
        绝对不要输出任何 markdown 标记。直接输出以下纯 JSON 对象：
        {
          "category": "必须从上述提供的列表中选择",
          "amount": "提取的金额（纯数字）",
          "date": "推算出的日期（yyyy-MM-dd）",
          "icon": "匹配的1个Emoji",
          "type": 0,
          "note": "完善后的简短备注（如将'油泼'完善为'油泼面'）"
        }
        """.trimIndent()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        _messages.value = _messages.value + ChatMessage(content = text, isFromUser = true)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ChatRequest(
                    messages = listOf(
                        // ✨ 每次发送前，调用方法获取注入了最新分类的 Prompt
                        Message(role = "system", content = getDynamicSystemPrompt()),
                        Message(role = "user", content = text)
                    )
                )

                val response = apiService.getAiCompletion(apiKey, request)
                val aiReplyContent = response.choices?.firstOrNull()?.message?.content ?: "{}"
                val cleanJson = aiReplyContent.replace("```json", "").replace("```", "").trim()

                val jsonObject = JSONObject(cleanJson)
                val preview = BillPreview(
                    // 如果大模型不听话返回了乱七八糟的分类，这里再加一层安全校验兜底
                    category = validateCategory(jsonObject.optString("category", "其他")),
                    amount = jsonObject.optString("amount", "0.0"),
                    date = jsonObject.optString("date", getCurrentDate()),
                    icon = jsonObject.optString("icon", "📝"),
                    color = Color(0xFF74EBD5),
                    note = jsonObject.optString("note", ""), // ✨ 解析备注
                    type = jsonObject.optInt("type", 0)      // ✨ 解析收支类型
                )

                _messages.value =
                    _messages.value + ChatMessage(
                        content = "识别成功！已生成账单详情：",
                        isFromUser = false,
                        billPreview = preview
                    )

            } catch (e: Exception) {
                e.printStackTrace()
                _messages.value =
                    _messages.value + ChatMessage(
                        content = "解析异常：${e.localizedMessage}",
                        isFromUser = false
                    )
            } finally {
                _isLoading.value = false
            }
        }
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
    fun confirmAndSaveLedger(msg: ChatMessage) {
        val preview = msg.billPreview ?: return
        viewModelScope.launch {
            try {
                // ✨ 时间魔法：保留 AI 算出的年月日，拼接当前的时分秒
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

                // 构建要存入 Room 数据库的实体
                val newLedger = LedgerEntity(
                    amount = preview.amount.toDoubleOrNull() ?: 0.0,
                    type = preview.type, // ✨ 使用 AI 动态判断的收支类型
                    categoryName = preview.category,
                    categoryIcon = preview.icon,
                    timestamp = finalTimestamp, // ✨ 使用注入了时分秒的时间
                    note = preview.note.ifBlank { preview.category }, // ✨ 如果备注为空，用分类名兜底
                    source = "AI"
                )

                repository.insertLedger(newLedger)

                // ✨ 核心机制：数据库保存成功后，遍历当前消息列表，找到这条消息，给它上锁！
                _messages.value = _messages.value.map { currentMsg ->
                    if (currentMsg.id == msg.id) {
                        currentMsg.copy(isSaved = true) // 状态彻底锁死
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


    // ✨ 新增：更新指定消息的账单预览内容
    fun updateMessagePreview(msgId: String, updatedPreview: BillPreview) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == msgId) {
                // 找到对应的消息，替换它的 billPreview
                msg.copy(billPreview = updatedPreview)
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