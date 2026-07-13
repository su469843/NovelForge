package io.qzz.lstudy.novelforge.data.ai

/**
 * AI 供应商配置
 *
 * 包含 provider 标识、显示名、BaseUrl、内置模型列表等信息。
 * 大多数国内供应商都提供 OpenAI 兼容协议，可统一通过 [AiClient] 调用。
 */
data class ProviderConfig(
    val key: String,
    val displayName: String,
    /** OpenAI 兼容协议的 BaseUrl，末尾不含斜杠 */
    val baseUrl: String,
    /** 内置模型列表（用户可在设置中追加自定义模型） */
    val models: List<String>,
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
 *
 * 模型清单参考各供应商 2026 年公开文档：
 * - DeepSeek V4 系列（deepseek-chat / deepseek-reasoner 旧名将于 2026-07-24 停用，
 *   分别对应 V4-Flash 非思考模式与思考模式）
 * - 智谱 GLM-4.7-Flash（2026-01 发布，免费开放，替代 GLM-4.5-Flash）
 * - 通义千问 Qwen3.5-Plus（2026-02 发布）与 Qwen-Max
 * - MiniMax abab6.5-chat / MiniMax-Text-01
 */
object ProviderConfigs {

    val ALL: List<ProviderConfig> = listOf(
        ProviderConfig(
            key = "deepseek",
            displayName = "DeepSeek",
            baseUrl = "https://api.deepseek.com/v1",
            models = listOf(
                "deepseek-chat",
                "deepseek-reasoner",
                "deepseek-v4-flash",
                "deepseek-v4-pro"
            ),
            description = "国内可用，性价比高"
        ),
        ProviderConfig(
            key = "qwen",
            displayName = "通义千问 (阿里)",
            baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1",
            models = listOf(
                "qwen-plus",
                "qwen-max",
                "qwen-turbo",
                "qwen3.5-plus"
            ),
            description = "兼容 OpenAI 协议"
        ),
        ProviderConfig(
            key = "ernie",
            displayName = "文心一言 (百度)",
            baseUrl = "https://qianfan.baidubce.com/v2",
            models = listOf("ernie-bot-turbo", "ernie-bot-4"),
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "glm",
            displayName = "智谱 GLM",
            baseUrl = "https://open.bigmodel.cn/api/paas/v4",
            models = listOf(
                "glm-4",
                "glm-4-flash",
                "glm-4-plus",
                "glm-4.5-flash",
                "glm-4.7-flash"
            ),
            description = "兼容 OpenAI 协议，4.7-Flash 免费"
        ),
        ProviderConfig(
            key = "doubao",
            displayName = "豆包 (字节)",
            baseUrl = "https://ark.cn-beijing.volces.com/api/v3",
            models = listOf("doubao-pro-32k", "doubao-pro-4k"),
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "moonshot",
            displayName = "Kimi (月之暗面)",
            baseUrl = "https://api.moonshot.cn/v1",
            models = listOf(
                "moonshot-v1-8k",
                "moonshot-v1-32k",
                "moonshot-v1-128k"
            ),
            description = "兼容 OpenAI 协议，长上下文"
        ),
        ProviderConfig(
            key = "minimax",
            displayName = "MiniMax",
            baseUrl = "https://api.minimax.chat/v1",
            models = listOf("abab6.5-chat", "abab6-chat", "MiniMax-Text-01"),
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "openai",
            displayName = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            models = listOf("gpt-3.5-turbo", "gpt-4", "gpt-4o", "gpt-4o-mini"),
            description = "国际标准协议"
        ),
        ProviderConfig(
            key = "claude",
            displayName = "Claude (Anthropic)",
            baseUrl = "https://api.anthropic.com/v1",
            models = listOf(
                "claude-3-5-sonnet-20241022",
                "claude-3-opus-20240229",
                "claude-3-haiku-20240307"
            ),
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "gemini",
            displayName = "Google Gemini",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash"),
            openAiCompatible = false,
            description = "暂不支持直接调用"
        ),
        ProviderConfig(
            key = "groq",
            displayName = "Groq",
            baseUrl = "https://api.groq.com/openai/v1",
            models = listOf("llama-3.1-70b-versatile", "llama-3.1-8b-instant"),
            description = "高速推理，兼容 OpenAI"
        ),
        ProviderConfig(
            key = "mistral",
            displayName = "Mistral",
            baseUrl = "https://api.mistral.ai/v1",
            models = listOf("mistral-large-latest", "mistral-small-latest"),
            description = "兼容 OpenAI 协议"
        )
    )

    /** 根据 provider key 查询配置 */
    fun byKey(key: String): ProviderConfig? = ALL.firstOrNull { it.key == key }
}
