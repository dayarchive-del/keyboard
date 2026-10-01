package dev.cluely.keyboard.data.api

import android.util.Log
import dev.cluely.keyboard.data.storage.ApiKeyStore
import dev.cluely.keyboard.domain.models.FreeModelPicker
import dev.cluely.keyboard.domain.models.ModelInfo
import dev.cluely.keyboard.domain.models.ModelsResponse
import dev.cluely.keyboard.domain.models.OpenRouterMessage
import dev.cluely.keyboard.domain.models.OpenRouterRequest
import dev.cluely.keyboard.domain.models.OpenRouterResponse
import dev.cluely.keyboard.domain.models.ProviderCatalog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic OpenAI-compatible client. Works with OpenRouter, OpenCode Zen,
 * or any custom base URL. Kept the old class name so Hilt injection
 * ([AppModule], [ChatOverlayService], [SettingsActivity]) keeps working.
 */
@Singleton
class OpenRouterClient @Inject constructor(
    private val httpClient: HttpClient,
    private val apiKeyStore: ApiKeyStore
) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun baseUrl(): String = apiKeyStore.getBaseUrl().trimEnd('/')

    private fun provider() = ProviderCatalog.byId(apiKeyStore.getProviderId())

    private fun requireKey(): String =
        apiKeyStore.getApiKey().ifBlank {
            throw IllegalStateException("${provider().name} API key is not configured.")
        }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val key = requireKey()
            val url = "${baseUrl()}/models"
            val response = httpClient.get(url) {
                header("Authorization", "Bearer $key")
            }
            if (!response.status.isSuccess()) {
                error("${provider().name} returned ${response.status.value}: ${response.bodyAsText().take(300)}")
            }
            val models = response.body<ModelsResponse>()
            // ✨ Auto-select best free model for this app's needs.
            val best = FreeModelPicker.pickBest(provider().id, models.data.map { it.id })
            if (best != null && best != apiKeyStore.getModel()) {
                apiKeyStore.setModel(best)
            }
            val autoNote = if (best != null) "\n${FreeModelPicker.reason(provider().id, best)}" else ""
            "Connected to ${provider().emoji} ${provider().name} • ${models.data.size} models$autoNote"
        }.onFailure {
            Log.e("OpenRouterClient", "Connection test failed", it)
        }
    }

    /** Fetch raw model ids from GET {baseUrl}/models. Never throws — returns failure Result. */
    suspend fun fetchModels(): Result<List<ModelInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val key = requireKey()
            val response = httpClient.get("${baseUrl()}/models") {
                header("Authorization", "Bearer $key")
            }
            if (!response.status.isSuccess()) {
                error("${provider().name} returned ${response.status.value}: ${response.bodyAsText().take(300)}")
            }
            response.body<ModelsResponse>().data
        }
    }

    /** Pick + save the best free vision model. Returns model id or null. */
    suspend fun autoPickBestFreeModel(): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val models = fetchModels().getOrThrow()
            val best = FreeModelPicker.pickBest(provider().id, models.map { it.id })
                ?: error("No models returned by ${provider().name}.")
            apiKeyStore.setModel(best)
            best
        }
    }

    suspend fun analyzeScreenshot(
        imageBase64: String,
        memory: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                analyzeScreenshotOnce(requireKey(), apiKeyStore.getModel(), imageBase64, memory)
            } catch (e: Exception) {
                // 🔄 Model gone / no longer free? Auto-pick best free + retry once.
                if (isModelError(e)) {
                    val best = autoPickBestFreeModel().getOrThrow()
                    analyzeScreenshotOnce(requireKey(), best, imageBase64, memory)
                } else throw e
            }
        }.onFailure {
            Log.e("OpenRouterClient", "Screenshot request failed", it)
        }
    }

    private fun isModelError(e: Exception): Boolean {
        val m = (e.message ?: "").lowercase()
        return "404" in m || "no endpoints" in m || "not found" in m ||
            "invalid model" in m || "does not exist" in m || "402" in m
    }

    private suspend fun analyzeScreenshotOnce(
        key: String,
        model: String,
        imageBase64: String,
        memory: String
    ): String {
            val p = provider()
            val prompt = """
                You write chat replies that sound exactly like the user.

                Screenshot is the conversation source of truth.
                Local style memory (clone this voice):
                ${memory.take(2000)}

                Detect emotion, relationship, and the latest message that needs a reply.
                Match the user's length, slang, emoji habits, and punctuation from memory.
                Do not sound like an AI assistant.

                Return EXACTLY 3 candidate replies.
                One reply per line.
                Do not number them.
                Do not explain them.
                Keep them short and natural.
                Avoid repeating questions already answered.
            """.trimIndent()

            val requestJson = """
                {
                  "model": ${json.encodeToString(model)},
                  "messages": [{
                    "role": "user",
                    "content": [
                      {"type":"text","text":${json.encodeToString(prompt)}},
                      {"type":"image_url","image_url":{"url":"data:image/png;base64,$imageBase64"}}
                    ]
                  }],
                  "max_tokens": 350,
                  "temperature": 0.7
                }
            """.trimIndent()

            val response = httpClient.post("${baseUrl()}/chat/completions") {
                header("Authorization", "Bearer $key")
                if (p.needsRefererHeaders) {
                    header("HTTP-Referer", "https://github.com/filiksyos/cluely-keyboard-android")
                    header("X-Title", "Cluely Keyboard")
                }
                contentType(ContentType.Application.Json)
                setBody(requestJson)
            }

            if (!response.status.isSuccess()) {
                error("${p.name} returned ${response.status.value}: ${response.bodyAsText().take(500)}")
            }

            val parsed = json.decodeFromString<OpenRouterResponse>(response.bodyAsText())
            return parsed.choices.firstOrNull()?.message?.content?.trim()
                ?: error("${p.name} returned no text response.")
    }

    suspend fun generateFromText(
        conversation: String,
        memory: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val key = requireKey()
            val model = apiKeyStore.getModel()
            val p = provider()

            val prompt = """
                You write chat replies that sound exactly like the user.

                CONVERSATION (from on-screen text — no screenshot was taken):
                $conversation

                LOCAL STYLE MEMORY (clone this voice — length, slang, emoji, tone):
                ${memory.take(2000)}

                Detect emotion and the latest message that needs a reply.
                Match the user's real texting style from memory.
                Do not sound like an AI assistant or customer-support bot.

                Return EXACTLY 3 candidate replies.
                One reply per line.
                Do not number them.
                Do not explain them.
                Keep them short and natural.
            """.trimIndent()

            val request = OpenRouterRequest(
                model = model,
                messages = listOf(
                    OpenRouterMessage("system", "Write like a normal human texting, not an AI assistant."),
                    OpenRouterMessage("user", prompt)
                ),
                maxTokens = 300,
                temperature = 0.7
            )

            val response = httpClient.post("${baseUrl()}/chat/completions") {
                header("Authorization", "Bearer $key")
                if (p.needsRefererHeaders) {
                    header("HTTP-Referer", "https://github.com/filiksyos/cluely-keyboard-android")
                    header("X-Title", "Cluely Keyboard")
                }
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                error("${p.name} returned ${response.status.value}: ${response.bodyAsText().take(500)}")
            }

            json.decodeFromString<OpenRouterResponse>(response.bodyAsText())
                .choices.firstOrNull()?.message?.content?.trim()
                ?: error("${p.name} returned no text response.")
        }
    }
}
