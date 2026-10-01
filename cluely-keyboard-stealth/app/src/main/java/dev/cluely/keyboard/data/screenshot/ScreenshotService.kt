package dev.cluely.keyboard.data.screenshot

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.cluely.keyboard.R
import dev.cluely.keyboard.ui.overlay.ChatOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ScreenshotService : Service() {

    @Inject
    lateinit var windowManager: WindowManager

    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var resultReceiver: BroadcastReceiver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startCaptureForeground()
        registerResultReceiver()
        startCapture()
        return START_NOT_STICKY
    }



    private fun startCaptureForeground() {
        val channelId = "cluely_capture"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Cluely screen capture",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Cluely")
            .setContentText("Capturing the screen for AI analysis")
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun registerResultReceiver() {
        resultReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == MediaProjectionActivity.ACTION_MEDIA_PROJECTION_RESULT) {
                    val resultCode = intent.getIntExtra(MediaProjectionActivity.EXTRA_RESULT_CODE, -1)
                    val resultData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(MediaProjectionActivity.EXTRA_RESULT_DATA, Intent::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra<Intent>(MediaProjectionActivity.EXTRA_RESULT_DATA)
                    }
                    
                    if (resultCode == android.app.Activity.RESULT_OK && resultData != null) {
                        mediaProjection = mediaProjectionManager?.getMediaProjection(resultCode, resultData)
                        captureScreenshot()
                    } else {
                        Log.e("ScreenshotService", "MediaProjection permission denied")
                        stopSelf()
                    }
                }
            }
        }
        val filter = IntentFilter(MediaProjectionActivity.ACTION_MEDIA_PROJECTION_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(resultReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(resultReceiver, filter)
        }
    }

    private fun startCapture() {
        try {
            mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val intent = Intent(this, MediaProjectionActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("ScreenshotService", "Error starting capture", e)
            stopSelf()
        }
    }

    private fun captureScreenshot() {
        scope.launch {
            try {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels
                val density = displayMetrics.densityDpi

                val imageReader = android.media.ImageReader.newInstance(
                    width, height, PixelFormat.RGBA_8888, 2
                )

                mediaProjection?.createVirtualDisplay(
                    "ScreenCapture",
                    width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    imageReader.surface,
                    null, null
                )

                val image = imageReader.acquireLatestImage()
                if (image != null) {
                    val bitmap = imageToBitmap(image)
                    val base64 = bitmapToBase64(bitmap)
                    
                    // Launch chat overlay with screenshot
                    val intent = Intent(this@ScreenshotService, ChatOverlayService::class.java).apply {
                        putExtra("screenshot_base64", base64)
                    }
                    startService(intent)

                    image.close()
                }
                imageReader.close()
                mediaProjection?.stop()
            } catch (e: Exception) {
                Log.e("ScreenshotService", "Error capturing screenshot", e)
            } finally {
                stopSelf()
            }
        }
    }

    private fun imageToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val width = image.width
        val height = image.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Compress for faster API calls
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onDestroy() {
        super.onDestroy()
        resultReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.e("ScreenshotService", "Error unregistering receiver", e)
            }
        }
        scope.cancel()
        mediaProjection?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}