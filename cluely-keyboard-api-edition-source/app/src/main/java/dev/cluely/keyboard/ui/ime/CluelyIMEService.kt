package dev.cluely.keyboard.ui.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.FrameLayout
import dev.cluely.keyboard.R
import dev.cluely.keyboard.data.screenshot.ScreenshotManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CluelyIMEService : InputMethodService() {

    @Inject
    lateinit var screenshotManager: ScreenshotManager

    private lateinit var keyboardView: KeyboardView
    private lateinit var screenshotButton: Button

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_INSERT_SUGGESTION) {
            intent.getStringExtra(EXTRA_SUGGESTION)?.let { suggestion ->
                currentInputConnection?.commitText(suggestion, 1)
            }
        }
        return START_STICKY
    }

    override fun onCreateInputView(): View {
        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Create keyboard view
        keyboardView = KeyboardView(this).apply {
            setKeyboardListener { key, code ->
                when {
                    code == SCREENSHOT_KEY_CODE -> {
                        screenshotManager.captureScreen(this@CluelyIMEService)
                    }
                    code == KeyEvent.KEYCODE_DEL -> {
                        currentInputConnection?.deleteSurroundingText(1, 0)
                    }
                    code == KeyEvent.KEYCODE_SPACE -> {
                        currentInputConnection?.commitText(" ", 1)
                    }
                    code == KeyEvent.KEYCODE_ENTER -> {
                        currentInputConnection?.sendKeyEvent(
                            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                        )
                    }
                    key != null -> {
                        currentInputConnection?.commitText(key, 1)
                    }
                }
            }
        }

        container.addView(keyboardView)
        return container
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        keyboardView.setFocused(true)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        keyboardView.setFocused(false)
    }

    companion object {
        const val ACTION_INSERT_SUGGESTION = "dev.cluely.keyboard.INSERT_SUGGESTION"
        const val EXTRA_SUGGESTION = "suggestion"
        private const val SCREENSHOT_KEY_CODE = -9001
    }
}