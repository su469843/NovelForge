package io.qzz.lstudy.novelforge.data.ai

/**
 * AI 供应商配置
 *
 * 包含 provider 标识、显示名、BaseUrl、默认模型名等信息。
 * 大多数国内供应商都提供 OpenAI 兼容协议，可统一通过 [AiClient] 调用。
 */
data class ProviderConfig(
    val key: String,
    val displayName: String,
    /** OpenAI 兼容协议的 BaseUrl，末尾不含斜杠 */
    val baseUrl: String,
    /** 默认模型名 */
    val defaultModel: String,
    /** 是否支持 OpenAI 兼容协议 */
    val openAiCompatible: Boolean = true,
    /** 简短描述（用于 UI 提示） */
    val description: String = ""
)

/**
 * 内置供应商配置表
 *
 * 注意：
 * - DeepSeek、通义千问、智谱 GLM、Kimi、Groq、Mistral、OpenAI 均原生兼容 OpenAI 协议
 * - 豆包、MiniMax、文心一言、Gemini、Claude 协议不同，标记为 openAiCompatible=false
 *   UI 会提示"暂不支持通过本应用直接调用"
 */
object ProviderConfigs {

    val ALL: List<ProviderConfig> = listOf(
        ProviderConfig(
            key = "deepseek",
            displayName = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            defaultModel = "deepseek-chat",
            description = "国内可用，性价比高"
        ),
        ProviderConfig(
            key = "qwen",
            displayName = "通义千问 (阿里)",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            defaultModel = "qwen-plus",
            description = "兼容 OpenAI 协议"
        ),
        ProviderConfig(
            key = "ernie",
            displayName = "文心一言 (百度)",
            baseUrl = "https://qianfan.baidubce.com/v2",
            defaultModel = "ernie-bot-turbo",
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "glm",
            displayName = "智谱 GLM",
            baseUrl = "https://open.bigmodel.cn/api/paas/v4",
            defaultModel = "glm-4",
            description = "兼容 OpenAI 协议"
        ),
        ProviderConfig(
            key = "doubao",
            displayName = "豆包 (字节)",
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3",
            defaultModel = "doubao-pro-32k",
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "moonshot",
            displayName = "Kimi (月之暗面)",
            baseUrl = "https://api.moonshot.cn/v1",
            defaultModel = "moonshot-v1-8k",
            description = "兼容 OpenAI 协议，长上下文"
        ),
        ProviderConfig(
            key = "minimax",
            displayName = "MiniMax",
            baseUrl = "https://api.minimax.chat/v1",
            defaultModel = "abab6.5-chat",
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "openai",
            displayName = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            defaultModel = "gpt-3.5-turbo",
            description = "国际标准协议"
        ),
        ProviderConfig(
            key = "claude",
            displayName = "Claude (Anthropic)",
            baseUrl = "https://api.anthropic.com/v1",
            defaultModel = "claude-3-5-sonnet-20241022",
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "gemini",
            displayName = "Google Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            defaultModel = "gemini-1.5-flash",
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "groq",
            displayName = "Groq",
            baseUrl = "https://api.groq.com/openai/v1",
            defaultModel = "llama-3.1-70b-versatile",
            description = "高速推理，兼容 OpenAI"
        ),
        ProviderConfig(
            key = "mistral",
            displayName = "Mistral",
            baseUrl = "https://api.mistral.ai/v1",
            defaultModel = "mistral-large-latest",
            description = "兼容 OpenAI 协议"
        )
    )

    /** 根据 provider key 查询配置 */
    fun byKey(key: String): ProviderConfig? = ALL.firstOrNull { it.key == key }
}
