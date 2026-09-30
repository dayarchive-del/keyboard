package dev.cluely.keyboard.data.screenshot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MediaProjectionActivity : ComponentActivity() {
    
    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = Intent(ACTION_MEDIA_PROJECTION_RESULT).apply {
            putExtra(EXTRA_RESULT_CODE, result.resultCode)
            putExtra(EXTRA_RESULT_DATA, result.data)
        }
        sendBroadcast(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(captureIntent)
    }

    companion object {
        const val ACTION_MEDIA_PROJECTION_RESULT = "dev.cluely.keyboard.MEDIA_PROJECTION_RESULT"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
    }
}

