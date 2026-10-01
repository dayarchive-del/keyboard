
package dev.cluely.keyboard.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 350,
    val temperature: Double = 0.7
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterResponse(
    val choices: List<OpenRouterChoice> = emptyList()
)

@Serializable
data class OpenRouterChoice(
    val message: OpenRouterResponseMessage? = null
)

@Serializable
data class OpenRouterResponseMessage(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class ModelsResponse(
    val data: List<ModelInfo> = emptyList()
)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String? = null,
    @SerialName("context_length") val contextLength: Long? = null
)
