package dev.cluely.keyboard.domain.models

/**
 * Picks the best FREE vision-capable model for Cluely's needs:
 * screenshot (image_url) + short chat replies.
 *
 * Strategy:
 * 1. Prefer provider's curated vision presets, in order (if present in /models).
 * 2. Else heuristics: contains "free" + vision keywords, skip known text-only.
 * 3. Else first available model (so the app still works).
 */
object FreeModelPicker {

    private val TEXT_ONLY_IDS = setOf(
        "muse-spark-1.3-contributor-free",
        "muse-spark-1.2-contributor-free",
        "jev-1.13-free",
        "jev-1.13"
    )

    private val VISION_HINTS = listOf(
        "openrouter/free", // router auto-picks a free vision model
        "mimo", "qwen", "vl", "vision", "multimodal",
        "gemini-flash", "gemini-2", "gpt-4o", "claude-3.5",
        "pickle", "bunny", "longcat", "nemotron", "ling",
        "flash", "exp"
    )

    fun pickBest(providerId: String, availableIds: List<String>): String? {
        if (availableIds.isEmpty()) return null
        val provider = ProviderCatalog.byId(providerId)
        val available = availableIds.toSet()

        // 1. Curated vision presets first.
        for (preset in provider.presets) {
            if (preset.vision && available.contains(preset.id)) return preset.id
        }

        // 2. Heuristic scoring.
        val scored = availableIds
            .filter { it !in TEXT_ONLY_IDS }
            .map { id -> id to score(providerId, id) }
            .sortedByDescending { it.second }

        // Prefer a free scored model; otherwise best scored.
        val free = scored.firstOrNull { (id, _) -> id.contains("free", ignoreCase = true) }
        if (free != null && free.second > 0) return free.first
        if (scored.isNotEmpty() && scored.first().second > 0) return scored.first().first

        // 3. Fallback: first non-text-only, else first overall, else provider default.
        return availableIds.firstOrNull { it !in TEXT_ONLY_IDS }
            ?: availableIds.firstOrNull()
            ?: provider.defaultModel
    }

    private fun score(providerId: String, id: String): Int {
        val lower = id.lowercase()
        var s = 0
        if (lower.contains("free")) s += 50
        for (hint in VISION_HINTS) {
            if (lower.contains(hint)) s += 20
        }
        if (lower.contains("openrouter/free")) s += 500
        if (providerId == ProviderCatalog.ZEN_ID) {
            // Zen curated order for this app.
            when {
                lower == "mimo-v2.5-free" -> s += 300
                lower == "mimo-v2.6-flash-free" -> s += 290
                lower == "big-pickle" -> s += 280
                lower == "space-bunny-free" -> s += 270
                lower == "longcat-2.5-preview-free" -> s += 260
                lower.contains("nemotron") && lower.contains("free") -> s += 200
                lower.contains("ling") && lower.contains("free") -> s += 190
            }
        }
        if (providerId == ProviderCatalog.OPENROUTER_ID) {
            if (lower.endsWith(":free")) s += 100
            if (lower.contains("qwen") && lower.contains("vl")) s += 80
        }
        if (lower.contains("tts") || lower.contains("embed") || lower.contains("whisper")) s -= 200
        return s
    }

    fun reason(providerId: String, modelId: String): String {
        val provider = ProviderCatalog.byId(providerId)
        return when {
            modelId.contains("free", ignoreCase = true) || modelId == "openrouter/free" || modelId == "big-pickle" ->
                "🎯 Auto-picked FREE vision model for ${provider.name}: $modelId"
            else ->
                "🎯 Auto-picked best available for ${provider.name}: $modelId (no free vision model found)"
        }
    }
}
