package dev.cluely.keyboard.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import java.lang.ref.WeakReference

/**
 * User-enabled, request-only accessibility service.
 *
 * Primary stealth path: reads the active window UI tree only when the IME AI
 * key is tapped. No system screenshot is taken, so apps that listen for the
 * Android 14 screenshot detection API / MediaStore screenshot events are not
 * notified (Snapchat, Instagram Vanish, view-once media, etc.).
 *
 * No passive monitoring. eventTypes stays 0.
 */
class CluelyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply { eventTypes = 0 }
        instance = WeakReference(this)
        Log.i(TAG, "Accessibility text access enabled; waiting for explicit AI-key request")
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // Intentionally empty: no passive or continuous monitoring.
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance?.get() === this) instance = null
        super.onDestroy()
    }

    private fun extractActiveWindowText(): String {
        val root = rootInActiveWindow ?: return ""
        val packageName = root.packageName?.toString().orEmpty()
        val lines = LinkedHashSet<String>()
        try {
            collectVisibleText(root, lines)
        } catch (t: Throwable) {
            Log.w(TAG, "Window text extraction failed", t)
        }

        if (lines.isEmpty()) return ""

        // Prefer chat-like lines: drop pure UI chrome when possible.
        val filtered = lines.filterNot { isLikelyChrome(it) }
        val body = (if (filtered.size >= 2) filtered else lines.toList())
            .takeLast(MAX_MESSAGE_LINES)

        return buildString {
            if (packageName.isNotBlank()) append("app: ").append(packageName).append('\n')
            body.forEach { append(it).append('\n') }
        }.trim()
    }

    private fun collectVisibleText(node: AccessibilityNodeInfo, output: LinkedHashSet<String>) {
        if (!node.isVisibleToUser || output.size >= MAX_NODES) return

        val raw = sequenceOf(node.text, node.contentDescription)
            .mapNotNull { it?.toString()?.trim() }
            .firstOrNull { it.isNotEmpty() }

        if (raw != null) {
            val cleaned = raw
                .replace(Regex("\\s+"), " ")
                .take(MAX_TEXT_PER_NODE)
            if (cleaned.isNotBlank()) output += cleaned
        }

        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child -> collectVisibleText(child, output) }
            if (output.size >= MAX_NODES) return
        }
    }

    private fun isLikelyChrome(text: String): Boolean {
        val t = text.lowercase()
        if (t.length <= 1) return true
        val chrome = listOf(
            "type a message", "send", "search", "camera", "gallery", "voice message",
            "online", "last seen", "tap to", "reply", "forward", "copy", "delete",
            "more options", "call", "video", "mute", "notifications", "back",
            "close", "ok", "cancel", "settings", "profile", "stories", "reels"
        )
        return chrome.any { t == it || t.startsWith("$it ") }
    }

    companion object {
        private const val TAG = "CluelyAccessibility"
        private const val MAX_NODES = 400
        private const val MAX_TEXT_PER_NODE = 2_000
        private const val MAX_MESSAGE_LINES = 40
        private var instance: WeakReference<CluelyAccessibilityService>? = null

        /**
         * One active-window read only when invoked by the IME AI key.
         * Callback is delivered on the main thread.
         * Returns false if the service is not enabled.
         */
        fun requestActiveWindowText(onResult: (String) -> Unit): Boolean {
            val service = instance?.get() ?: return false
            service.mainExecutor.execute {
                onResult(service.extractActiveWindowText())
            }
            return true
        }

        fun isEnabled(): Boolean = instance?.get() != null
    }
}
