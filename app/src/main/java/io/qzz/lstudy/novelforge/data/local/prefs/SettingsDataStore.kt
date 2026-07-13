package io.qzz.lstudy.novelforge.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用级 DataStore 单例
 * 用 preferencesDataStore 在文件系统持久化用户设置
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "novelforge_settings"
)

/**
 * 用户设置 DataStore
 *
 * 存储内容：
 * 1. 各 AI 模型的 API Key（Map<String, String>，key 为 provider 标识，value 为密钥）
 *    实现方式：每个 provider 用独立 preference key 存储，前缀 [API_KEY_PREFIX]
 * 2. 默认导出模式（local / cloud）
 * 3. 当前选中的 AI 模型 provider
 * 4. 默认目标章节数与每章字数
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** 观察所有 API Key，返回 Map<provider, key>。读取异常时回退为空 Map */
    fun observeApiKeys(): Flow<Map<String, String>> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs ->
            prefs.asMap().mapNotNull { (key, value) ->
                val name = key.name
                if (name.startsWith(KEY_API_KEY_PREFIX) && value is String && value.isNotEmpty()) {
                    name.removePrefix(KEY_API_KEY_PREFIX) to value
                } else null
            }.toMap()
        }

    /** 一次性获取所有 API Key（首次读取可能为空，推荐用 observeApiKeys 观察） */
    suspend fun getApiKeys(): Map<String, String> {
        var result: Map<String, String> = emptyMap()
        context.settingsDataStore.data
            .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
            .map { prefs ->
                prefs.asMap().mapNotNull { (key, value) ->
                    val name = key.name
                    if (name.startsWith(KEY_API_KEY_PREFIX) && value is String && value.isNotEmpty()) {
                        name.removePrefix(KEY_API_KEY_PREFIX) to value
                    } else null
                }.toMap()
            }
            .collect { result = it; return@collect }
        return result
    }

    /** 观察单个 provider 的 API Key */
    fun observeApiKey(provider: String): Flow<String?> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[stringPreferencesKey(KEY_API_KEY_PREFIX + provider)] }

    /** 设置/更新某个 provider 的 API Key；传入空串等价于删除 */
    suspend fun setApiKey(provider: String, apiKey: String) {
        context.settingsDataStore.edit { prefs ->
            val key = stringPreferencesKey(KEY_API_KEY_PREFIX + provider)
            if (apiKey.isEmpty()) {
                prefs.remove(key)
            } else {
                prefs[key] = apiKey
            }
        }
    }

    /** 观察默认导出模式 */
    fun observeExportMode(): Flow<String> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_EXPORT_MODE] ?: DEFAULT_EXPORT_MODE }

    /** 设置默认导出模式 */
    suspend fun setExportMode(mode: String) {
        context.settingsDataStore.edit { it[KEY_EXPORT_MODE] = mode }
    }

    /** 观察当前选中的 AI 模型 provider */
    fun observeActiveProvider(): Flow<String> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_ACTIVE_PROVIDER] ?: DEFAULT_PROVIDER }

    /** 设置当前选中的 AI 模型 provider */
    suspend fun setActiveProvider(provider: String) {
        context.settingsDataStore.edit { it[KEY_ACTIVE_PROVIDER] = provider }
    }

    /** 观察默认目标章节数 */
    fun observeDefaultChapterCount(): Flow<Int> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_DEFAULT_CHAPTER_COUNT] ?: DEFAULT_CHAPTER_COUNT }

    /** 设置默认目标章节数 */
    suspend fun setDefaultChapterCount(count: Int) {
        context.settingsDataStore.edit { it[KEY_DEFAULT_CHAPTER_COUNT] = count }
    }

    /** 观察默认每章字数 */
    fun observeDefaultWordsPerChapter(): Flow<Int> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_DEFAULT_WORDS_PER_CHAPTER] ?: DEFAULT_WORDS_PER_CHAPTER }

    /** 设置默认每章字数 */
    suspend fun setDefaultWordsPerChapter(words: Int) {
        context.settingsDataStore.edit { it[KEY_DEFAULT_WORDS_PER_CHAPTER] = words }
    }

    companion object {
        private const val KEY_API_KEY_PREFIX = "api_key_"
        private val KEY_EXPORT_MODE = stringPreferencesKey("export_mode")
        private val KEY_ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        private val KEY_DEFAULT_CHAPTER_COUNT =
            androidx.datastore.preferences.core.intPreferencesKey("default_chapter_count")
        private val KEY_DEFAULT_WORDS_PER_CHAPTER =
            androidx.datastore.preferences.core.intPreferencesKey("default_words_per_chapter")
        private val KEY_APP_THEME = stringPreferencesKey("app_theme")
        private val KEY_CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
        private val KEY_CUSTOM_MODEL = stringPreferencesKey("custom_model")

        /** 默认导出模式：本地 */
        const val DEFAULT_EXPORT_MODE = "local"

        /** 默认 AI provider：DeepSeek（国内可用、成本低） */
        const val DEFAULT_PROVIDER = "deepseek"

        /** 默认目标章节数 */
        const val DEFAULT_CHAPTER_COUNT = 10

        /** 默认每章字数 */
        const val DEFAULT_WORDS_PER_CHAPTER = 2000

        /** 默认主题：紫色 Material */
        const val DEFAULT_THEME = "purple"
    }

    /** 观察当前主题名称 */
    fun observeTheme(): Flow<String> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_APP_THEME] ?: DEFAULT_THEME }

    /** 设置主题 */
    suspend fun setTheme(themeName: String) {
        context.settingsDataStore.edit { it[KEY_APP_THEME] = themeName }
    }

    // ===================== 自定义供应商配置 =====================

    /** 观察自定义供应商的 BaseUrl */
    fun observeCustomBaseUrl(): Flow<String> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_CUSTOM_BASE_URL] ?: "" }

    /** 设置自定义供应商的 BaseUrl */
    suspend fun setCustomBaseUrl(url: String) {
        context.settingsDataStore.edit { it[KEY_CUSTOM_BASE_URL] = url }
    }

    /** 观察自定义供应商的模型名 */
    fun observeCustomModel(): Flow<String> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { it[KEY_CUSTOM_MODEL] ?: "" }

    /** 设置自定义供应商的模型名 */
    suspend fun setCustomModel(model: String) {
        context.settingsDataStore.edit { it[KEY_CUSTOM_MODEL] = model }
    }
}
