package dev.cluely.keyboard.ui.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var keyboardListener: ((String?, Int) -> Unit)? = null
    private var isFocused = true
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private val keyMap = mapOf(
        // Row 1
        "q" to 0, "w" to 1, "e" to 2, "r" to 3, "t" to 4, "y" to 5, "u" to 6, "i" to 7, "o" to 8, "p" to 9,
        // Row 2
        "a" to 10, "s" to 11, "d" to 12, "f" to 13, "g" to 14, "h" to 15, "j" to 16, "k" to 17, "l" to 18,
        // Row 3
        "z" to 19, "x" to 20, "c" to 21, "v" to 22, "b" to 23, "n" to 24, "m" to 25,
        // Special keys
        "⌫" to 26, "AI" to 27
    )

    private val keyRects = mutableMapOf<Int, Rect>()
    private val keyLabels = listOf(
        // Row 1
        "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
        // Row 2
        "a", "s", "d", "f", "g", "h", "j", "k", "l",
        // Row 3
        "z", "x", "c", "v", "b", "n", "m",
        // Special keys
        "⌫", "AI"
    )

    fun setKeyboardListener(listener: (String?, Int) -> Unit) {
        keyboardListener = listener
    }

    fun setFocused(focused: Boolean) {
        isFocused = focused
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isFocused) return

        val width = width
        val height = height
        val keyWidth = width / 10f
        val keyHeight = height / 4f
        val padding = 2

        keyRects.clear()

        // Draw keys
        var keyIndex = 0
        for (row in 0 until 4) {
            for (col in 0 until 10) {
                if (keyIndex >= keyLabels.size) break

                val left = (col * keyWidth).toInt() + padding
                val top = (row * keyHeight).toInt() + padding
                val right = ((col + 1) * keyWidth).toInt() - padding
                val bottom = ((row + 1) * keyHeight).toInt() - padding

                val rect = Rect(left, top, right, bottom)
                keyRects[keyIndex] = rect

                // Draw background
                canvas.drawRect(rect, Paint().apply {
                    color = Color.LTGRAY
                })

                // Draw border
                canvas.drawRect(rect, Paint().apply {
                    color = Color.GRAY
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                })

                // Draw label
                val textX = (left + right) / 2f
                val textY = (top + bottom) / 2f + 15
                canvas.drawText(keyLabels[keyIndex], textX, textY, paint)

                keyIndex++
                if (keyIndex >= keyLabels.size) break
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isFocused) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                for ((index, rect) in keyRects) {
                    if (rect.contains(x.toInt(), y.toInt())) {
                        val label = keyLabels.getOrNull(index) ?: ""
                        when (label) {
                            "⌫" -> keyboardListener?.invoke(null, KeyEvent.KEYCODE_DEL)
                            "AI" -> keyboardListener?.invoke(null, SCREENSHOT_KEY_CODE)
                            else -> keyboardListener?.invoke(label.lowercase(), 0)
                        }
                        invalidate()
                        return true
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    companion object {
        private const val SCREENSHOT_KEY_CODE = -9001
    }
}