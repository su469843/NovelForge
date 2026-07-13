package io.qzz.lstudy.novelforge.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.qzz.lstudy.novelforge.data.ai.ProviderConfigs
import io.qzz.lstudy.novelforge.data.repository.SettingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 设置页 ViewModel：管理 API Key、导出模式、主题、自定义供应商配置、自定义模型 */
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

    /** 当前主题名称 */
    val appTheme: StateFlow<String> = settingRepository
        .observeTheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "purple")

    /** 自定义供应商 BaseUrl */
    val customBaseUrl: StateFlow<String> = settingRepository
        .observeCustomBaseUrl()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** 自定义供应商模型名 */
    val customModel: StateFlow<String> = settingRepository
        .observeCustomModel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /** 各 provider 的自定义模型列表（按 provider key 索引） */
    val customModels: StateFlow<Map<String, List<String>>> = combine(
        ProviderConfigs.ALL.map { cfg ->
            settingRepository.observeCustomModels(cfg.key)
                .map { list -> cfg.key to list }
        }
    ) { pairs ->
        pairs.toMap()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    fun setApiKey(provider: String, key: String) {
        viewModelScope.launch { settingRepository.setApiKey(provider, key) }
    }

    fun setExportMode(mode: String) {
        viewModelScope.launch { settingRepository.setExportMode(mode) }
    }

    fun setActiveProvider(provider: String) {
        viewModelScope.launch { settingRepository.setActiveProvider(provider) }
    }

    fun setTheme(themeName: String) {
        viewModelScope.launch { settingRepository.setTheme(themeName) }
    }

    fun setCustomBaseUrl(url: String) {
        viewModelScope.launch { settingRepository.setCustomBaseUrl(url) }
    }

    fun setCustomModel(model: String) {
        viewModelScope.launch { settingRepository.setCustomModel(model) }
    }

    fun addCustomModel(provider: String, model: String) {
        viewModelScope.launch { settingRepository.addCustomModel(provider, model) }
    }

    fun removeCustomModel(provider: String, model: String) {
        viewModelScope.launch { settingRepository.removeCustomModel(provider, model) }
    }

    companion object {
        /** 内置 AI 供应商（provider 标识, 显示名称, 分类） */
        data class ProviderInfo(
            val key: String,
            val displayName: String,
            val category: String, // "国内" / "国际"
            /** 显示用的首字母（用于圆形头像） */
            val initial: String,
            /** 是否支持通过本应用直接调用（OpenAI 兼容协议） */
            val callable: Boolean,
            /** 内置模型列表 */
            val models: List<String>
        )

        /** 所有内置供应商，分类排序 */
        val PROVIDERS: List<ProviderInfo> = ProviderConfigs.ALL.map { cfg ->
            ProviderInfo(
                key = cfg.key,
                displayName = cfg.displayName,
                category = if (
                    cfg.key in listOf("deepseek", "qwen", "ernie", "glm", "doubao", "moonshot", "minimax")
                ) "国内" else "国际",
                initial = cfg.displayName.firstOrNull()?.toString() ?: "?",
                callable = cfg.openAiCompatible,
                models = cfg.models
            )
        } + ProviderInfo(
            key = "custom",
            displayName = "自定义供应商",
            category = "自定义",
            initial = "C",
            callable = true,
            models = emptyList()
        )

        /** 仅内置供应商（不含自定义），用于 Skill 选择等场景 */
        val BUILTIN_PROVIDERS = PROVIDERS.filter { it.key != "custom" }
    }
}
