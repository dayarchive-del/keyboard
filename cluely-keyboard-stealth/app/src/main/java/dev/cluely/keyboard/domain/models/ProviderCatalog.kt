package dev.cluely.keyboard.domain.models

data class PresetModel(
    val id: String,
    val label: String,
    val vision: Boolean = true
)

data class LlmProvider(
    val id: String,
    val name: String,
    val emoji: String,
    val baseUrl: String,
    val defaultModel: String,
    val keyHint: String,
    val keyUrl: String,
    val helpText: String,
    val presets: List<PresetModel>,
    val isCustom: Boolean = false,
    val needsRefererHeaders: Boolean = false
)

object ProviderCatalog {
    const val OPENROUTER_ID = "openrouter"
    const val ZEN_ID = "zen"
    const val CUSTOM_ID = "custom"

    val OPENROUTER = LlmProvider(
        id = OPENROUTER_ID,
        name = "OpenRouter",
        emoji = "\uD83D\uDE80",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "openrouter/free",
        keyHint = "sk-or-v1-…",
        keyUrl = "https://openrouter.ai/keys",
        helpText = "One key, hundreds of models. Start with openrouter/free.",
        presets = listOf(
            PresetModel("openrouter/free", "✨ Free router (auto-picks free vision model)"),
            PresetModel("qwen/qwen2.5-vl-72b-instruct:free", "\uD83D\uDC40 Qwen VL 72B (free vision)"),
            PresetModel("google/gemini-flash-1.5", "⚡ Gemini Flash (fast + cheap vision)")
        ),
        needsRefererHeaders = true
    )

    val ZEN = LlmProvider(
        id = ZEN_ID,
        name = "OpenCode Zen",
        emoji = "🧘",
        baseUrl = "https://opencode.ai/zen/v1",
        defaultModel = "mimo-v2.5-free",
        keyHint = "zen-… (from opencode.ai/auth)",
        keyUrl = "https://opencode.ai/auth",
        helpText = "Curated coding models, several FREE. Get a key at opencode.ai/auth.",
        presets = listOf(
            PresetModel("mimo-v2.5-free", "✨ MiMo 2.5 Free (multimodal)"),
            PresetModel("deepseek-v4-flash-vision-exp", "\uD83D\uDC40 DeepSeek V4 Flash Vision"),
            PresetModel("big-pickle", "🥒 Big Pickle (free stealth)"),
            PresetModel("nemotron-3-ultra-free", "🆓 Nemotron 3 Ultra Free"),
            PresetModel("muse-spark-1.3-contributor-free", "💬 Muse Spark (text only)", vision = false)
        )
    )

    val CUSTOM = LlmProvider(
        id = CUSTOM_ID,
        name = "Custom",
        emoji = "🔧",
        baseUrl = "",
        defaultModel = "gpt-4o-mini",
        keyHint = "paste any OpenAI-compatible key…",
        keyUrl = "",
        helpText = "Any OpenAI-compatible endpoint (Ollama, LM Studio, vLLM, OpenAI…). Must serve POST {baseUrl}/chat/completions + GET {baseUrl}/models.",
        presets = listOf(
            PresetModel("gpt-4o-mini", "GPT-4o mini"),
            PresetModel("qwen2.5-vl", "Qwen VL (local)")
        ),
        isCustom = true
    )

    val ALL = listOf(OPENROUTER, ZEN, CUSTOM)

    fun byId(id: String?): LlmProvider = ALL.firstOrNull { it.id == id } ?: OPENROUTER
}
