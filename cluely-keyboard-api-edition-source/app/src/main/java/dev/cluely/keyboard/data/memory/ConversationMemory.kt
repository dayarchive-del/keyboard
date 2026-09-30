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
data class MemoryTurn(
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Singleton
class ConversationMemory @Inject constructor(
    private val context: Context
) {
    private val key = stringPreferencesKey("conversation_memory")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun get(): List<MemoryTurn> {
        val raw = context.memoryDataStore.data.first()[key] ?: return emptyList()
        return runCatching { json.decodeFromString<List<MemoryTurn>>(raw) }
            .getOrDefault(emptyList())
    }

    suspend fun append(vararg turns: MemoryTurn) {
        val current = get().toMutableList()
        current += turns
        // Keep a useful rolling window; the model sees recent turns plus the current screen.
        val trimmed = current.takeLast(30)
        context.memoryDataStore.edit { it[key] = json.encodeToString(trimmed) }
    }

    suspend fun clear() {
        context.memoryDataStore.edit { it.remove(key) }
    }
}
