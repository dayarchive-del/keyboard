package dev.cluely.keyboard.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import dev.cluely.keyboard.data.api.OpenRouterClient
import dev.cluely.keyboard.data.memory.ConversationMemory
import dev.cluely.keyboard.data.storage.ApiKeyStore
import dev.cluely.keyboard.domain.models.ProviderCatalog
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject lateinit var apiKeyStore: ApiKeyStore
    @Inject lateinit var api: OpenRouterClient
    @Inject lateinit var memory: ConversationMemory

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var providerId by rememberSaveable { mutableStateOf(apiKeyStore.getProviderId()) }
            var apiKey by rememberSaveable(providerId) { mutableStateOf(apiKeyStore.getApiKey(providerId)) }
            var model by rememberSaveable(providerId) { mutableStateOf(apiKeyStore.getModel(providerId)) }
            var customUrl by rememberSaveable { mutableStateOf(apiKeyStore.getCustomBaseUrl()) }
            var showKey by rememberSaveable { mutableStateOf(false) }
            var status by remember { mutableStateOf("") }
            var testing by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            val provider = ProviderCatalog.byId(providerId)
            val funTitle = "⌨️ Cluely Keyboard"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(funTitle, style = MaterialTheme.typography.headlineMedium)
                Text(
                    "✨ Your sneaky chat sidekick — pick a brain, paste a key, tap AI!",
                    style = MaterialTheme.typography.titleMedium
                )

                Text("1️⃣ Pick a provider", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProviderCatalog.ALL.forEach { p ->
                        FilterChip(
                            selected = p.id == providerId,
                            onClick = {
                                // Persist current edits before switching so keys aren't lost.
                                apiKeyStore.setApiKey(providerId, apiKey)
                                apiKeyStore.setModel(providerId, model.ifBlank { ProviderCatalog.byId(providerId).defaultModel })
                                if (ProviderCatalog.byId(providerId).isCustom) {
                                    apiKeyStore.setCustomBaseUrl(customUrl)
                                }
                                providerId = p.id
                                apiKeyStore.setProviderId(p.id)
                                status = ""
                            },
                            label = { Text("${p.emoji} ${p.name}") }
                        )
                    }
                }

                Card(colors = CardDefaults.cardColors()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("ℹ️ ${provider.helpText}", style = MaterialTheme.typography.bodyMedium)
                        if (provider.keyUrl.isNotBlank()) {
                            Text(
                                "🔑 Get a key: ${provider.keyUrl}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            "🌐 Endpoint: ${if (provider.isCustom) customUrl.ifBlank { "enter below 👇" } else provider.baseUrl}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (provider.isCustom) {
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it; status = "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("🔧 Custom base URL") },
                        placeholder = { Text("https://api.openai.com/v1") },
                        supportingText = { Text("Must be OpenAI-compatible: /chat/completions + /models") },
                        singleLine = true
                    )
                }

                Text("2️⃣ Paste your key", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; status = "" },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("${provider.emoji} ${provider.name} API key") },
                    placeholder = { Text(provider.keyHint) },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showKey = !showKey }) {
                        Text(if (showKey) "🙈 Hide key" else "👀 Show key")
                    }
                    TextButton(
                        onClick = {
                            apiKey = ""
                            apiKeyStore.clearApiKey()
                            status = "🧹 Key removed for ${provider.name}."
                        }
                    ) {
                        Text("🧹 Clear")
                    }
                }

                Text("3️⃣ Pick a model — or let us auto-pick ✨", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it; status = "" },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("🤖 Model") },
                    supportingText = { Text("Default: ${provider.defaultModel} • Auto-pick finds best FREE vision model") },
                    singleLine = true
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    provider.presets.forEach { preset ->
                        SuggestionChip(
                            onClick = { model = preset.id; status = "" },
                            label = { Text(preset.label) }
                        )
                    }
                }

                OutlinedButton(
                    enabled = apiKey.isNotBlank() && !testing &&
                        (!provider.isCustom || customUrl.isNotBlank()),
                    onClick = {
                        apiKeyStore.setProviderId(providerId)
                        apiKeyStore.setApiKey(providerId, apiKey)
                        if (provider.isCustom) apiKeyStore.setCustomBaseUrl(customUrl)
                        testing = true
                        status = "✨ Scanning ${provider.name} for best FREE vision model…"
                        scope.launch {
                            val result = api.autoPickBestFreeModel()
                            testing = false
                            result.fold(
                                onSuccess = {
                                    model = it
                                    status = "🎯 ${dev.cluely.keyboard.domain.models.FreeModelPicker.reason(providerId, it)}"
                                },
                                onFailure = { status = "😅 Auto-pick failed: ${it.message}" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (testing) "⏳ Scanning…" else "✨ Auto-pick best FREE model")
                }

                Button(
                    enabled = apiKey.isNotBlank() && !testing &&
                        (!provider.isCustom || customUrl.isNotBlank()),
                    onClick = {
                        apiKeyStore.setProviderId(providerId)
                        apiKeyStore.setApiKey(providerId, apiKey)
                        apiKeyStore.setModel(providerId, model.ifBlank { provider.defaultModel })
                        if (provider.isCustom) apiKeyStore.setCustomBaseUrl(customUrl)
                        testing = true
                        status = "🔌 Testing ${provider.emoji} ${provider.name}…"
                        scope.launch {
                            val result = api.testConnection()
                            testing = false
                            // Refresh model field — testConnection auto-saves best free model.
                            model = apiKeyStore.getModel(providerId)
                            status = result.fold(
                                onSuccess = { "🎉 $it — you're good to go!" },
                                onFailure = { "😅 ${it.message ?: "Connection failed"}" }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (testing) "⏳ Testing…" else "💾 Save & test connection")
                }

                if (status.isNotBlank()) {
                    Text(status, color = MaterialTheme.colorScheme.primary)
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            memory.clear()
                            status = "🧠 Conversation memory cleared. Fresh start!"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🧠 Clear conversation memory")
                }

                Text(
                    "🎮 How to play:\n" +
                        "1. Open a chat 💬\n" +
                        "2. Tap the Cluely AI key 📸\n" +
                        "3. We snap + ask ${provider.name} 🧠\n" +
                        "4. Pick 1 of 3 spicy replies 🔥\n" +
                        "5. Tap to insert ✍️",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    "🔒 Privacy: key stays encrypted on-device. Screenshot is sent to ${provider.name} only when YOU tap AI. Memory stays local.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
