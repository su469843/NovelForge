package io.qzz.lstudy.novelforge.ui.novel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.ai.AiClient
import io.qzz.lstudy.novelforge.data.ai.AiResult
import io.qzz.lstudy.novelforge.data.ai.ChatMessage
import io.qzz.lstudy.novelforge.data.ai.ProviderConfigs
import io.qzz.lstudy.novelforge.data.local.entity.Chapter
import io.qzz.lstudy.novelforge.data.local.entity.Novel
import io.qzz.lstudy.novelforge.data.repository.NovelRepository
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 章节生成状态 */
sealed class GenerateState {
    object Idle : GenerateState()
    data class Loading(val message: String, val current: Int = 0, val total: Int = 0) : GenerateState()
    data class Success(val chapterTitle: String, val tokensUsed: Int = 0) : GenerateState()
    data class Error(val message: String) : GenerateState()
}

/** 生成模式 */
enum class GenerateMode {
    /** 逐章生成：每次只生成一章 */
    CHAPTER_BY_CHAPTER,
    /** 一次性生成全部章节 */
    ALL_AT_ONCE
}

/** 对话消息（UI 层展示用） */
data class DialogMessage(
    val role: String, // "user" / "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 小说详情页 ViewModel
 *
 * 功能：
 * 1. 观察小说信息和章节列表
 * 2. AI 生成章节（支持逐章 / 一次性生成全部）
 * 3. Token 用量累计到 Novel
 * 4. AI 对话抽屉：针对当前章节内容多轮对话
 */
@HiltViewModel
class NovelDetailViewModel @Inject constructor(
    private val novelRepository: NovelRepository,
    private val settingRepository: SettingRepository,
    private val aiClient: AiClient
) : ViewModel() {

    private var novelId: Long = 0L

    private val _novel = MutableStateFlow<Novel?>(null)
    val novel: StateFlow<Novel?> = _novel.asStateFlow()

    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    private val _generateState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val generateState: StateFlow<GenerateState> = _generateState.asStateFlow()

    /** AI 对话消息历史 */
    private val _dialogMessages = MutableStateFlow<List<DialogMessage>>(emptyList())
    val dialogMessages: StateFlow<List<DialogMessage>> = _dialogMessages.asStateFlow()

    /** 对话加载状态 */
    private val _dialogLoading = MutableStateFlow(false)
    val dialogLoading: StateFlow<Boolean> = _dialogLoading.asStateFlow()

    /** 加载小说详情 */
    fun load(novelId: Long) {
        if (this.novelId == novelId) return
        this.novelId = novelId
        viewModelScope.launch {
            novelRepository.observeNovel(novelId).collect { n -> _novel.value = n }
        }
        viewModelScope.launch {
            novelRepository.observeChapters(novelId).collect { list -> _chapters.value = list }
        }
    }

    /**
     * AI 生成下一章
     *
     * @param mode  生成模式
     * @param targetChapterCount  仅在 ALL_AT_ONCE 模式下使用，目标章节数
     */
    fun generateNextChapter(
        mode: GenerateMode = GenerateMode.CHAPTER_BY_CHAPTER,
        targetChapterCount: Int = 0
    ) {
        if (novelId == 0L) return
        val current = _novel.value ?: return
        if (_generateState.value is GenerateState.Loading) return

        when (mode) {
            GenerateMode.CHAPTER_BY_CHAPTER -> generateOne(current)
            GenerateMode.ALL_AT_ONCE -> generateAll(current, targetChapterCount)
        }
    }

    /** 生成单个章节 */
    private fun generateOne(current: Novel) {
        _generateState.value = GenerateState.Loading("正在准备调用 AI...", 0, 1)
        viewModelScope.launch {
            val result = callAiGenerate(current, _chapters.value)
            when (result) {
                is AiResult.Success -> {
                    saveChapter(current, result.content, result.totalTokens)
                    _generateState.value = GenerateState.Success(
                        parseTitle(result.content),
                        result.totalTokens
                    )
                }
                is AiResult.Error -> {
                    _generateState.value = GenerateState.Error(result.message)
                }
            }
        }
    }

    /** 一次性生成全部章节 */
    private fun generateAll(current: Novel, targetCount: Int) {
        val total = if (targetCount > 0) targetCount else current.targetChapters
        if (total <= 0) {
            _generateState.value = GenerateState.Error("请先在小说设置中指定目标章节数")
            return
        }
        viewModelScope.launch {
            val existing = _chapters.value
            val startOrder = existing.size + 1
            var totalTokensUsed = 0
            for (i in startOrder..total) {
                _generateState.value = GenerateState.Loading(
                    "正在生成第 $i / $total 章...",
                    current = i - startOrder + 1,
                    total = total - startOrder + 1
                )
                when (val r = callAiGenerate(current, _chapters.value)) {
                    is AiResult.Success -> {
                        saveChapter(current, r.content, r.totalTokens)
                        totalTokensUsed += r.totalTokens
                    }
                    is AiResult.Error -> {
                        _generateState.value = GenerateState.Error("第 $i 章生成失败：${r.message}")
                        return@launch
                    }
                }
            }
            _generateState.value = GenerateState.Success("全部章节", totalTokensUsed)
        }
    }

    /** 实际调用 AI 生成一章 */
    private suspend fun callAiGenerate(current: Novel, existing: List<Chapter>): AiResult {
        val providerKey = settingRepository.observeActiveProvider().first()
        val apiKeys = settingRepository.getApiKeys()
        val apiKey = apiKeys[providerKey].orEmpty()

        // 解析 baseUrl 和 model
        val (baseUrl, model) = when (providerKey) {
            "custom" -> {
                val url = settingRepository.observeCustomBaseUrl().first()
                val m = settingRepository.observeCustomModel().first()
                url to m
            }
            else -> {
                val cfg = ProviderConfigs.byKey(providerKey)
                if (cfg == null || !cfg.openAiCompatible) {
                    return AiResult.Error(
                        "当前供应商 [$providerKey] 暂不支持直接调用，请在设置中切换为 DeepSeek / 通义千问 / 智谱 GLM / Kimi / Groq / Mistral / OpenAI / 自定义供应商"
                    )
                }
                cfg.baseUrl to (current.model.ifBlank { cfg.models.firstOrNull() ?: "" })
            }
        }

        if (apiKey.isBlank()) return AiResult.Error("未配置 [$providerKey] 的 API Key")
        if (baseUrl.isBlank() || model.isBlank()) return AiResult.Error("请求地址或模型名未配置")

        val chapterOrder = existing.size + 1
        val summary = if (existing.isEmpty()) {
            "（暂无前文，请从开篇写起）"
        } else {
            existing.takeLast(3).joinToString("\n\n") { ch ->
                "第${ch.order}章 ${ch.title}：\n${ch.content.take(500)}..."
            }
        }

        val systemPrompt = """
            你是一位资深中文小说作者，擅长长篇连载创作。
            请根据小说标题和已有内容，创作下一章。
            要求：
            1. 章节标题简练有吸引力
            2. 内容连贯、情节推进自然
            3. 字数约 2000 字
            4. 输出格式为「第X章 标题」开头，空一行后接正文，不要额外说明
        """.trimIndent()

        val userPrompt = """
            小说标题：${current.title}
            目标总字数：${current.targetWords}
            当前已写章节数：${existing.size}

            最近章节内容摘要：
            $summary

            请创作第 $chapterOrder 章。
        """.trimIndent()

        return aiClient.chat(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            temperature = 0.85,
            maxTokens = 4096
        )
    }

    /** 保存生成的章节到数据库，并累加 token */
    private suspend fun saveChapter(current: Novel, content: String, tokens: Int) {
        val existing = _chapters.value
        val chapterOrder = existing.size + 1
        val firstLine = content.lineSequence().firstOrNull()?.trim().orEmpty()
        val title = if (firstLine.isNotBlank()) firstLine else "第${chapterOrder}章"
        val body = if (firstLine.isNotBlank()) {
            content.substringAfter(firstLine).trim().ifBlank { content }
        } else content

        novelRepository.addChapter(
            novelId = novelId,
            title = title,
            order = chapterOrder,
            content = body
        )
        // 更新字数与 token
        val totalWords = _novel.value?.currentWords ?: 0
        novelRepository.updateNovelCurrentWords(novelId, totalWords + body.length)
        if (tokens > 0) novelRepository.addNovelTokens(novelId, tokens)
    }

    /** 从 AI 返回内容解析章节标题 */
    private fun parseTitle(content: String): String {
        val firstLine = content.lineSequence().firstOrNull()?.trim().orEmpty()
        return if (firstLine.isNotBlank()) firstLine else "新章节"
    }

    /** 重置生成状态 */
    fun resetGenerateState() {
        _generateState.value = GenerateState.Idle
    }

    // ===================== AI 对话抽屉 =====================

    /**
     * 发送对话消息
     *
     * @param chapterId  当前正在查看的章节 ID
     * @param userMessage  用户输入的提示词，如"分析这篇文章的人物塑造"
     */
    fun sendDialogMessage(chapter: Chapter?, userMessage: String) {
        if (userMessage.isBlank()) return
        if (_dialogLoading.value) return

        // 添加用户消息
        _dialogMessages.value = _dialogMessages.value + DialogMessage("user", userMessage)
        _dialogLoading.value = true

        viewModelScope.launch {
            try {
                val providerKey = settingRepository.observeActiveProvider().first()
                val apiKeys = settingRepository.getApiKeys()
                val apiKey = apiKeys[providerKey].orEmpty()

                val (baseUrl, model) = when (providerKey) {
                    "custom" -> {
                        val url = settingRepository.observeCustomBaseUrl().first()
                        val m = settingRepository.observeCustomModel().first()
                        url to m
                    }
                    else -> {
                        val cfg = ProviderConfigs.byKey(providerKey) ?: return@launch
                        cfg.baseUrl to (_novel.value?.model?.ifBlank { cfg.models.firstOrNull() ?: "" }
                            ?: cfg.models.firstOrNull().orEmpty())
                    }
                }

                if (apiKey.isBlank() || baseUrl.isBlank() || model.isBlank()) {
                    _dialogMessages.value = _dialogMessages.value + DialogMessage(
                        "assistant",
                        "请先到设置中配置完整的 API Key、BaseUrl 与模型名"
                    )
                    _dialogLoading.value = false
                    return@launch
                }

                // 构造消息列表：系统提示 + 当前章节内容 + 历史对话
                val systemPrompt = """
                    你是一位中文小说创作助手。用户正在阅读/编辑一章小说，可能会向你提问或请求分析。
                    请基于提供的章节内容回答用户问题，必要时给出修改建议。
                    回答要简洁、聚焦，避免无关内容。
                """.trimIndent()

                val messages = mutableListOf<ChatMessage>()
                messages.add(ChatMessage("system", systemPrompt))
                if (chapter != null) {
                    messages.add(ChatMessage(
                        "user",
                        "当前章节标题：${chapter.title}\n当前章节正文：\n${chapter.content}"
                    ))
                    messages.add(ChatMessage("assistant", "好的，我已阅读这一章，请问您想了解什么？"))
                }
                // 加入历史对话
                _dialogMessages.value.forEach { m ->
                    messages.add(ChatMessage(m.role, m.content))
                }

                when (val r = aiClient.chatWithMessages(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    model = model,
                    messages = messages,
                    temperature = 0.7,
                    maxTokens = 2048
                )) {
                    is AiResult.Success -> {
                        _dialogMessages.value = _dialogMessages.value + DialogMessage("assistant", r.content)
                        if (r.totalTokens > 0) {
                            novelRepository.addNovelTokens(novelId, r.totalTokens)
                        }
                    }
                    is AiResult.Error -> {
                        _dialogMessages.value = _dialogMessages.value + DialogMessage(
                            "assistant",
                            "请求失败：${r.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _dialogMessages.value = _dialogMessages.value + DialogMessage(
                    "assistant",
                    "发生异常：${e.message}"
                )
            } finally {
                _dialogLoading.value = false
            }
        }
    }

    /** 清空对话历史 */
    fun clearDialog() {
        _dialogMessages.value = emptyList()
    }

    /** 更新章节内容 */
    fun updateChapterContent(chapter: Chapter, newContent: String) {
        viewModelScope.launch {
            novelRepository.updateChapterContent(chapter.id, newContent)
            val all = novelRepository.listChapters(novelId)
            val total = all.sumOf { it.content.length }
            novelRepository.updateNovelCurrentWords(novelId, total)
        }
    }

    /** 删除章节 */
    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            novelRepository.deleteChapter(chapter)
            val all = novelRepository.listChapters(novelId)
            val total = all.sumOf { it.content.length }
            novelRepository.updateNovelCurrentWords(novelId, total)
        }
    }
}
