package io.qzz.lstudy.novelforge.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 调用结果
 */
sealed class AiResult {
    data class Success(val content: String) : AiResult()
    data class Error(val message: String, val code: Int = -1) : AiResult()
}

/**
 * AI 客户端：调用 OpenAI 兼容协议的 chat/completions 接口
 *
 * 使用 HttpURLConnection，无需引入额外依赖。
 * 支持 DeepSeek、通义千问、智谱 GLM、Kimi、Groq、Mistral、OpenAI 等兼容协议的供应商。
 * 自定义供应商需用户在设置中填写 BaseUrl 与模型名。
 */
@Singleton
class AiClient @Inject constructor() {

    /**
     * 调用 AI 生成文本
     *
     * @param baseUrl  OpenAI 兼容协议的 BaseUrl，如 https://api.deepseek.com/v1
     * @param apiKey   API Key
     * @param model    模型名，如 deepseek-chat
     * @param systemPrompt  系统提示词
     * @param userPrompt    用户提示词
     * @param temperature   采样温度，0.0-2.0
     * @param maxTokens     最大生成 token 数
     */
    suspend fun chat(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        temperature: Double = 0.8,
        maxTokens: Int = 2048
    ): AiResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext AiResult.Error("BaseUrl 未配置，请到设置中填写")
        }
        if (apiKey.isBlank()) {
            return@withContext AiResult.Error("API Key 未配置，请到设置中填写")
        }
        if (model.isBlank()) {
            return@withContext AiResult.Error("模型名未配置，请到设置中填写")
        }

        val endpoint = baseUrl.trimEnd('/') + "/chat/completions"
        var conn: HttpURLConnection? = null
        try {
            conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30_000
                readTimeout = 120_000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            // 构造请求体
            val body = JSONObject().apply {
                put("model", model)
                put("temperature", temperature)
                put("max_tokens", maxTokens)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream?.bufferedReader()?.use(BufferedReader::readText) ?: ""
            if (code !in 200..299) {
                val msg = try {
                    JSONObject(responseText).optJSONObject("error")?.optString("message") ?: responseText
                } catch (_: Exception) {
                    responseText.ifBlank { "HTTP $code" }
                }
                return@withContext AiResult.Error("请求失败 (HTTP $code)：$msg", code)
            }

            val content = try {
                JSONObject(responseText)
                    .optJSONArray("choices")
                    ?.optJSONObject(0)
                    ?.optJSONObject("message")
                    ?.optString("content")
                    ?: ""
            } catch (e: Exception) {
                return@withContext AiResult.Error("解析响应失败：${e.message}")
            }

            if (content.isBlank()) {
                return@withContext AiResult.Error("AI 返回内容为空")
            }
            AiResult.Success(content)
        } catch (e: java.net.SocketTimeoutException) {
            AiResult.Error("请求超时：${e.message}")
        } catch (e: java.net.UnknownHostException) {
            AiResult.Error("无法连接到服务器：${e.message}，请检查 BaseUrl 与网络")
        } catch (e: Exception) {
            AiResult.Error("请求异常：${e.message}")
        } finally {
            conn?.disconnect()
        }
    }
}
