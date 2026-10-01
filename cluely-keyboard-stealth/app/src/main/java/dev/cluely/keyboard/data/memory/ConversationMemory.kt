package dev.cluely.keyboard.data.memory

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.memoryDataStore by preferencesDataStore("cluely_memory")

@Serializable
data class ReplyExample(
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val contextSnippet: String = ""
)

data class ReplyStyleMemory(
    val preference: String,
    val examples: List<ReplyExample>
)

/**
 * On-device automatic reply-style memory.
 *
 * Learns from every accepted or edited suggestion. Stores only local
 * style/context summaries. Fully editable and deletable. Never leaves the phone.
 */
@Singleton
class ConversationMemory @Inject constructor(
    private val context: Context
) {
    private val preferenceKey = stringPreferencesKey("reply_style_preference")
    private val examplesKey = stringPreferencesKey("accepted_reply_examples")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun recordAcceptedReply(reply: String, contextSnippet: String = "") {
        val clean = reply.trim().replace(Regex("\\s+"), " ").take(MAX_EXAMPLE_LENGTH)
        if (clean.isBlank()) return
        val snippet = contextSnippet.trim().replace(Regex("\\s+"), " ").take(MAX_CONTEXT_SNIPPET)
        val examples = getExamples()
            .filterNot { it.text.equals(clean, ignoreCase = true) }
            .takeLast(MAX_EXAMPLES - 1) + ReplyExample(clean, contextSnippet = snippet)
        context.memoryDataStore.edit {
            it[examplesKey] = json.encodeToString(examples)
        }
        // Light auto-style inference from recent examples when preference is empty.
        if (getStylePreference().isBlank() && examples.size >= 3) {
            val inferred = inferStyleHint(examples)
            if (inferred.isNotBlank()) setStylePreference(inferred)
        }
    }

    suspend fun getStyleMemory(): ReplyStyleMemory =
        ReplyStyleMemory(getStylePreference(), getExamples())

    suspend fun getStylePreference(): String =
        context.memoryDataStore.data.first()[preferenceKey].orEmpty().take(MAX_PREFERENCE_LENGTH)

    suspend fun setStylePreference(value: String) {
        context.memoryDataStore.edit {
            it[preferenceKey] = value.trim().take(MAX_PREFERENCE_LENGTH)
        }
    }

    suspend fun getExamples(): List<ReplyExample> {
        val raw = context.memoryDataStore.data.first()[examplesKey] ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<ReplyExample>>(raw).takeLast(MAX_EXAMPLES)
        }.getOrDefault(emptyList())
    }

    suspend fun clearExamples() {
        context.memoryDataStore.edit { it.remove(examplesKey) }
    }

    suspend fun promptSummary(): String {
        val memory = getStyleMemory()
        return buildString {
            append("Reply in the user's exact voice.\n")
            if (memory.preference.isNotBlank()) {
                append("Preferred style: ${memory.preference}\n")
            }
            if (memory.examples.isNotEmpty()) {
                append("Recent accepted replies (clone tone, length, emoji use, slang):\n")
                memory.examples.takeLast(MAX_EXAMPLES).forEachIndexed { i, ex ->
                    append("${i + 1}. ${ex.text}\n")
                }
            } else {
                append("No style examples yet — write natural short texts.\n")
            }
        }.trim().take(MAX_SUMMARY_LENGTH)
    }

    suspend fun clear() {
        context.memoryDataStore.edit {
            it.remove(preferenceKey)
            it.remove(examplesKey)
        }
    }

    private fun inferStyleHint(examples: List<ReplyExample>): String {
        val texts = examples.map { it.text }
        val avgLen = texts.map { it.length }.average()
        val emojiHeavy = texts.count { it.any { c -> c.code > 0x1F300 } } >= texts.size / 2
        val shortForm = avgLen < 40
        return buildString {
            if (shortForm) append("short casual texts; ")
            if (emojiHeavy) append("uses emoji often; ")
            append("match the user's punctuation and slang")
        }.trim().trimEnd(';').take(MAX_PREFERENCE_LENGTH)
    }

    companion object {
        const val MAX_EXAMPLES = 12
        const val MAX_EXAMPLE_LENGTH = 220
        const val MAX_CONTEXT_SNIPPET = 120
        const val MAX_PREFERENCE_LENGTH = 320
        const val MAX_SUMMARY_LENGTH = 2200
    }
}
