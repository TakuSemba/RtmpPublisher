package com.takusemba.rtmppublishersample

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import com.takusemba.rtmppublisher.RtmpPublisher

class MainActivity : AppCompatActivity() {

    private val publisher = RtmpPublisher()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val glView: GLSurfaceView = findViewById(R.id.surface_view)
        publisher.initialize(this, glView)

        findViewById<Button>(R.id.toggle_publish).setOnClickListener {
            if (publisher.isPublishing) {
                publisher.stopPublishing()
            } else {
                publisher.startPublishing(BuildConfig.STREAMING_URL)
            }
            updateControls()
        }

        findViewById<ImageView>(R.id.toggle_camera).setOnClickListener {
            publisher.switchCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        updateControls()
    }

    private fun updateControls() {
        val publishButton: Button = findViewById(R.id.toggle_publish)
        publishButton.text = getString(if (publisher.isPublishing)
            R.string.stop_publishing else R.string.start_publishing)
        publishButton.background = ContextCompat.getDrawable(this, if (publisher.isPublishing)
            R.drawable.round_corner_white_dark else R.drawable.round_corner_white_dark)
    }
}