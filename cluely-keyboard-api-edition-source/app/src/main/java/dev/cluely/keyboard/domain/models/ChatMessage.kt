package dev.cluely.keyboard.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = "",
    val content: String,
    val isUserMessage: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class VisionAnalysisRequest(
    val imageBase64: String,
    val question: String?
)

@Serializable
data class OpenRouterMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val max_tokens: Int = 500
)

@Serializable
data class OpenRouterResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

@Serializable
data class Message(
    val content: String
)