package io.qzz.lstudy.novelforge.ui.novel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.ai.AiClient
import io.qzz.lstudy.novelforge.data.ai.AiResult
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
    data class Loading(val message: String) : GenerateState()
    data class Success(val chapterTitle: String) : GenerateState()
    data class Error(val message: String) : GenerateState()
}

/**
 * 小说详情页 ViewModel
 *
 * 功能：
 * 1. 观察小说信息和章节列表
 * 2. AI 生成下一章（调用 OpenAI 兼容协议）
 * 3. 章节手动编辑与保存
 */
@HiltViewModel
class NovelDetailViewModel @Inject constructor(
    private val novelRepository: NovelRepository,
    private val settingRepository: SettingRepository,
    private val aiClient: AiClient
) : ViewModel() {

    /** 当前小说 ID */
    private var novelId: Long = 0L

    /** 当前小说信息 */
    private val _novel = MutableStateFlow<Novel?>(null)
    val novel: StateFlow<Novel?> = _novel.asStateFlow()

    /** 当前小说的章节列表 */
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    /** 生成状态 */
    private val _generateState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val generateState: StateFlow<GenerateState> = _generateState.asStateFlow()

    /** 加载小说详情 */
    fun load(novelId: Long) {
        if (this.novelId == novelId) return
        this.novelId = novelId
        viewModelScope.launch {
            novelRepository.observeNovel(novelId).collect { n ->
                _novel.value = n
            }
        }
        viewModelScope.launch {
            novelRepository.observeChapters(novelId).collect { list ->
                _chapters.value = list
            }
        }
    }

    /**
     * AI 生成下一章
     *
     * 流程：
     * 1. 读取当前选中的 provider 与对应的 API Key、BaseUrl、模型
     * 2. 拼接 Prompt（小说标题 + 已有章节摘要 + 续写指令）
     * 3. 调用 AiClient.chat
     * 4. 将生成内容写入新章节
     */
    fun generateNextChapter() {
        if (novelId == 0L) return
        val current = _novel.value ?: return
        if (_generateState.value is GenerateState.Loading) return

        _generateState.value = GenerateState.Loading("正在准备调用 AI...")
        viewModelScope.launch {
            try {
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
                            _generateState.value = GenerateState.Error(
                                "当前供应商 [$providerKey] 暂不支持直接调用，请在设置中切换为 DeepSeek / 通义千问 / 智谱 GLM / Kimi / Groq / Mistral / OpenAI / 自定义供应商"
                            )
                            return@launch
                        }
                        cfg.baseUrl to cfg.defaultModel
                    }
                }

                if (apiKey.isBlank()) {
                    _generateState.value = GenerateState.Error(
                        "未配置 [$providerKey] 的 API Key，请到设置中填写"
                    )
                    return@launch
                }
                if (baseUrl.isBlank() || model.isBlank()) {
                    _generateState.value = GenerateState.Error(
                        "请求地址或模型名未配置，请到设置中填写"
                    )
                    return@launch
                }

                _generateState.value = GenerateState.Loading("正在让 AI 构思下一章...")

                // 拼接 Prompt
                val existing = _chapters.value
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

                when (val r = aiClient.chat(
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    model = model,
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    temperature = 0.85,
                    maxTokens = 4096
                )) {
                    is AiResult.Success -> {
                        val content = r.content.trim()
                        // 解析标题（第一行）
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
                        // 更新小说当前字数
                        val totalWords = _novel.value?.currentWords ?: 0
                        novelRepository.updateNovelCurrentWords(
                            novelId = novelId,
                            currentWords = totalWords + body.length
                        )
                        _generateState.value = GenerateState.Success(title)
                    }
                    is AiResult.Error -> {
                        _generateState.value = GenerateState.Error(r.message)
                    }
                }
            } catch (e: Exception) {
                _generateState.value = GenerateState.Error("生成异常：${e.message}")
            }
        }
    }

    /** 重置生成状态 */
    fun resetGenerateState() {
        _generateState.value = GenerateState.Idle
    }

    /** 更新章节内容 */
    fun updateChapterContent(chapter: Chapter, newContent: String) {
        viewModelScope.launch {
            novelRepository.updateChapterContent(chapter.id, newContent)
            // 重新计算小说总字数
            val all = novelRepository.listChapters(novelId)
            val total = all.sumOf { it.content.length }
            novelRepository.updateNovelCurrentWords(novelId, total)
        }
    }

    /** 删除章节 */
    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            novelRepository.deleteChapter(chapter)
            // 重新计算总字数
            val all = novelRepository.listChapters(novelId)
            val total = all.sumOf { it.content.length }
            novelRepository.updateNovelCurrentWords(novelId, total)
        }
    }
}
