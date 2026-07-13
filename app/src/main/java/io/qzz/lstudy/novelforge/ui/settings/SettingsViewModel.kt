package io.qzz.lstudy.novelforge.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 设置页 ViewModel：管理 API Key 与导出模式 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingRepository: SettingRepository
) : ViewModel() {

    /** 默认导出模式 */
    val exportMode: StateFlow<String> = settingRepository
        .observeExportMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingRepository.EXPORT_MODE_LOCAL)

    /** 当前选中的 AI provider */
    val activeProvider: StateFlow<String> = settingRepository
        .observeActiveProvider()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek")

    /** 全部 API Key */
    val apiKeys: StateFlow<Map<String, String>> = settingRepository
        .observeApiKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setApiKey(provider: String, key: String) {
        viewModelScope.launch { settingRepository.setApiKey(provider, key) }
    }

    fun setExportMode(mode: String) {
        viewModelScope.launch { settingRepository.setExportMode(mode) }
    }

    fun setActiveProvider(provider: String) {
        viewModelScope.launch { settingRepository.setActiveProvider(provider) }
    }

    companion object {
        /** 内置 AI 供应商（provider 标识, 显示名称, 分类） */
        data class ProviderInfo(
            val key: String,
            val displayName: String,
            val category: String // "国内" / "国际"
        )

        /** 所有内置供应商，分类排序 */
        val PROVIDERS: List<ProviderInfo> = listOf(
            // 国内
            ProviderInfo("deepseek", "DeepSeek", "国内"),
            ProviderInfo("qwen", "通义千问 (阿里)", "国内"),
            ProviderInfo("ernie", "文心一言 (百度)", "国内"),
            ProviderInfo("glm", "智谱 GLM", "国内"),
            ProviderInfo("doubao", "豆包 (字节)", "国内"),
            ProviderInfo("moonshot", "Kimi (月之暗面)", "国内"),
            ProviderInfo("minimax", "MiniMax", "国内"),
            // 国际
            ProviderInfo("openai", "OpenAI", "国际"),
            ProviderInfo("claude", "Claude (Anthropic)", "国际"),
            ProviderInfo("gemini", "Google Gemini", "国际"),
            ProviderInfo("groq", "Groq", "国际"),
            ProviderInfo("mistral", "Mistral", "国际"),
            ProviderInfo("custom", "自定义供应商", "自定义")
        )

        /** 仅内置供应商（不含自定义），用于 Skill 选择等场景 */
        val BUILTIN_PROVIDERS = PROVIDERS.filter { it.key != "custom" }
    }
}