
package dev.cluely.keyboard.ui.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import dev.cluely.keyboard.data.api.OpenRouterClient
import dev.cluely.keyboard.data.memory.ConversationMemory
import dev.cluely.keyboard.data.storage.ApiKeyStore
import dev.cluely.keyboard.domain.models.ProviderCatalog
import dev.cluely.keyboard.ui.ime.CluelyIMEService
import dev.cluely.keyboard.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatOverlayService : Service() {

    @Inject lateinit var api: OpenRouterClient
    @Inject lateinit var apiKeyStore: ApiKeyStore
    @Inject lateinit var memory: ConversationMemory

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val screenshotBase64 = intent?.getStringExtra("screenshot_base64") ?: ""
        val conversationText = intent?.getStringExtra(EXTRA_CONVERSATION_TEXT).orEmpty()

        if (screenshotBase64.isBlank() && conversationText.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        createOverlay(screenshotBase64, conversationText)
        return START_NOT_STICKY
    }

    private fun createOverlay(screenshotBase64: String, conversationText: String) {
        val provider = ProviderCatalog.byId(apiKeyStore.getProviderId())
        val state = mutableStateOf(
            ReplyState(
                status = if (apiKeyStore.isConfigured()) {
                    "🔮 Asking ${provider.emoji} ${provider.name} (${apiKeyStore.getModel()})…"
                } else {
                    "🔑 No ${provider.name} key yet — open setup to add one!"
                },
                suggestions = emptyList()
            )
        )

        composeView = ComposeView(this).apply {
            setContent {
                ReplyOverlay(
                    state = state.value,
                    onSuggestion = {
                        serviceScope.launch {
                            memory.recordAcceptedReply(it)
                            sendSuggestionToKeyboard(it)
                            stopSelf()
                        }
                    },
                    onSettings = {
                        startActivity(Intent(this@ChatOverlayService, SettingsActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        stopSelf()
                    },
                    onClose = { stopSelf() }
                )
            }
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 70
        }

        try {
            windowManager?.addView(composeView, layoutParams)
        } catch (e: Exception) {
            Log.e("ChatOverlayService", "Could not show overlay", e)
            stopSelf()
            return
        }

        if (!apiKeyStore.isConfigured()) return

        serviceScope.launch {
            val localMemory = memory.promptSummary()
            val result = if (conversationText.isNotBlank()) {
                api.generateFromText(conversationText.take(MAX_CONVERSATION_CHARS), localMemory)
            } else {
                api.analyzeScreenshot(
                    imageBase64 = screenshotBase64,
                    memory = localMemory
                )
            }

            result.onSuccess { raw ->
                val suggestions = parseSuggestions(raw)
                if (suggestions.isEmpty()) {
                    state.value = ReplyState("🤔 The model returned no usable replies. Try again!", emptyList())
                } else {
                    state.value = ReplyState("🔥 Pick your winner:", suggestions)
                }
                refresh(state)
            }.onFailure { error ->
                state.value = ReplyState(
                    friendlyError(error),
                    emptyList()
                )
                refresh(state)
            }
        }
    }

    private fun parseSuggestions(raw: String): List<String> =
        raw.lines()
            .map { it.trim() }
            .map { it.replace(Regex("^\\d+[.)]\\s*"), "") }
            .map { it.removePrefix("- ").removePrefix("• ") }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)

    private fun friendlyError(error: Throwable): String {
        val provider = ProviderCatalog.byId(apiKeyStore.getProviderId())
        val message = error.message.orEmpty()
        return when {
            "401" in message || "403" in message ->
                "🔑 ${provider.name} rejected the key. Open Settings and check it."
            "429" in message ->
                "⏳ ${provider.name} rate limit. Chill a sec or switch models."
            "402" in message ->
                "💳 This model needs credits. Try a 🆓 free one!"
            else ->
                "😅 Couldn't get suggestions: ${message.ifBlank { "unknown error" }}"
        }
    }

    private fun refresh(state: androidx.compose.runtime.MutableState<ReplyState>) {
        composeView?.setContent {
            ReplyOverlay(
                state = state.value,
                onSuggestion = {
                    serviceScope.launch {
                        memory.recordAcceptedReply(it)
                        sendSuggestionToKeyboard(it)
                        stopSelf()
                    }
                },
                onSettings = {
                    startActivity(Intent(this@ChatOverlayService, SettingsActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                    stopSelf()
                },
                onClose = { stopSelf() }
            )
        }
    }

    private fun sendSuggestionToKeyboard(suggestion: String) {
        startService(
            Intent(this, CluelyIMEService::class.java).apply {
                action = CluelyIMEService.ACTION_INSERT_SUGGESTION
                putExtra(CluelyIMEService.EXTRA_SUGGESTION, suggestion)
            }
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            composeView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.e("ChatOverlayService", "Overlay cleanup failed", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_CONVERSATION_TEXT = "conversation_text"
        private const val MAX_CONVERSATION_CHARS = 12_000
    }
}

data class ReplyState(
    val status: String,
    val suggestions: List<String>
)

@androidx.compose.runtime.Composable
fun ReplyOverlay(
    state: ReplyState,
    onSuggestion: (String) -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = MaterialTheme.shapes.large)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("✨ Cluely • AI Replies 🔥", fontSize = 17.sp, color = Color.Black)
            TextButton(onClick = onClose) { Text("✌️ Close") }
        }

        Text(
            state.status,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (state.suggestions.isEmpty() && (state.status.contains("key") || state.status.contains("🔑"))) {
            Button(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 Open AI setup")
            }
        }

        state.suggestions.forEach { suggestion ->
            Button(
                onClick = { onSuggestion(suggestion) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(suggestion)
            }
        }
    }
}
